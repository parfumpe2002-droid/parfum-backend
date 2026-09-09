package com.parfum.controller;

import com.parfum.dto.CommerceDtos.FavoritoRequest;
import com.parfum.jpa.entity.DecantEnvase;
import com.parfum.jpa.entity.Producto;
import com.parfum.jpa.entity.ProductoDecant;
import com.parfum.jpa.entity.ProductoPresentacion;
import com.parfum.jpa.repository.ProductoRepository;
import com.parfum.mongo.document.Favorito;
import com.parfum.mongo.repository.FavoritoRepository;
import com.parfum.security.AuthenticatedUser;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {
    private final FavoritoRepository repository;
    private final ProductoRepository productoRepository;

    public FavoritoController(FavoritoRepository repository, ProductoRepository productoRepository) {
        this.repository = repository;
        this.productoRepository = productoRepository;
    }

    @GetMapping("/me")
    public List<Favorito> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return repository.findByUsuarioIdOrderByCreadoEnDesc(user.id());
    }

    @PostMapping("/{productoId}")
    public Favorito add(@AuthenticationPrincipal AuthenticatedUser user,
                        @PathVariable Long productoId,
                        @RequestBody(required = false) FavoritoRequest request) {
        Producto product = findProduct(productoId);
        Selection selection = resolveSelection(product, request);
        return repository.findByUsuarioIdAndVarianteClave(user.id(), selection.variantKey())
                .orElseGet(() -> {
                    Favorito favorite = new Favorito();
                    favorite.setUsuarioId(user.id());
                    favorite.setProductoId(product.getId());
                    favorite.setPresentacionId(selection.presentationId());
                    favorite.setProductoDecantId(selection.productDecantId());
                    favorite.setTipoItem(selection.type());
                    favorite.setVarianteClave(selection.variantKey());
                    favorite.setMililitros(selection.milliliters());
                    favorite.setPresentacion(selection.label());
                    favorite.setNombre(product.getNombre());
                    favorite.setMarca(product.getMarca());
                    favorite.setPrecio(selection.price().doubleValue());
                    favorite.setStock(selection.stock());
                    favorite.setImagenUrl(selection.imageUrl());
                    return repository.save(favorite);
                });
    }

    @DeleteMapping("/{productoId}")
    public Map<String, String> remove(@AuthenticationPrincipal AuthenticatedUser user,
                                      @PathVariable Long productoId,
                                      @RequestParam(required = false) Long presentacionId,
                                      @RequestParam(required = false) Long productoDecantId,
                                      @RequestParam(required = false) String tipoItem) {
        String type = normalizeType(tipoItem, productoDecantId);
        repository.deleteByUsuarioIdAndVarianteClave(
                user.id(), variantKey(productoId, type, presentacionId, productoDecantId));
        return Map.of("message", "Eliminado de favoritos");
    }

    private Producto findProduct(Long id) {
        return productoRepository.findById(id).filter(Producto::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    private Selection resolveSelection(Producto product, FavoritoRequest request) {
        Long decantId = request == null ? null : request.productoDecantId();
        Long presentationId = request == null ? null : request.presentacionId();
        String type = normalizeType(request == null ? null : request.tipoItem(), decantId);
        if ("DECANT".equals(type)) {
            ProductoDecant decant = product.getDecants().stream()
                    .filter(ProductoDecant::isActivo)
                    .filter(item -> item.getEnvase() != null && item.getEnvase().isActivo())
                    .filter(item -> decantId != null && decantId.equals(item.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Decant no disponible"));
            DecantEnvase envase = decant.getEnvase();
            return new Selection("DECANT", null, decant.getId(), envase.getMililitros(),
                    "DECANT · " + envase.getMililitros() + " ml · " + envase.getNombre(),
                    safePrice(decant.getPrecio()), decant.getStock(),
                    envase.getImagenUrl() != null ? envase.getImagenUrl() : envase.getFallbackImage(),
                    variantKey(product.getId(), "DECANT", null, decant.getId()));
        }

        ProductoPresentacion presentation = null;
        if (presentationId != null) {
            presentation = product.getPresentaciones().stream()
                    .filter(ProductoPresentacion::isActivo)
                    .filter(item -> presentationId.equals(item.getId()))
                    .findFirst().orElse(null);
        }
        if (presentation == null) {
            presentation = product.getPresentaciones().stream()
                    .filter(ProductoPresentacion::isActivo)
                    .sorted(Comparator.comparing(ProductoPresentacion::getOrdenVisual)
                            .thenComparing(ProductoPresentacion::getMililitros))
                    .findFirst().orElse(null);
        }
        Long resolvedId = presentation == null ? null : presentation.getId();
        return new Selection("BOTELLA", resolvedId, null,
                presentation == null ? null : presentation.getMililitros(),
                presentation == null ? "Presentación estándar" : presentation.getEtiqueta(),
                safePrice(presentation == null ? product.getPrecio() : presentation.getPrecio()),
                presentation == null ? product.getStock() : presentation.getStock(),
                product.getImagenUrl() != null ? product.getImagenUrl() : product.getFallbackImage(),
                variantKey(product.getId(), "BOTELLA", resolvedId, null));
    }

    private BigDecimal safePrice(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
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

    private record Selection(String type, Long presentationId, Long productDecantId,
                             Integer milliliters, String label, BigDecimal price, Integer stock,
                             String imageUrl, String variantKey) {}
}
