package com.parfum.service;

import com.parfum.dto.ProductoDtos.PresentacionRequest;
import com.parfum.dto.ProductoDtos.PresentacionResponse;
import com.parfum.dto.ProductoDtos.ProductoDecantRequest;
import com.parfum.dto.ProductoDtos.ProductoDecantResponse;
import com.parfum.dto.ProductoDtos.ProductoRequest;
import com.parfum.dto.ProductoDtos.ProductoResponse;
import com.parfum.jpa.entity.DecantEnvase;
import com.parfum.jpa.entity.Producto;
import com.parfum.jpa.entity.ProductoDecant;
import com.parfum.jpa.entity.ProductoPresentacion;
import com.parfum.jpa.repository.DecantEnvaseRepository;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProductoMapper {
    private final DecantEnvaseRepository envases;

    public ProductoMapper(DecantEnvaseRepository envases) {
        this.envases = envases;
    }

    public ProductoResponse toResponse(Producto p) {
        List<PresentacionResponse> presentaciones = p.getPresentaciones().stream()
                .sorted(Comparator.comparing(ProductoPresentacion::getOrdenVisual)
                        .thenComparing(ProductoPresentacion::getMililitros))
                .map(item -> new PresentacionResponse(
                        item.getId(), item.getMililitros(), item.getEtiqueta(), item.getPrecio(),
                        item.getStock(), item.isActivo(), item.getOrdenVisual()))
                .toList();

        List<ProductoDecantResponse> decants = p.getDecants().stream()
                .filter(item -> item.getEnvase() != null)
                .sorted(Comparator.comparing(ProductoDecant::getOrdenVisual)
                        .thenComparing(item -> item.getEnvase().getMililitros())
                        .thenComparing(ProductoDecant::getId, Comparator.nullsLast(Long::compareTo)))
                .map(this::toDecantResponse)
                .toList();

        return new ProductoResponse(
                p.getId(), p.getSku(), p.getSlug(), p.getNombre(), p.getMarca(), p.getCategoria(),
                p.getGenero(), p.getConcentracion(), p.getDescripcion(), p.getFamiliaOlfativa(),
                p.getAnoLanzamiento(), p.getPerfumista(), p.getNotasSalida(), p.getNotasCorazon(),
                p.getNotasFondo(), p.getAcordesPrincipales(), p.getDuracion(), p.getProyeccion(),
                p.getEstaciones(), p.getOcasiones(), p.getEstilo(), p.getFuenteReferencia(),
                p.getPrecio(), p.getStock(), p.getImagenUrl(), p.getImagenPublicId(),
                p.getFallbackImage(), p.isDestacado(), p.isActivo(), p.isDecantDisponible(),
                presentaciones, decants, p.getCreadoEn(), p.getActualizadoEn());
    }

    private ProductoDecantResponse toDecantResponse(ProductoDecant item) {
        DecantEnvase envase = item.getEnvase();
        return new ProductoDecantResponse(
                item.getId(), envase.getId(), envase.getNombre(), envase.getMililitros(),
                envase.getMililitros() + " ml · " + envase.getNombre(),
                envase.getImagenUrl(), envase.getImagenPublicId(), envase.getFallbackImage(),
                item.getPrecio(), item.getStock(), item.isActivo() && envase.isActivo(), item.getOrdenVisual());
    }

    public void apply(Producto p, ProductoRequest r) {
        String nombre = r.nombre().trim();
        String marca = r.marca().trim();
        p.setNombre(nombre);
        p.setMarca(marca);
        p.setCategoria(r.categoria().trim());
        p.setGenero(r.genero().trim());
        p.setConcentracion(clean(r.concentracion()));
        p.setDescripcion(clean(r.descripcion()));
        p.setFamiliaOlfativa(clean(r.familiaOlfativa()));
        p.setAnoLanzamiento(r.anoLanzamiento());
        p.setPerfumista(clean(r.perfumista()));
        p.setNotasSalida(clean(r.notasSalida()));
        p.setNotasCorazon(clean(r.notasCorazon()));
        p.setNotasFondo(clean(r.notasFondo()));
        p.setAcordesPrincipales(clean(r.acordesPrincipales()));
        p.setDuracion(clean(r.duracion()));
        p.setProyeccion(clean(r.proyeccion()));
        p.setEstaciones(clean(r.estaciones()));
        p.setOcasiones(clean(r.ocasiones()));
        p.setEstilo(clean(r.estilo()));
        p.setFuenteReferencia(clean(r.fuenteReferencia()));
        p.setImagenUrl(clean(r.imagenUrl()));
        p.setImagenPublicId(clean(r.imagenPublicId()));
        p.setFallbackImage(clean(r.fallbackImage()));
        p.setDestacado(r.destacado());
        p.setActivo(r.activo());
        p.setDecantDisponible(r.decantDisponible());

        String requestedSku = clean(r.sku());
        if (requestedSku != null) {
            p.setSku(requestedSku.toUpperCase(Locale.ROOT));
        } else if (p.getSku() == null || p.getSku().isBlank()) {
            String hash = Integer.toUnsignedString((nombre + "|" + marca).toLowerCase(Locale.ROOT).hashCode(), 36);
            p.setSku(("PAR-" + hash).toUpperCase(Locale.ROOT));
        }

        String requestedSlug = clean(r.slug());
        if (requestedSlug != null) {
            p.setSlug(slugify(requestedSlug));
        } else if (p.getSlug() == null || p.getSlug().isBlank()) {
            p.setSlug(slugify(nombre));
        }

        if (r.presentaciones() != null) {
            syncPresentaciones(p, r.presentaciones());
            p.recalcularResumenComercial();
        } else {
            p.setPrecio(r.precio() == null ? BigDecimal.ZERO : r.precio());
            p.setStock(r.stock() == null ? 0 : r.stock());
        }

        if (r.decants() != null) syncDecants(p, r.decants());
    }

    private void syncPresentaciones(Producto producto, List<PresentacionRequest> requests) {
        Set<Integer> mililitrosUnicos = new HashSet<>();
        List<ProductoPresentacion> result = new ArrayList<>();

        for (int index = 0; index < requests.size(); index++) {
            PresentacionRequest request = requests.get(index);
            if (!mililitrosUnicos.add(request.mililitros())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No puedes repetir la presentación de " + request.mililitros() + " ml");
            }

            ProductoPresentacion presentacion = findExisting(producto, request);
            if (presentacion == null) presentacion = new ProductoPresentacion();
            presentacion.setProducto(producto);
            presentacion.setMililitros(request.mililitros());
            presentacion.setPrecio(request.precio() == null ? BigDecimal.ZERO : request.precio());
            presentacion.setStock(request.stock() == null ? 0 : request.stock());
            presentacion.setActivo(request.activo());
            presentacion.setOrdenVisual(request.ordenVisual() == null ? index : request.ordenVisual());
            result.add(presentacion);
        }

        producto.getPresentaciones().removeIf(existing -> result.stream().noneMatch(item -> item == existing));
        for (ProductoPresentacion item : result) {
            if (!producto.getPresentaciones().contains(item)) producto.agregarPresentacion(item);
        }
    }

    private void syncDecants(Producto producto, List<ProductoDecantRequest> requests) {
        Set<Long> envasesUnicos = new HashSet<>();
        List<ProductoDecant> result = new ArrayList<>();

        for (int index = 0; index < requests.size(); index++) {
            ProductoDecantRequest request = requests.get(index);
            if (!envasesUnicos.add(request.envaseId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No puedes repetir el mismo envase de decant");
            }
            DecantEnvase envase = envases.findById(request.envaseId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Envase de decant no encontrado"));
            ProductoDecant decant = findExistingDecant(producto, request);
            if (decant == null) decant = new ProductoDecant();
            decant.setProducto(producto);
            decant.setEnvase(envase);
            decant.setPrecio(request.precio() == null ? BigDecimal.ZERO : request.precio());
            decant.setStock(request.stock() == null ? 0 : request.stock());
            decant.setActivo(request.activo());
            decant.setOrdenVisual(request.ordenVisual() == null ? index : request.ordenVisual());
            result.add(decant);
        }

        producto.getDecants().removeIf(existing -> result.stream().noneMatch(item -> item == existing));
        for (ProductoDecant item : result) {
            if (!producto.getDecants().contains(item)) producto.agregarDecant(item);
        }
    }

    private ProductoPresentacion findExisting(Producto producto, PresentacionRequest request) {
        if (request.id() != null) {
            ProductoPresentacion byId = producto.getPresentaciones().stream()
                    .filter(item -> request.id().equals(item.getId()))
                    .findFirst().orElse(null);
            if (byId != null) return byId;
        }
        return producto.getPresentaciones().stream()
                .filter(item -> request.mililitros().equals(item.getMililitros()))
                .findFirst().orElse(null);
    }

    private ProductoDecant findExistingDecant(Producto producto, ProductoDecantRequest request) {
        if (request.id() != null) {
            ProductoDecant byId = producto.getDecants().stream()
                    .filter(item -> request.id().equals(item.getId()))
                    .findFirst().orElse(null);
            if (byId != null) return byId;
        }
        return producto.getDecants().stream()
                .filter(item -> item.getEnvase() != null && request.envaseId().equals(item.getEnvase().getId()))
                .findFirst().orElse(null);
    }

    public static String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "producto" : normalized;
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
