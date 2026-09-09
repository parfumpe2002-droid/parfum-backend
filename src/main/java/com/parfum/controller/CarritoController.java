package com.parfum.controller;

import com.parfum.dto.CommerceDtos.CarritoRequest;
import com.parfum.jpa.entity.DecantEnvase;
import com.parfum.jpa.entity.Producto;
import com.parfum.jpa.entity.ProductoDecant;
import com.parfum.jpa.entity.ProductoPresentacion;
import com.parfum.jpa.repository.ProductoRepository;
import com.parfum.mongo.document.CarritoItem;
import com.parfum.mongo.repository.CarritoRepository;
import com.parfum.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {
    private final CarritoRepository repository;
    private final ProductoRepository productoRepository;

    public CarritoController(CarritoRepository repository, ProductoRepository productoRepository) {
        this.repository = repository;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/me")
    public List<CarritoItem> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return repository.findByUsuarioIdOrderByActualizadoEnDesc(user.id());
    }

    @PostMapping("/{productoId}")
    public CarritoItem add(@AuthenticationPrincipal AuthenticatedUser user,
                           @PathVariable Long productoId,
                           @Valid @RequestBody CarritoRequest request) {
        Producto producto = findProduct(productoId);
        Selection selection = resolveSelection(producto, request);
        CarritoItem item = repository
                .findByUsuarioIdAndVarianteClave(user.id(), selection.variantKey())
                .orElseGet(CarritoItem::new);
        int nuevaCantidad = (item.getCantidad() == null ? 0 : item.getCantidad()) + request.cantidad();
        validateStock(selection.price(), selection.stock(), nuevaCantidad);
        fill(item, user.id(), producto, selection, nuevaCantidad);
        return repository.save(item);
    }

    @PutMapping("/{productoId}")
    public CarritoItem update(@AuthenticationPrincipal AuthenticatedUser user,
                              @PathVariable Long productoId,
                              @Valid @RequestBody CarritoRequest request) {
        Producto producto = findProduct(productoId);
        Selection selection = resolveSelection(producto, request);
        validateStock(selection.price(), selection.stock(), request.cantidad());
        CarritoItem item = repository
                .findByUsuarioIdAndVarianteClave(user.id(), selection.variantKey())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Presentación no encontrada en el carrito"));
        fill(item, user.id(), producto, selection, request.cantidad());
        return repository.save(item);
    }

    @DeleteMapping("/{productoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal AuthenticatedUser user,
                       @PathVariable Long productoId,
                       @RequestParam(required = false) Long presentacionId,
                       @RequestParam(required = false) Long productoDecantId,
                       @RequestParam(required = false) String tipoItem) {
        if (presentacionId == null && productoDecantId == null && (tipoItem == null || tipoItem.isBlank())) {
            repository.deleteByUsuarioIdAndProductoId(user.id(), productoId);
            return;
        }
        String type = normalizeType(tipoItem, productoDecantId);
        String key = variantKey(productoId, type, presentacionId, productoDecantId);
        repository.deleteByUsuarioIdAndVarianteClave(user.id(), key);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@AuthenticationPrincipal AuthenticatedUser user) {
        repository.deleteByUsuarioId(user.id());
    }

    private Producto findProduct(Long id) {
        return productoRepository.findById(id).filter(Producto::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    private Selection resolveSelection(Producto producto, CarritoRequest request) {
        String type = normalizeType(request.tipoItem(), request.productoDecantId());
        if ("DECANT".equals(type)) {
            if (!producto.isDecantDisponible()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Este perfume no está disponible en decant");
            }
            if (request.productoDecantId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una presentación de decant");
            }
            ProductoDecant decant = producto.getDecants().stream()
                    .filter(ProductoDecant::isActivo)
                    .filter(item -> item.getEnvase() != null && item.getEnvase().isActivo())
                    .filter(item -> request.productoDecantId().equals(item.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Decant no disponible"));
            DecantEnvase envase = decant.getEnvase();
            return new Selection("DECANT", null, decant.getId(), envase.getMililitros(),
                    envase.getMililitros() + " ml · " + envase.getNombre(),
                    decant.getPrecio(), decant.getStock(),
                    envase.getImagenUrl() != null ? envase.getImagenUrl() : envase.getFallbackImage(),
                    variantKey(producto.getId(), "DECANT", null, decant.getId()));
        }

        ProductoPresentacion presentacion = findPresentation(producto, request.presentacionId());
        Long presentationId = presentacion == null ? null : presentacion.getId();
        return new Selection("BOTELLA", presentationId, null,
                presentacion == null ? null : presentacion.getMililitros(),
                presentacion == null ? "Presentación estándar" : presentacion.getEtiqueta(),
                presentacion == null ? producto.getPrecio() : presentacion.getPrecio(),
                presentacion == null ? producto.getStock() : presentacion.getStock(),
                producto.getImagenUrl() != null ? producto.getImagenUrl() : producto.getFallbackImage(),
                variantKey(producto.getId(), "BOTELLA", presentationId, null));
    }

    private ProductoPresentacion findPresentation(Producto producto, Long presentationId) {
        List<ProductoPresentacion> active = producto.getPresentaciones().stream()
                .filter(ProductoPresentacion::isActivo)
                .sorted(Comparator.comparing(ProductoPresentacion::getOrdenVisual)
                        .thenComparing(ProductoPresentacion::getMililitros))
                .toList();
        if (active.isEmpty()) return null;
        if (presentationId != null) {
            return active.stream().filter(item -> presentationId.equals(item.getId())).findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Presentación no disponible"));
        }
        return active.stream()
                .filter(item -> item.getPrecio() != null && item.getPrecio().signum() > 0)
                .filter(item -> item.getStock() != null && item.getStock() > 0)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Elige una presentación con precio y stock disponibles"));
    }

    private void validateStock(BigDecimal price, Integer stock, int cantidad) {
        if (price == null || price.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta presentación todavía no tiene precio");
        }
        if (stock == null || stock < cantidad) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente para esta presentación");
        }
    }

    private void fill(CarritoItem item, Long userId, Producto producto,
                      Selection selection, int quantity) {
        item.setUsuarioId(userId);
        item.setProductoId(producto.getId());
        item.setPresentacionId(selection.presentationId());
        item.setProductoDecantId(selection.productDecantId());
        item.setTipoItem(selection.type());
        item.setVarianteClave(selection.variantKey());
        item.setMililitros(selection.milliliters());
        item.setPresentacion(selection.label());
        item.setNombre(producto.getNombre());
        item.setMarca(producto.getMarca());
        item.setPrecio(selection.price().doubleValue());
        item.setCantidad(quantity);
        item.setImagenUrl(selection.imageUrl());
        item.setActualizadoEn(Instant.now());
    }

    private String normalizeType(String raw, Long productDecantId) {
        if (productDecantId != null) return "DECANT";
        String value = raw == null ? "BOTELLA" : raw.trim().toUpperCase(Locale.ROOT);
        return "DECANT".equals(value) ? "DECANT" : "BOTELLA";
    }

    private String variantKey(Long productId, String type, Long presentationId, Long productDecantId) {
        return productId + "::" + type + "::" + ("DECANT".equals(type)
                ? String.valueOf(productDecantId)
                : String.valueOf(presentationId == null ? "standard" : presentationId));
    }

    private record Selection(
            String type,
            Long presentationId,
            Long productDecantId,
            Integer milliliters,
            String label,
            BigDecimal price,
            Integer stock,
            String imageUrl,
            String variantKey) {}
}
