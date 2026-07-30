package com.parfum.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class CommerceDtos {
    private CommerceDtos() {}

    public record CantidadRequest(@NotNull @Min(1) @Max(99) Integer cantidad) {}

    public record CarritoRequest(
            @NotNull @Min(1) @Max(99) Integer cantidad,
            Long presentacionId,
            Long productoDecantId,
            @Size(max = 20) String tipoItem) {}

    public record FavoritoRequest(
            Long presentacionId,
            Long productoDecantId,
            @Size(max = 20) String tipoItem) {}

    public record ItemPedidoRequest(
            @NotNull Long productoId,
            Long presentacionId,
            Long productoDecantId,
            @Size(max = 20) String tipoItem,
            @NotNull @Min(1) @Max(99) Integer cantidad) {}

    public record CrearPedidoRequest(
            @NotEmpty List<@Valid ItemPedidoRequest> items,
            Long regaloProductoId,
            @NotBlank @Size(max = 50) String metodoPago,
            @Size(max = 80) String numeroOperacion,
            @Size(max = 800) String comprobanteUrl,
            @Size(max = 300) String comprobantePublicId,
            @NotBlank @Size(max = 350) String direccionEntrega,
            @Size(max = 100) String nombreCliente,
            @Email @Size(max = 160) String correoCliente,
            @Size(max = 30) String telefonoContacto) {}

    public record CambiarEstadoRequest(@NotBlank String estado) {}

    public record CambiarEstadoPagoRequest(
            @NotBlank String estadoPago,
            @Size(max = 500) String observacion) {}

    public record ComprobanteResponse(String url, String publicId) {}

    public record DetallePedidoResponse(
            Long productoId,
            Long presentacionId,
            Long productoDecantId,
            String tipoItem,
            Integer mililitros,
            String presentacion,
            String nombreProducto,
            String imagenUrl,
            BigDecimal precioUnitario,
            Integer cantidad,
            boolean regalo) {}

    public record PedidoResponse(
            Long id,
            Long usuarioId,
            String usuarioNombre,
            String usuarioEmail,
            String clienteNombre,
            String clienteCorreo,
            String clienteTelefono,
            BigDecimal total,
            String estado,
            String metodoPago,
            String estadoPago,
            String numeroOperacion,
            String comprobanteUrl,
            String observacionPago,
            Instant pagadoEn,
            String direccionEntrega,
            Instant creadoEn,
            List<DetallePedidoResponse> detalles) {}

    public record ResenaRequest(
            @NotNull @Min(1) @Max(5) Integer puntuacion,
            @NotBlank @Size(max = 1000) String comentario) {}

    public record ContactoRequest(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @jakarta.validation.constraints.Email @Size(max = 160) String correo,
            @NotBlank @Size(max = 160) String asunto,
            @NotBlank @Size(max = 2000) String mensaje) {}

    public record ContactoEstadoRequest(@NotBlank @Size(max = 30) String estado) {}
}
