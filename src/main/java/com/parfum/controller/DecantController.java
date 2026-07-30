package com.parfum.controller;

import com.parfum.dto.DecantDtos.EnvaseRequest;
import com.parfum.dto.DecantDtos.EnvaseResponse;
import com.parfum.dto.ProductoDtos.ProductoResponse;
import com.parfum.jpa.entity.DecantEnvase;
import com.parfum.jpa.entity.Producto;
import com.parfum.jpa.entity.ProductoDecant;
import com.parfum.jpa.repository.DecantEnvaseRepository;
import com.parfum.jpa.repository.ProductoRepository;
import com.parfum.service.ProductoMapper;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class DecantController {
    private final DecantEnvaseRepository envases;
    private final ProductoRepository productos;
    private final ProductoMapper productoMapper;

    public DecantController(DecantEnvaseRepository envases,
                            ProductoRepository productos,
                            ProductoMapper productoMapper) {
        this.envases = envases;
        this.productos = productos;
        this.productoMapper = productoMapper;
    }

    @GetMapping("/api/decants/envases")
    public List<EnvaseResponse> publicContainers() {
        return envases.findByActivoTrueOrderByMililitrosAscOrdenVisualAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/api/decants/regalos-arabes")
    public List<ProductoResponse> arabicGiftOptions() {
        return productos.findAll().stream()
                .filter(Producto::isActivo)
                .filter(Producto::isDecantDisponible)
                .filter(product -> normalize(product.getCategoria()).contains("arabe"))
                .filter(product -> product.getDecants().stream()
                        .filter(ProductoDecant::isActivo)
                        .filter(item -> item.getEnvase() != null && item.getEnvase().isActivo())
                        .anyMatch(item -> Integer.valueOf(3).equals(item.getEnvase().getMililitros())
                                && item.getStock() != null && item.getStock() > 0))
                .sorted(Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(productoMapper::toResponse)
                .toList();
    }

    @GetMapping("/api/admin/decants/envases")
    public List<EnvaseResponse> adminContainers() {
        return envases.findAll().stream()
                .sorted(Comparator.comparing(DecantEnvase::getMililitros)
                        .thenComparing(DecantEnvase::getOrdenVisual)
                        .thenComparing(DecantEnvase::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/api/admin/decants/envases")
    @ResponseStatus(HttpStatus.CREATED)
    public EnvaseResponse create(@Valid @RequestBody EnvaseRequest request) {
        DecantEnvase envase = new DecantEnvase();
        apply(envase, request);
        return toResponse(envases.save(envase));
    }

    @PutMapping("/api/admin/decants/envases/{id}")
    public EnvaseResponse update(@PathVariable Long id,
                                 @Valid @RequestBody EnvaseRequest request) {
        DecantEnvase envase = envases.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Envase de decant no encontrado"));
        apply(envase, request);
        return toResponse(envases.save(envase));
    }

    @DeleteMapping("/api/admin/decants/envases/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        DecantEnvase envase = envases.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Envase de decant no encontrado"));
        boolean inUse = productos.findAll().stream()
                .flatMap(product -> product.getDecants().stream())
                .anyMatch(item -> item.getEnvase() != null && id.equals(item.getEnvase().getId()));
        if (inUse) {
            envase.setActivo(false);
            envases.save(envase);
        } else {
            envases.delete(envase);
        }
    }

    private void apply(DecantEnvase envase, EnvaseRequest request) {
        envase.setNombre(request.nombre().trim());
        envase.setMililitros(request.mililitros());
        envase.setDescripcion(clean(request.descripcion()));
        envase.setImagenUrl(clean(request.imagenUrl()));
        envase.setImagenPublicId(clean(request.imagenPublicId()));
        envase.setFallbackImage(clean(request.fallbackImage()));
        envase.setActivo(request.activo());
        envase.setOrdenVisual(request.ordenVisual() == null ? 0 : request.ordenVisual());
    }

    private EnvaseResponse toResponse(DecantEnvase envase) {
        return new EnvaseResponse(
                envase.getId(), envase.getNombre(), envase.getMililitros(), envase.getDescripcion(),
                envase.getImagenUrl(), envase.getImagenPublicId(), envase.getFallbackImage(),
                envase.isActivo(), envase.getOrdenVisual(), envase.getCreadoEn(), envase.getActualizadoEn());
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalize(String value) {
        if (value == null) return "";
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
