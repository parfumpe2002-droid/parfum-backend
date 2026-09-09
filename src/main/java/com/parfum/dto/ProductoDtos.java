package com.parfum.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class ProductoDtos {
    private ProductoDtos() {}

    public record PresentacionRequest(
            Long id,
            @NotNull @Min(1) Integer mililitros,
            @NotNull @DecimalMin("0.0") BigDecimal precio,
            @NotNull @Min(0) Integer stock,
            boolean activo,
            @Min(0) Integer ordenVisual) {}

    public record PresentacionResponse(
            Long id,
            Integer mililitros,
            String etiqueta,
            BigDecimal precio,
            Integer stock,
            boolean activo,
            Integer ordenVisual) {}

    public record ProductoDecantRequest(
            Long id,
            @NotNull Long envaseId,
            @NotNull @DecimalMin("0.0") BigDecimal precio,
            @NotNull @Min(0) Integer stock,
            boolean activo,
            @Min(0) Integer ordenVisual) {}

    public record ProductoDecantResponse(
            Long id,
            Long envaseId,
            String envaseNombre,
            Integer mililitros,
            String etiqueta,
            String imagenUrl,
            String imagenPublicId,
            String fallbackImage,
            BigDecimal precio,
            Integer stock,
            boolean activo,
            Integer ordenVisual) {}

    public record ProductoRequest(
            @Size(max = 40) String sku,
            @Size(max = 180) String slug,
            @NotBlank @Size(max = 140) String nombre,
            @NotBlank @Size(max = 100) String marca,
            @NotBlank @Size(max = 60) String categoria,
            @NotBlank @Size(max = 30) String genero,
            @Size(max = 80) String concentracion,
            @Size(max = 2000) String descripcion,
            @Size(max = 120) String familiaOlfativa,
            @Min(1800) Integer anoLanzamiento,
            @Size(max = 300) String perfumista,
            @Size(max = 1200) String notasSalida,
            @Size(max = 1200) String notasCorazon,
            @Size(max = 1200) String notasFondo,
            @Size(max = 1200) String acordesPrincipales,
            @Size(max = 160) String duracion,
            @Size(max = 220) String proyeccion,
            @Size(max = 300) String estaciones,
            @Size(max = 400) String ocasiones,
            @Size(max = 500) String estilo,
            @Size(max = 700) String fuenteReferencia,
            @DecimalMin("0.0") BigDecimal precio,
            @Min(0) Integer stock,
            @Size(max = 600) String imagenUrl,
            @Size(max = 300) String imagenPublicId,
            @Size(max = 300) String fallbackImage,
            boolean destacado,
            boolean activo,
            boolean decantDisponible,
            List<@Valid PresentacionRequest> presentaciones,
            List<@Valid ProductoDecantRequest> decants) {}

    public record ProductoResponse(
            Long id,
            String sku,
            String slug,
            String nombre,
            String marca,
            String categoria,
            String genero,
            String concentracion,
            String descripcion,
            String familiaOlfativa,
            Integer anoLanzamiento,
            String perfumista,
            String notasSalida,
            String notasCorazon,
            String notasFondo,
            String acordesPrincipales,
            String duracion,
            String proyeccion,
            String estaciones,
            String ocasiones,
            String estilo,
            String fuenteReferencia,
            BigDecimal precio,
            Integer stock,
            String imagenUrl,
            String imagenPublicId,
            String fallbackImage,
            boolean destacado,
            boolean activo,
            boolean decantDisponible,
            List<PresentacionResponse> presentaciones,
            List<ProductoDecantResponse> decants,
            Instant creadoEn,
            Instant actualizadoEn) {}
}
