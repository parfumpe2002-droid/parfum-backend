package com.parfum.controller;

import com.parfum.dto.ComboDtos.ComboRequest;
import com.parfum.dto.ComboDtos.ComboResponse;
import com.parfum.service.ComboService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ComboController {
    private final ComboService service;
    public ComboController(ComboService service) { this.service = service; }

    @GetMapping("/api/combos")
    public List<ComboResponse> publicados() { return service.publicos(); }

    @GetMapping("/api/admin/combos")
    public List<ComboResponse> administrar() { return service.administrar(); }

    @PostMapping("/api/admin/combos")
    @ResponseStatus(HttpStatus.CREATED)
    public ComboResponse crear(@Valid @RequestBody ComboRequest request) {
        return service.guardar(null, request);
    }

    @PutMapping("/api/admin/combos/{id}")
    public ComboResponse modificar(@PathVariable Long id, @Valid @RequestBody ComboRequest request) {
        return service.guardar(id, request);
    }

    @DeleteMapping("/api/admin/combos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
