package com.parfum.controller;

import com.parfum.dto.CommerceDtos.ContactoEstadoRequest;
import com.parfum.dto.CommerceDtos.ContactoRequest;
import com.parfum.mongo.document.Contacto;
import com.parfum.mongo.repository.ContactoRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/contactos")
public class ContactoController {
    private final ContactoRepository repository;
    public ContactoController(ContactoRepository repository) { this.repository = repository; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Contacto create(@Valid @RequestBody ContactoRequest request) {
        Contacto c = new Contacto(); c.setNombre(request.nombre().trim()); c.setCorreo(request.correo().trim().toLowerCase());
        c.setAsunto(request.asunto().trim()); c.setMensaje(request.mensaje().trim()); return repository.save(c);
    }

    @GetMapping
    public List<Contacto> list() { return repository.findAllByOrderByCreadoEnDesc(); }

    @PatchMapping("/{id}/estado")
    public Contacto status(@PathVariable String id, @Valid @RequestBody ContactoEstadoRequest request) {
        Contacto c = repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensaje no encontrado"));
        c.setEstado(request.estado().trim().toUpperCase()); return repository.save(c);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensaje no encontrado");
        }
        repository.deleteById(id);
    }

    @DeleteMapping
    public Map<String, Object> deleteMany(
            @RequestParam(defaultValue = "false") boolean all,
            @RequestParam(required = false) String estado) {
        long deleted;
        if (all) {
            deleted = repository.count();
            repository.deleteAll();
        } else if (estado != null && !estado.isBlank()) {
            deleted = repository.deleteByEstado(estado.trim().toUpperCase(Locale.ROOT));
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Indica all=true o un estado para eliminar mensajes"
            );
        }
        return Map.of("eliminados", deleted);
    }

}
