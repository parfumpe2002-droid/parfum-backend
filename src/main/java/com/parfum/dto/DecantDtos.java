package com.parfum.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class DecantDtos {
    private DecantDtos() {}

    public record EnvaseRequest(
            @NotBlank @Size(max = 120) String nombre,
            @NotNull @Min(1) Integer mililitros,
            @Size(max = 400) String descripcion,
            @Size(max = 600) String imagenUrl,
            @Size(max = 300) String imagenPublicId,
            @Size(max = 300) String fallbackImage,
            boolean activo,
            @Min(0) Integer ordenVisual) {}

    public record EnvaseResponse(
            Long id,
            String nombre,
            Integer mililitros,
            String descripcion,
            String imagenUrl,
            String imagenPublicId,
            String fallbackImage,
            boolean activo,
            Integer ordenVisual,
            Instant creadoEn,
            Instant actualizadoEn) {}
}
