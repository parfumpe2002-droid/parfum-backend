package com.parfum.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parfum.jpa.entity.DecantEnvase;
import com.parfum.jpa.entity.Producto;
import com.parfum.jpa.entity.ProductoDecant;
import com.parfum.jpa.entity.Rol;
import com.parfum.jpa.entity.Usuario;
import com.parfum.jpa.repository.DecantEnvaseRepository;
import com.parfum.jpa.repository.ProductoRepository;
import com.parfum.jpa.repository.UsuarioRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inicialización segura de Parfum.
 *
 * La API y el login NO dependen de que termine el catálogo. El catálogo se
 * sincroniza después de ApplicationReady y cada perfume se procesa de forma
 * independiente. De esta forma una fila problemática nunca apaga Render.
 *
 * Los SKU del JSON son la identidad canónica. Los SKU históricos se mantienen
 * iguales a los que ya existían en Neon y los productos nuevos usan PAR-D26-*.
 * Las imágenes Cloudinary de una fila existente nunca se reemplazan aquí.
 */
@Component
public class DataInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final int EXPECTED_CATALOG_SIZE = 86;

    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final DecantEnvaseRepository envases;
    private final PasswordEncoder encoder;
    private final ObjectMapper objectMapper;
    private final boolean catalogSync;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;

    public DataInitializer(
            ProductoRepository productos,
            UsuarioRepository usuarios,
            DecantEnvaseRepository envases,
            PasswordEncoder encoder,
            ObjectMapper objectMapper,
            @Value("${app.catalog-sync:false}") boolean catalogSync,
            @Value("${app.admin-email}") String adminEmail,
            @Value("${app.admin-password}") String adminPassword,
            @Value("${app.admin-name}") String adminName) {
        this.productos = productos;
        this.usuarios = usuarios;
        this.envases = envases;
        this.encoder = encoder;
        this.objectMapper = objectMapper;
        this.catalogSync = catalogSync;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminName = adminName;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("PARFUM backend v1.1.0: catálogo administrado desde Neon; sincronización automática={}", catalogSync);
        try {
            createAdmin();
        } catch (Exception error) {
            log.error("No se pudo sincronizar el administrador. La API continuará disponible.", error);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncCatalogAfterStartup() {
        // En producción, los precios, stock e imágenes se editan desde el panel Admin.
        // Por seguridad NO volvemos a importar el JSON en cada deploy: hacerlo podría
        // pisar cambios reales del cliente. La reparación queda disponible solo si
        // APP_CATALOG_SYNC=true se activa de forma temporal y consciente.
        if (!catalogSync) {
            log.info("CATALOGO PROTEGIDO: se conservan productos, precios, stock e imágenes existentes en Neon");
            return;
        }
        log.warn("APP_CATALOG_SYNC=true: se ejecutará la sincronización de reparación del catálogo");

        Thread worker = new Thread(() -> {
            final int maxAttempts = 5;
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                int ok = 0;
                int failed = 0;
                try {
                    List<SeedProduct> seeds = seedProducts();
                    log.info("CATALOGO: intento {}/{} - iniciando sincronización de {} perfumes",
                            attempt, maxAttempts, seeds.size());
                    List<DecantEnvase> containers = seedDecantContainers();

                    for (SeedProduct seed : seeds) {
                        try {
                            upsertSeedProduct(seed, containers);
                            ok++;
                        } catch (Exception error) {
                            failed++;
                            log.error("CATALOGO: error aislado en {} [{}]. Se continúa con el siguiente perfume.",
                                    seed.nombre(), seed.sku(), error);
                        }
                    }

                    long active = productos.countByActivoTrue();
                    long total = productos.count();
                    long present = countCatalogProductsPresent(seeds);
                    log.info("CATALOGO: intento {} terminado. correctos={}, errores={}, catalogoPresente={}/{}, totalEnNeon={}, activosEnNeon={}",
                            attempt, ok, failed, present, seeds.size(), total, active);

                    if (present == seeds.size()) {
                        log.info("CATALOGO READY: los {} perfumes del archivo ya existen en Neon", seeds.size());
                        return;
                    }
                } catch (Exception error) {
                    log.error("CATALOGO: error general en intento {}. La API seguirá funcionando.", attempt, error);
                }

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(15000L * attempt);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            log.error("CATALOGO: no se logró completar la reparación tras {} intentos. Revisar los errores aislados anteriores.", maxAttempts);
        }, "parfum-catalog-sync-v110");
        worker.start();
    }

    private void upsertSeedProduct(SeedProduct seed, List<DecantEnvase> containers) {
        Producto product = resolveSeedProduct(seed);
        boolean isNew = product.getId() == null;
        boolean wasQuarantined = (!isNew && product.getSku() != null && product.getSku().startsWith("LEGACY-"))
                || (!isNew && product.getSlug() != null && product.getSlug().startsWith("duplicado-"));

        // Repara SKU robados por deploys anteriores. La identidad real se decide
        // por slug/nombre+marca; si otro registro ocupa el SKU canónico por error,
        // se le asigna un SKU de cuarentena y se libera el correcto.
        assignCanonicalSku(product, seed);

        // Si una versión fallida dejó otra fila con el slug que necesitamos, esa
        // fila se aparta sin borrarla. Así no perdemos datos ni chocamos con UNIQUE.
        makeSlugAvailable(product, seed.slug());

        product.setSlug(seed.slug());
        product.setNombre(seed.nombre());
        product.setMarca(seed.marca());
        product.setCategoria(seed.categoria());
        product.setGenero(seed.genero());
        product.setConcentracion(seed.concentracion());
        product.setDecantDisponible(true);

        // El concepto actual de la tienda es SOLO decants. Las antiguas
        // presentaciones de botella no deben intervenir en precio/stock ni carrito.
        if (!product.getPresentaciones().isEmpty()) product.limpiarPresentaciones();

        if (isNew || isLegacyDescription(product.getDescripcion())) product.setDescripcion(seed.descripcion());
        setIfMissingOrPlaceholder(product.getFamiliaOlfativa(), product::setFamiliaOlfativa, seed.familiaOlfativa());
        if (product.getAnoLanzamiento() == null) product.setAnoLanzamiento(seed.anoLanzamiento());
        setIfMissingOrPlaceholder(product.getPerfumista(), product::setPerfumista, seed.perfumista());
        setIfMissingOrPlaceholder(product.getNotasSalida(), product::setNotasSalida, seed.notasSalida());
        setIfMissingOrPlaceholder(product.getNotasCorazon(), product::setNotasCorazon, seed.notasCorazon());
        setIfMissingOrPlaceholder(product.getNotasFondo(), product::setNotasFondo, seed.notasFondo());
        setIfMissingOrPlaceholder(product.getAcordesPrincipales(), product::setAcordesPrincipales, seed.acordesPrincipales());
        setIfBlank(product.getDuracion(), product::setDuracion, seed.duracion());
        setIfBlank(product.getProyeccion(), product::setProyeccion, seed.proyeccion());
        setIfBlank(product.getEstaciones(), product::setEstaciones, seed.estaciones());
        setIfBlank(product.getOcasiones(), product::setOcasiones, seed.ocasiones());
        setIfBlank(product.getEstilo(), product::setEstilo, seed.estilo());
        setIfMissingOrPlaceholder(product.getFuenteReferencia(), product::setFuenteReferencia, seed.fuenteReferencia());

        // Cloudinary manda para imágenes. Solo rellenamos la imagen local de respaldo.
        if (product.getFallbackImage() == null || product.getFallbackImage().isBlank()) {
            product.setFallbackImage(seed.fallbackImage());
        }

        if (isNew) {
            product.setDestacado(seed.destacado());
        }
        if (isNew || wasQuarantined) {
            product.setActivo(seed.activo());
        }

        // Guardamos primero para obtener ID en productos nuevos y luego asociar decants.
        product = productos.saveAndFlush(product);
        syncDecants(product, seed, containers);
        product.recalcularResumenComercial();
        productos.saveAndFlush(product);
    }

    /**
     * Prioridad de identidad:
     * 1) SKU canónico (repara nombres/slugs alterados por deploys previos),
     * 2) slug,
     * 3) nombre + marca,
     * 4) fila nueva.
     */
    private Producto resolveSeedProduct(SeedProduct seed) {
        // IMPORTANTE: no buscar primero por SKU. La base actual puede contener
        // filas como Althair con PAR-H-004 debido a un deploy anterior. Buscar
        // por SKU primero convertiría Althair en Stronger With You.
        Optional<Producto> bySlug = productos.findBySlugIgnoreCase(seed.slug());
        if (bySlug.isPresent() && identityMatches(bySlug.get(), seed)) return bySlug.get();

        Optional<Producto> byName = productos.findByNombreIgnoreCaseAndMarcaIgnoreCase(seed.nombre(), seed.marca());
        if (byName.isPresent()) return byName.get();

        Optional<Producto> bySku = productos.findBySkuIgnoreCase(seed.sku());
        if (bySku.isPresent() && identityMatches(bySku.get(), seed)) return bySku.get();

        // Si el slug existe pero fue parcialmente corrompido, preferimos reparar
        // esa misma fila para conservar imagen Cloudinary y referencias existentes.
        if (bySlug.isPresent()) return bySlug.get();

        return new Producto();
    }

    private boolean identityMatches(Producto product, SeedProduct seed) {
        if (product == null) return false;
        String productSlug = normalizeText(product.getSlug());
        String seedSlug = normalizeText(seed.slug());
        if (!productSlug.isBlank() && productSlug.equals(seedSlug)) return true;

        return normalizeText(product.getNombre()).equals(normalizeText(seed.nombre()))
                && normalizeText(product.getMarca()).equals(normalizeText(seed.marca()));
    }

    private void assignCanonicalSku(Producto target, SeedProduct seed) {
        Optional<Producto> holderOpt = productos.findBySkuIgnoreCase(seed.sku());
        if (holderOpt.isPresent()) {
            Producto holder = holderOpt.get();
            boolean sameRow = target.getId() != null && target.getId().equals(holder.getId());
            if (!sameRow) {
                String quarantine = "LEGACY-" + holder.getId();
                holder.setSku(quarantine);
                productos.saveAndFlush(holder);
                log.warn("CATALOGO: SKU {} estaba ocupado incorrectamente por id {} ({}). Se movió a {}",
                        seed.sku(), holder.getId(), holder.getNombre(), quarantine);
            }
        }
        target.setSku(seed.sku());
    }

    private void makeSlugAvailable(Producto target, String desiredSlug) {
        productos.findBySlugIgnoreCase(desiredSlug).ifPresent(holder -> {
            if (target.getId() != null && target.getId().equals(holder.getId())) return;

            // Rescatamos una imagen administrada si la fila canónica aún no tiene una.
            if ((target.getImagenUrl() == null || target.getImagenUrl().isBlank())
                    && holder.getImagenUrl() != null && !holder.getImagenUrl().isBlank()) {
                target.setImagenUrl(holder.getImagenUrl());
                target.setImagenPublicId(holder.getImagenPublicId());
            }

            String quarantined = "duplicado-" + holder.getId() + "-" + desiredSlug;
            if (quarantined.length() > 175) quarantined = quarantined.substring(0, 175);
            holder.setSlug(quarantined);
            holder.setActivo(false);
            holder.setDecantDisponible(false);
            productos.saveAndFlush(holder);
            log.warn("CATALOGO: slug {} estaba duplicado en id {}. Fila apartada como {}",
                    desiredSlug, holder.getId(), quarantined);
        });
    }

    private void syncDecants(Producto product, SeedProduct seed, List<DecantEnvase> containers) {
        for (DecantEnvase canonicalEnvase : containers) {
            Integer ml = canonicalEnvase.getMililitros();

            // Versiones anteriores crearon varios envases con el mismo ml y
            // nombres distintos. Reutilizamos cualquier decant existente de ese
            // tamaño para no duplicar más filas.
            ProductoDecant decant = product.getDecants().stream()
                    .filter(item -> item.getEnvase() != null)
                    .filter(item -> java.util.Objects.equals(item.getEnvase().getMililitros(), ml))
                    .sorted(Comparator.comparing(ProductoDecant::isActivo).reversed()
                            .thenComparing(item -> item.getId() == null ? Long.MAX_VALUE : item.getId()))
                    .findFirst().orElse(null);

            boolean isNewDecant = decant == null;
            if (isNewDecant) {
                decant = new ProductoDecant();
                decant.setEnvase(canonicalEnvase);
                product.agregarDecant(decant);
            }

            SeedDecant source = findSeedDecant(seed, ml);
            if (source != null && source.precio() != null && source.precio().signum() > 0) {
                decant.setPrecio(source.precio());
                decant.setStock(source.stock() == null ? 100 : source.stock());
                decant.setActivo(source.activo());
            } else if (isNewDecant) {
                // Los 12 perfumes de mujer no tienen precios en el Excel.
                decant.setPrecio(BigDecimal.ZERO);
                decant.setStock(0);
                decant.setActivo(false);
            }
            decant.setOrdenVisual(canonicalEnvase.getOrdenVisual());
        }
        product.getDecants().sort(Comparator.comparing(ProductoDecant::getOrdenVisual));
    }

    private SeedDecant findSeedDecant(SeedProduct seed, Integer ml) {
        if (seed.decants() == null) return null;
        return seed.decants().stream()
                .filter(item -> java.util.Objects.equals(item.mililitros(), ml))
                .findFirst().orElse(null);
    }

    private List<DecantEnvase> seedDecantContainers() {
        List<ContainerSeed> seeds = List.of(
                new ContainerSeed("Vidrio negro 3 ml", 3,
                        "Frasco atomizador de vidrio de 3 ml.",
                        "imagen/decants/decant-3ml.png", 10),
                new ContainerSeed("Vidrio negro 5 ml", 5,
                        "Frasco atomizador de vidrio de 5 ml.",
                        "imagen/decants/decant-5ml.png", 20),
                new ContainerSeed("Premium degradé 10 ml", 10,
                        "Presentación premium degradada de 10 ml.",
                        "imagen/decants/decant-10ml-premium.png", 30),
                new ContainerSeed("Atomizador 20 ml", 20,
                        "Atomizador de 20 ml para decant.",
                        "imagen/decants/decant-10ml-premium.png", 40),
                new ContainerSeed("Atomizador 30 ml", 30,
                        "Atomizador de 30 ml para decant.",
                        "imagen/decants/decant-10ml-premium.png", 50)
        );

        for (ContainerSeed seed : seeds) {
            // Buscar por mililitros, no por nombre. La BD histórica contiene
            // nombres distintos para 3/5/10 ml y eso generaba duplicados.
            DecantEnvase envase = envases.findAll().stream()
                    .filter(item -> java.util.Objects.equals(seed.mililitros(), item.getMililitros()))
                    .sorted(Comparator.comparing(DecantEnvase::isActivo).reversed()
                            .thenComparing(item -> item.getId() == null ? Long.MAX_VALUE : item.getId()))
                    .findFirst().orElse(null);

            boolean isNew = envase == null;
            if (isNew) {
                envase = new DecantEnvase();
                envase.setNombre(seed.nombre());
                envase.setMililitros(seed.mililitros());
                envase.setDescripcion(seed.descripcion());
                envase.setActivo(true);
            }
            if (envase.getDescripcion() == null || envase.getDescripcion().isBlank()) {
                envase.setDescripcion(seed.descripcion());
            }
            if (envase.getImagenUrl() == null || envase.getImagenUrl().isBlank()) {
                envase.setFallbackImage(seed.fallbackImage());
            }
            envase.setOrdenVisual(seed.orden());
            envases.saveAndFlush(envase);
        }

        return envases.findAll().stream()
                .filter(item -> List.of(3, 5, 10, 20, 30).contains(item.getMililitros()))
                .collect(java.util.stream.Collectors.toMap(
                        DecantEnvase::getMililitros,
                        item -> item,
                        (a, b) -> a.getId() != null && b.getId() != null && a.getId() <= b.getId() ? a : b,
                        java.util.TreeMap::new))
                .values().stream().toList();
    }

    private boolean isLegacyDescription(String description) {
        if (description == null || description.isBlank()) return true;
        String normalized = description.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("fragancia masculina de")
                || normalized.startsWith("fragancia masculina de perfumería");
    }

    private void setIfMissingOrPlaceholder(String current, java.util.function.Consumer<String> setter, String seedValue) {
        String normalized = current == null ? "" : current.trim().toLowerCase(Locale.ROOT);
        boolean missing = normalized.isBlank()
                || normalized.equals("por consultar")
                || normalized.equals("https://www.fragrantica.es/")
                || normalized.equals("https://fragrantica.es/");
        if (missing && seedValue != null && !seedValue.isBlank()) {
            setter.accept(seedValue);
        }
    }

    private void setIfBlank(String current, java.util.function.Consumer<String> setter, String seedValue) {
        if ((current == null || current.isBlank()) && seedValue != null && !seedValue.isBlank()) {
            setter.accept(seedValue);
        }
    }

    private void createAdmin() {
        if (adminEmail == null || adminEmail.isBlank()) return;
        String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        Usuario admin = usuarios.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (admin == null) {
            if (adminPassword == null || adminPassword.isBlank()) return;
            admin = new Usuario();
            admin.setNombre(adminName == null || adminName.isBlank() ? "Administrador Parfum" : adminName.trim());
            admin.setEmail(normalizedEmail);
            admin.setPasswordHash(encoder.encode(adminPassword));
        }
        admin.setRol(Rol.ADMIN);
        admin.setActivo(true);
        usuarios.save(admin);
    }

    private long countCatalogProductsPresent(List<SeedProduct> seeds) {
        return seeds.stream().filter(seed ->
                productos.findBySlugIgnoreCase(seed.slug()).isPresent()
                        || productos.findByNombreIgnoreCaseAndMarcaIgnoreCase(seed.nombre(), seed.marca()).isPresent()
        ).count();
    }

    private List<SeedProduct> seedProducts() {
        try {
            ClassPathResource resource = new ClassPathResource("catalogo/perfumes.json");
            List<SeedProduct> seeds = objectMapper.readValue(resource.getInputStream(), new TypeReference<List<SeedProduct>>() {});
            if (seeds.size() != EXPECTED_CATALOG_SIZE) {
                log.warn("CATALOGO: se esperaban {} perfumes y el JSON contiene {}", EXPECTED_CATALOG_SIZE, seeds.size());
            }
            return seeds;
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo leer el catálogo inicial de perfumes", error);
        }
    }

    private String normalizeText(String value) {
        if (value == null) return "";
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeSku(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    public record SeedProduct(
            String sku, String slug, String nombre, String marca, String categoria, String genero,
            String concentracion, String descripcion, BigDecimal precio, Integer stock,
            String fallbackImage, boolean destacado, boolean activo, String familiaOlfativa,
            Integer anoLanzamiento, String perfumista, String notasSalida, String notasCorazon,
            String notasFondo, String acordesPrincipales, String duracion, String proyeccion,
            String estaciones, String ocasiones, String estilo, String fuenteReferencia,
            List<Object> presentaciones, List<SeedDecant> decants) {}

    public record SeedDecant(Integer mililitros, BigDecimal precio, Integer stock, boolean activo) {}
    private record ContainerSeed(String nombre, Integer mililitros, String descripcion, String fallbackImage, Integer orden) {}
}
