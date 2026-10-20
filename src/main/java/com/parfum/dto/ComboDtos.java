package com.parfum.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public final class ComboDtos {
    private ComboDtos() {}

    public record ComponenteRequest(
        @NotNull Long productoId,
        Long varianteId,
        @NotBlank String tipoItem,
        @NotNull @Min(1) @Max(20) Integer cantidad
    ) {}

    public record ComboRequest(
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 1200) String descripcion,
        @NotNull @DecimalMin(value = "0.01") BigDecimal precio,
        @Size(max = 600) String imagenUrl,
        boolean publicado,
        @NotNull @Size(min = 2, max = 12) List<@Valid ComponenteRequest> componentes
    ) {}

    public record ComponenteResponse(
        Long productoId, Long varianteId, String tipoItem, Integer cantidad,
        String nombre, String marca, String presentacion, String imagenUrl,
        BigDecimal precioIndividual, Integer stock
    ) {}

    public record ComboResponse(
        Long id, String nombre, String descripcion, BigDecimal precio, String imagenUrl,
        boolean publicado, boolean disponible, int stockEstimado,
        List<ComponenteResponse> componentes
    ) {}
}
