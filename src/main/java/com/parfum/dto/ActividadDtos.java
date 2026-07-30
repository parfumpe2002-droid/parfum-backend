package com.parfum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class ActividadDtos {
    private ActividadDtos() {}

    public record ActividadRequest(
            @NotBlank @Size(max = 40) String tipo,
            @Size(max = 80) String pagina,
            @Size(max = 300) String ruta,
            @Size(max = 80) String sessionId,
            Long productoId,
            @Size(max = 140) String productoNombre,
            @Size(max = 500) String detalle,
            @Size(max = 40) String dispositivo) {}

    public record ActividadResponse(
            String id,
            String tipo,
            String pagina,
            String ruta,
            String sessionId,
            Long usuarioId,
            String usuarioEmail,
            Long productoId,
            String productoNombre,
            String detalle,
            String dispositivo,
            Instant creadoEn) {}
}
