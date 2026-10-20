package com.parfum.service;

import com.parfum.dto.ComboDtos.ComboRequest;
import com.parfum.dto.ComboDtos.ComboResponse;
import com.parfum.dto.ComboDtos.ComponenteRequest;
import com.parfum.dto.ComboDtos.ComponenteResponse;
import com.parfum.jpa.entity.Combo;
import com.parfum.jpa.entity.ComboComponente;
import com.parfum.jpa.entity.Producto;
import com.parfum.jpa.entity.ProductoDecant;
import com.parfum.jpa.entity.ProductoPresentacion;
import com.parfum.jpa.repository.ComboRepository;
import com.parfum.jpa.repository.ProductoRepository;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ComboService {
    private final ComboRepository combos;
    private final ProductoRepository productos;

    public ComboService(ComboRepository combos, ProductoRepository productos) {
        this.combos = combos;
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public List<ComboResponse> publicos() {
        return combos.findByPublicadoTrueOrderByActualizadoEnDesc().stream()
                .map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public List<ComboResponse> administrar() {
        return combos.findAllByOrderByActualizadoEnDesc().stream()
                .map(this::response).toList();
    }

    @Transactional
    public ComboResponse guardar(Long id, ComboRequest request) {
        Combo combo = id == null ? new Combo() : combos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Combo no encontrado"));

        String image = clean(request.imagenUrl());
        if (image != null) {
            try {
                URI uri = URI.create(image);
                if (!"https".equalsIgnoreCase(uri.getScheme())
                        || !"res.cloudinary.com".equalsIgnoreCase(uri.getHost())) {
                    throw new IllegalArgumentException("La imagen debe estar alojada en Cloudinary");
                }
            } catch (IllegalArgumentException error) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usa una imagen válida de Cloudinary");
            }
        }

        List<ComboComponente> componentes = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ComponenteRequest item : request.componentes()) {
            String type = item.tipoItem().trim().toUpperCase(Locale.ROOT);
            if (!"DECANT".equals(type) && !"BOTELLA".equals(type)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona decant o botella");
            }
            ComboComponente current = new ComboComponente(item.productoId(), item.varianteId(),
                    type, item.cantidad());
            ComponenteResponse resolved = resolve(current);
            if (resolved == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La presentación seleccionada no existe o no está activa");
            }
            String key = item.productoId() + ":" + type + ":" + item.varianteId();
            if (!seen.add(key)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No repitas la misma presentación: ajusta la cantidad");
            }
            if (request.publicado() && resolved.stock() < item.cantidad()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Sin stock suficiente para publicar: " + resolved.nombre());
            }
            componentes.add(current);
        }
        combo.setNombre(request.nombre().trim());
        combo.setDescripcion(clean(request.descripcion()));
        combo.setPrecio(request.precio());
        combo.setImagenUrl(image);
        combo.setPublicado(request.publicado());
        combo.setComponentes(componentes);
        return response(combos.saveAndFlush(combo));
    }

    @Transactional
    public void eliminar(Long id) {
        Combo combo = combos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Combo no encontrado"));
        // No toca productos ni sus existencias.
        combos.delete(combo);
    }

    private ComponenteResponse resolve(ComboComponente line) {
        Producto p = productos.findById(line.getProductoId()).orElse(null);
        if (p == null || !p.isActivo()) return null;
        String type = line.getTipoItem();
        BigDecimal price;
        Integer stock;
        String label;
        Long variant = line.getVarianteId();

        if ("DECANT".equals(type)) {
            if (!p.isDecantDisponible() || variant == null) return null;
            ProductoDecant decant = p.getDecants().stream()
                    .filter(x -> x.isActivo() && x.getEnvase().isActivo() && variant.equals(x.getId()))
                    .findFirst().orElse(null);
            if (decant == null) return null;
            price = decant.getPrecio();
            stock = decant.getStock();
            label = decant.getEnvase().getMililitros() + " ml · " + decant.getEnvase().getNombre();
        } else if ("BOTELLA".equals(type)) {
            if (!p.getPresentaciones().isEmpty()) {
                if (variant == null) return null;
                ProductoPresentacion presentation = p.getPresentaciones().stream()
                        .filter(x -> x.isActivo() && variant.equals(x.getId()))
                        .findFirst().orElse(null);
                if (presentation == null) return null;
                price = presentation.getPrecio();
                stock = presentation.getStock();
                label = presentation.getEtiqueta();
            } else {
                if (variant != null) return null;
                price = p.getPrecio();
                stock = p.getStock();
                label = "Frasco completo";
            }
        } else return null;

        if (price == null || price.signum() <= 0) return null;
        return new ComponenteResponse(p.getId(), variant, type, line.getCantidad(),
                p.getNombre(), p.getMarca(), label,
                p.getImagenUrl() != null ? p.getImagenUrl() : p.getFallbackImage(),
                price, stock == null ? 0 : Math.max(0, stock));
    }

    private ComboResponse response(Combo combo) {
        List<ComponenteResponse> parts = new ArrayList<>();
        Map<String, Integer> demand = new HashMap<>();
        Map<String, Integer> available = new HashMap<>();
        boolean valid = !combo.getComponentes().isEmpty();
        for (ComboComponente line : combo.getComponentes()) {
            ComponenteResponse current = resolve(line);
            if (current == null) {
                valid = false;
                parts.add(new ComponenteResponse(line.getProductoId(), line.getVarianteId(),
                        line.getTipoItem(), line.getCantidad(), "Producto no disponible", "", "No disponible",
                        null, BigDecimal.ZERO, 0));
                continue;
            }
            parts.add(current);
            String key = current.productoId() + ":" + current.tipoItem() + ":" + current.varianteId();
            demand.merge(key, current.cantidad(), Integer::sum);
            available.put(key, current.stock());
        }
        int stock = demand.isEmpty() ? 0 : demand.entrySet().stream()
                .mapToInt(entry -> available.get(entry.getKey()) / entry.getValue())
                .min().orElse(0);
        if (!valid) stock = 0;
        return new ComboResponse(combo.getId(), combo.getNombre(), combo.getDescripcion(),
                combo.getPrecio(), combo.getImagenUrl(), combo.isPublicado(),
                combo.isPublicado() && stock > 0, stock, parts);
    }

    private static String clean(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
