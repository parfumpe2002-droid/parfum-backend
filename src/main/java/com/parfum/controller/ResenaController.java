package com.parfum.controller;

import com.parfum.dto.CommerceDtos.ResenaRequest;
import com.parfum.mongo.document.Resena;
import com.parfum.mongo.repository.ResenaRepository;
import com.parfum.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/resenas")
public class ResenaController {
    private final ResenaRepository repository;
    public ResenaController(ResenaRepository repository) { this.repository = repository; }

    @GetMapping("/producto/{productoId}")
    public List<Resena> productReviews(@PathVariable Long productoId) {
        return repository.findByProductoIdOrderByCreadoEnDesc(productoId);
    }

    @PostMapping("/producto/{productoId}")
    public Resena save(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long productoId,
                       @Valid @RequestBody ResenaRequest request) {
        Resena r = repository.findByUsuarioIdAndProductoId(user.id(), productoId).orElseGet(Resena::new);
        r.setUsuarioId(user.id()); r.setNombreUsuario(user.nombre()); r.setProductoId(productoId);
        r.setPuntuacion(request.puntuacion()); r.setComentario(request.comentario().trim()); r.setCreadoEn(Instant.now());
        return repository.save(r);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id) {
        Resena r = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reseña no encontrada"));
        if (!r.getUsuarioId().equals(user.id()) && !"ADMIN".equals(user.rol())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes eliminar esta reseña");
        repository.delete(r); return Map.of("message", "Reseña eliminada");
    }
}
