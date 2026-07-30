package com.parfum.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parfum.jpa.entity.DecantEnvase;
import com.parfum.jpa.entity.Producto;
import com.parfum.jpa.entity.ProductoDecant;
import com.parfum.jpa.entity.ProductoPresentacion;
import com.parfum.jpa.entity.Rol;
import com.parfum.jpa.entity.Usuario;
import com.parfum.jpa.repository.DecantEnvaseRepository;
import com.parfum.jpa.repository.ProductoRepository;
import com.parfum.jpa.repository.UsuarioRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final DecantEnvaseRepository envases;
    private final PasswordEncoder encoder;
    private final ObjectMapper objectMapper;
    private final boolean seedData;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminName;

    public DataInitializer(
            ProductoRepository productos,
            UsuarioRepository usuarios,
            DecantEnvaseRepository envases,
            PasswordEncoder encoder,
            ObjectMapper objectMapper,
            @Value("${app.seed-data}") boolean seedData,
            @Value("${app.admin-email}") String adminEmail,
            @Value("${app.admin-password}") String adminPassword,
            @Value("${app.admin-name}") String adminName) {
        this.productos = productos;
        this.usuarios = usuarios;
        this.envases = envases;
        this.encoder = encoder;
        this.objectMapper = objectMapper;
        this.seedData = seedData;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminName = adminName;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<DecantEnvase> defaultContainers = seedDecantContainers();
        if (seedData) seedCatalog(false);
        seedArabicProducts();
        attachDefaultDecants(defaultContainers);
        createAdmin();
    }

    private void seedCatalog(boolean onlyArabic) {
        for (SeedProduct seed : seedProducts()) {
            boolean arabic = normalize(seed.categoria()).contains("arabe");
            if (onlyArabic && !arabic) continue;
            upsertSeedProduct(seed);
        }
    }

    private void seedArabicProducts() {
        seedCatalog(true);
    }

    private void upsertSeedProduct(SeedProduct seed) {
        Producto product = productos.findBySkuIgnoreCase(seed.sku())
                .or(() -> productos.findByNombreIgnoreCaseAndMarcaIgnoreCase(seed.nombre(), seed.marca()))
                .orElseGet(Producto::new);

        boolean isNew = product.getId() == null;
        product.setSku(seed.sku());
        product.setSlug(seed.slug());
        product.setNombre(seed.nombre());
        product.setMarca(seed.marca());
        product.setCategoria(seed.categoria());
        product.setGenero(seed.genero());
        product.setConcentracion(seed.concentracion());
        product.setDecantDisponible(true);

        if (isNew || isLegacyDescription(product.getDescripcion())) product.setDescripcion(seed.descripcion());
        setIfBlank(product.getFamiliaOlfativa(), product::setFamiliaOlfativa, seed.familiaOlfativa());
        if (product.getAnoLanzamiento() == null) product.setAnoLanzamiento(seed.anoLanzamiento());
        setIfBlank(product.getPerfumista(), product::setPerfumista, seed.perfumista());
        setIfBlank(product.getNotasSalida(), product::setNotasSalida, seed.notasSalida());
        setIfBlank(product.getNotasCorazon(), product::setNotasCorazon, seed.notasCorazon());
        setIfBlank(product.getNotasFondo(), product::setNotasFondo, seed.notasFondo());
        setIfBlank(product.getAcordesPrincipales(), product::setAcordesPrincipales, seed.acordesPrincipales());
        setIfBlank(product.getDuracion(), product::setDuracion, seed.duracion());
        setIfBlank(product.getProyeccion(), product::setProyeccion, seed.proyeccion());
        setIfBlank(product.getEstaciones(), product::setEstaciones, seed.estaciones());
        setIfBlank(product.getOcasiones(), product::setOcasiones, seed.ocasiones());
        setIfBlank(product.getEstilo(), product::setEstilo, seed.estilo());
        setIfBlank(product.getFuenteReferencia(), product::setFuenteReferencia, seed.fuenteReferencia());

        if (isNew) {
            product.setPrecio(seed.precio());
            product.setStock(seed.stock());
            product.setDestacado(seed.destacado());
            product.setActivo(seed.activo());
        }
        if (seed.fallbackImage() != null && !seed.fallbackImage().isBlank()) {
            product.setFallbackImage(seed.fallbackImage());
        }
        removeAccidentalDecantsFromBottleSizes(product);
        if (product.getPresentaciones().isEmpty() && seed.presentaciones() != null) {
            int order = 0;
            for (SeedPresentation item : seed.presentaciones()) {
                ProductoPresentacion presentation = new ProductoPresentacion();
                presentation.setMililitros(item.mililitros());
                presentation.setPrecio(item.precio() == null ? BigDecimal.ZERO : item.precio());
                presentation.setStock(item.stock() == null ? 0 : item.stock());
                presentation.setActivo(item.activo());
                presentation.setOrdenVisual(order++);
                product.agregarPresentacion(presentation);
            }
        }
        product.recalcularResumenComercial();
        productos.save(product);
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
                        "Presentación premium degradada de 10 ml. El administrador puede agregar más diseños de 10 ml.",
                        "imagen/decants/decant-10ml-premium.png", 30)
        );
        for (ContainerSeed seed : seeds) {
            DecantEnvase envase = envases.findAll().stream()
                    .filter(item -> seed.nombre().equalsIgnoreCase(item.getNombre()))
                    .findFirst().orElseGet(DecantEnvase::new);
            envase.setNombre(seed.nombre());
            envase.setMililitros(seed.mililitros());
            envase.setDescripcion(seed.descripcion());
            if (envase.getImagenUrl() == null || envase.getImagenUrl().isBlank()) {
                envase.setFallbackImage(seed.fallbackImage());
            }
            envase.setActivo(true);
            envase.setOrdenVisual(seed.orden());
            envases.save(envase);
        }
        return envases.findByActivoTrueOrderByMililitrosAscOrdenVisualAsc();
    }

    private void attachDefaultDecants(List<DecantEnvase> containers) {
        for (Producto product : productos.findAll()) {
            product.setDecantDisponible(true);
            removeAccidentalDecantsFromBottleSizes(product);
            for (DecantEnvase envase : containers) {
                boolean exists = product.getDecants().stream()
                        .anyMatch(item -> item.getEnvase() != null && envase.getId().equals(item.getEnvase().getId()));
                if (exists) continue;
                ProductoDecant decant = new ProductoDecant();
                decant.setEnvase(envase);
                decant.setPrecio(BigDecimal.ZERO);
                decant.setStock(0);
                decant.setActivo(true);
                decant.setOrdenVisual(envase.getOrdenVisual());
                product.agregarDecant(decant);
            }
            product.getDecants().sort(Comparator.comparing(ProductoDecant::getOrdenVisual));
            product.recalcularResumenComercial();
            productos.save(product);
        }
    }

    private void removeAccidentalDecantsFromBottleSizes(Producto product) {
        product.getPresentaciones().removeIf(item ->
                item.getMililitros() != null
                        && List.of(3, 5, 10).contains(item.getMililitros())
                        && (item.getPrecio() == null || item.getPrecio().signum() == 0)
                        && (item.getStock() == null || item.getStock() == 0)
                        && item.getOrdenVisual() != null
                        && item.getOrdenVisual() < 0);
    }

    private boolean isLegacyDescription(String description) {
        if (description == null || description.isBlank()) return true;
        String normalized = description.trim().toLowerCase();
        return normalized.startsWith("fragancia masculina de")
                || normalized.startsWith("fragancia masculina de perfumería");
    }

    private void setIfBlank(String current, java.util.function.Consumer<String> setter, String seedValue) {
        if ((current == null || current.isBlank()) && seedValue != null && !seedValue.isBlank()) {
            setter.accept(seedValue);
        }
    }

    private void createAdmin() {
        if (adminEmail == null || adminEmail.isBlank()) return;
        String normalizedEmail = adminEmail.trim().toLowerCase();
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

    private List<SeedProduct> seedProducts() {
        try {
            ClassPathResource resource = new ClassPathResource("catalogo/perfumes.json");
            return objectMapper.readValue(resource.getInputStream(), new TypeReference<List<SeedProduct>>() {});
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo leer el catálogo inicial de perfumes", error);
        }
    }

    private String normalize(String value) {
        if (value == null) return "";
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    public record SeedProduct(
            String sku, String slug, String nombre, String marca, String categoria, String genero,
            String concentracion, String descripcion, BigDecimal precio, Integer stock,
            String fallbackImage, boolean destacado, boolean activo, String familiaOlfativa,
            Integer anoLanzamiento, String perfumista, String notasSalida, String notasCorazon,
            String notasFondo, String acordesPrincipales, String duracion, String proyeccion,
            String estaciones, String ocasiones, String estilo, String fuenteReferencia,
            List<SeedPresentation> presentaciones) {}

    public record SeedPresentation(Integer mililitros, BigDecimal precio, Integer stock, boolean activo) {}
    private record ContainerSeed(String nombre, Integer mililitros, String descripcion, String fallbackImage, Integer orden) {}
}
