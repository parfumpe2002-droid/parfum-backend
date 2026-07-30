package com.parfum.controller;

import com.parfum.dto.ProductoDtos.ProductoRequest;
import com.parfum.dto.ProductoDtos.ProductoResponse;
import com.parfum.jpa.entity.Producto;
import com.parfum.jpa.repository.ProductoRepository;
import com.parfum.service.ProductoMapper;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoRepository repository;
    private final ProductoMapper mapper;

    public ProductoController(ProductoRepository repository, ProductoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<ProductoResponse> list(@RequestParam(required = false) String q,
                                       @RequestParam(required = false) String categoria,
                                       @RequestParam(required = false) String marca,
                                       @RequestParam(required = false) BigDecimal minPrecio,
                                       @RequestParam(required = false) BigDecimal maxPrecio,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "16") int size,
                                       @RequestParam(defaultValue = "destacado,desc") String sort) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        String[] parts = sort.split(",", 2);
        String property = switch (parts[0]) {
            case "precio", "nombre", "marca", "creadoEn", "actualizadoEn", "destacado", "sku" -> parts[0];
            default -> "destacado";
        };
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        var pageable = PageRequest.of(
                Math.max(page, 0),
                safeSize,
                Sort.by(direction, property).and(Sort.by(Sort.Direction.DESC, "actualizadoEn")));
        return repository.buscar(
                normalizeFilter(q),
                normalizeFilter(categoria),
                normalizeFilter(marca),
                minPrecio,
                maxPrecio,
                pageable
        ).map(mapper::toResponse);
    }

    @GetMapping("/destacados")
    public List<ProductoResponse> featured() {
        return repository.findTop15ByActivoTrueAndDestacadoTrueOrderByActualizadoEnDesc()
                .stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ProductoResponse get(@PathVariable Long id) {
        return mapper.toResponse(active(repository.findById(id)
                .orElseThrow(() -> notFound())));
    }

    @GetMapping("/slug/{slug}")
    public ProductoResponse getBySlug(@PathVariable String slug) {
        return mapper.toResponse(active(repository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> notFound())));
    }

    @GetMapping("/sku/{sku}")
    public ProductoResponse getBySku(@PathVariable String sku) {
        return mapper.toResponse(active(repository.findBySkuIgnoreCase(sku)
                .orElseThrow(() -> notFound())));
    }

    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse create(@Valid @RequestBody ProductoRequest request) {
        Producto product = new Producto();
        mapper.apply(product, request);
        ensureUnique(product);
        return mapper.toResponse(repository.save(product));
    }

    @PutMapping("/{id}")
    @Transactional
    public ProductoResponse update(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        Producto product = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        mapper.apply(product, request);
        ensureUnique(product);
        return mapper.toResponse(repository.save(product));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Producto product = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        product.setActivo(false);
        repository.save(product);
    }

    private Producto active(Producto product) {
        if (!product.isActivo()) throw notFound();
        return product;
    }

    private void ensureUnique(Producto product) {
        repository.findBySkuIgnoreCase(product.getSku())
                .filter(existing -> !existing.getId().equals(product.getId()))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese SKU");
                });
        repository.findBySlugIgnoreCase(product.getSlug())
                .filter(existing -> !existing.getId().equals(product.getId()))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con ese slug");
                });
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
    }

    private static String normalizeFilter(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
