package com.parfum.controller;

import com.parfum.jpa.repository.ProductoRepository;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final ProductoRepository productos;

    public HealthController(ProductoRepository productos) {
        this.productos = productos;
    }

    @GetMapping
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "parfum-backend", "version", "1.1.1", "time", Instant.now());
    }

    @GetMapping("/catalogo")
    public Map<String, Object> catalogo() {
        long activos = productos.countByActivoTrue();
        long total = productos.count();
        return Map.of(
                "status", total >= 86 ? "READY" : "SYNCING",
                "productosTotal", total,
                "productosActivos", activos,
                "minimosEsperados", 86,
                "time", Instant.now());
    }
}
