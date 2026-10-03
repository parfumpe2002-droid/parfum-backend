package com.parfum.controller;

import com.parfum.mongo.document.Reclamo;
import com.parfum.mongo.repository.ReclamoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/reclamos")
public class ReclamoController {
    private final ReclamoRepository repository;

    public ReclamoController(ReclamoRepository repository) {
        this.repository = repository;
    }

    public record ReclamoRequest(
            @NotBlank @Pattern(regexp = "RECLAMO|QUEJA") String tipo,
            @NotBlank @Size(max = 120) String nombre,
            @NotBlank @Size(max = 20) String documento,
            @NotBlank @Email @Size(max = 160) String correo,
            @NotBlank @Size(max = 30) String telefono,
            @NotBlank @Size(max = 3000) String detalle,
            @Size(max = 80) String pedidoReferencia,
            @NotBlank @Size(max = 1500) String pedidoConsumidor) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@Valid @RequestBody ReclamoRequest request) {
        Reclamo r = new Reclamo();
        r.setTipo(request.tipo().trim().toUpperCase(Locale.ROOT));
        r.setNombre(request.nombre().trim());
        r.setDocumento(request.documento().trim());
        r.setCorreo(request.correo().trim().toLowerCase(Locale.ROOT));
        r.setTelefono(request.telefono().trim());
        r.setDetalle(request.detalle().trim());
        r.setPedidoReferencia(clean(request.pedidoReferencia()));
        r.setPedidoConsumidor(request.pedidoConsumidor().trim());
        r = repository.save(r);
        return Map.of(
                "id", r.getId(),
                "estado", r.getEstado(),
                "message", "Tu reclamo o queja fue registrado correctamente."
        );
    }

    @GetMapping
    public List<Reclamo> list() {
        return repository.findAllByOrderByCreadoEnDesc();
    }

    @PatchMapping("/{id}/estado")
    public Reclamo status(@PathVariable String id, @RequestBody Map<String, String> body) {
        Reclamo r = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro no encontrado"));
        String estado = clean(body.get("estado"));
        if (estado == null || !List.of("RECIBIDO", "EN_PROCESO", "RESPONDIDO", "CERRADO").contains(estado.toUpperCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
        }
        r.setEstado(estado.toUpperCase(Locale.ROOT));
        return repository.save(r);
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
