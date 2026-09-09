package com.parfum.controller;

import com.parfum.jpa.entity.Producto;
import com.parfum.jpa.repository.ProductoRepository;
import com.parfum.mongo.document.HistorialVisita;
import com.parfum.mongo.repository.HistorialRepository;
import com.parfum.security.AuthenticatedUser;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/historial")
public class HistorialController {
    private final HistorialRepository repository;
    private final ProductoRepository productoRepository;

    public HistorialController(HistorialRepository repository, ProductoRepository productoRepository) {
        this.repository = repository; this.productoRepository = productoRepository;
    }

    @GetMapping("/me")
    public List<HistorialVisita> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return repository.findTop30ByUsuarioIdOrderByVistoEnDesc(user.id());
    }

    @PostMapping("/{productoId}")
    public HistorialVisita add(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long productoId) {
        Producto p = productoRepository.findById(productoId).filter(Producto::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        HistorialVisita h = new HistorialVisita(); h.setUsuarioId(user.id()); h.setProductoId(p.getId()); h.setNombre(p.getNombre());
        h.setMarca(p.getMarca()); h.setImagenUrl(p.getImagenUrl() != null ? p.getImagenUrl() : p.getFallbackImage());
        return repository.save(h);
    }

    @DeleteMapping("/me")
    public Map<String, String> clear(@AuthenticationPrincipal AuthenticatedUser user) {
        repository.deleteByUsuarioId(user.id()); return Map.of("message", "Historial eliminado");
    }
}
