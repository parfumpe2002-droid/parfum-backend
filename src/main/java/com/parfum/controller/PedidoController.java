package com.parfum.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.parfum.dto.CommerceDtos.CambiarEstadoPagoRequest;
import com.parfum.dto.CommerceDtos.CambiarEstadoRequest;
import com.parfum.dto.CommerceDtos.ComprobanteResponse;
import com.parfum.dto.CommerceDtos.CrearPedidoRequest;
import com.parfum.dto.CommerceDtos.PedidoResponse;
import com.parfum.jpa.entity.EstadoPago;
import com.parfum.jpa.entity.EstadoPedido;
import com.parfum.jpa.entity.Pedido;
import com.parfum.jpa.repository.PedidoRepository;
import com.parfum.security.AuthenticatedUser;
import com.parfum.service.AuthService;
import com.parfum.service.PedidoService;
import com.parfum.service.WebPushService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoRepository repository;
    private final PedidoService pedidoService;
    private final AuthService authService;
    private final Cloudinary cloudinary;
    private final WebPushService webPushService;
    private final String cloudName;

    public PedidoController(PedidoRepository repository,
                            PedidoService pedidoService,
                            AuthService authService,
                            Cloudinary cloudinary,
                            WebPushService webPushService,
                            @Value("${cloudinary.cloud-name}") String cloudName) {
        this.repository = repository;
        this.pedidoService = pedidoService;
        this.authService = authService;
        this.cloudinary = cloudinary;
        this.webPushService = webPushService;
        this.cloudName = cloudName;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse create(@AuthenticationPrincipal AuthenticatedUser user,
                                 @Valid @RequestBody CrearPedidoRequest request) {
        return pedidoService.crear(user == null ? null : authService.requireUser(user.id()), request);
    }

    /**
     * No existe ya un uploader público genérico. El archivo solo puede asociarse
     * a un pedido existente cuyo propietario esté autenticado o presente el token
     * temporal de invitado devuelto al crear ese pedido.
     */
    @PostMapping(value = "/{id}/comprobante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PedidoResponse replaceProof(@PathVariable Long id,
                                       @AuthenticationPrincipal AuthenticatedUser authenticated,
                                       @RequestHeader(value = "X-Parfum-Guest-Token", required = false) String guestToken,
                                       @RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "numeroOperacion", required = false) String numeroOperacion) throws IOException {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        boolean owner = authenticated != null && pedido.getUsuario().getId().equals(authenticated.id());
        boolean admin = authenticated != null && "ADMIN".equalsIgnoreCase(authenticated.rol());
        boolean guest = pedidoService.guestTokenValid(pedido, guestToken);
        if (!owner && !admin && !guest) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar este pedido");
        }
        if (pedido.getEstado() == EstadoPedido.ENTREGADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este pedido ya no admite otro comprobante");
        }

        String operation = trimToNull(numeroOperacion);
        if (operation != null && operation.length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Número de operación demasiado largo");
        }

        ComprobanteResponse uploaded = uploadPaymentProof(file);
        String previousPublicId = pedido.getComprobantePublicId();
        pedido.setNumeroOperacion(operation);
        pedido.setComprobanteUrl(uploaded.url());
        pedido.setComprobantePublicId(uploaded.publicId());
        pedido.setEstadoPago(EstadoPago.PENDIENTE_VERIFICACION);
        pedido.setObservacionPago(null);
        pedido.setPagadoEn(null);
        if (pedido.getEstado() == EstadoPedido.CONFIRMADO) pedido.setEstado(EstadoPedido.PENDIENTE);
        Pedido saved = repository.save(pedido);

        if (isOwnedPaymentProof(previousPublicId) && !previousPublicId.equals(uploaded.publicId())) {
            try { cloudinary.uploader().destroy(previousPublicId, ObjectUtils.emptyMap()); } catch (Exception ignored) {}
        }
        return pedidoService.toResponse(saved);
    }

    @GetMapping("/me")
    public List<PedidoResponse> mine(@AuthenticationPrincipal AuthenticatedUser user) {
        return repository.findByUsuarioIdOrderByCreadoEnDesc(user.id()).stream()
                .map(pedidoService::toResponse).toList();
    }

    @GetMapping
    public List<PedidoResponse> all() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Pedido::getCreadoEn).reversed())
                .map(pedidoService::toResponse).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        if (List.of(EstadoPedido.PREPARANDO, EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO).contains(pedido.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar un pedido en preparación, enviado o entregado");
        }
        if (pedido.isStockAplicado()) pedidoService.liberarStock(pedido);
        String publicId = pedido.getComprobantePublicId();
        repository.delete(pedido);
        if (isOwnedPaymentProof(publicId)) {
            try { cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap()); } catch (Exception ignored) {}
        }
    }

    @PatchMapping("/{id}/pago")
    public PedidoResponse changePaymentStatus(@PathVariable Long id,
                                              @Valid @RequestBody CambiarEstadoPagoRequest request) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        EstadoPago next;
        try {
            next = EstadoPago.valueOf(request.estadoPago().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de pago inválido");
        }

        if (next != EstadoPago.CONFIRMADO
                && List.of(EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO).contains(pedido.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No puedes rechazar el pago de un pedido que ya fue enviado o entregado");
        }

        if (next == EstadoPago.CONFIRMADO && !pedido.isStockAplicado()) {
            pedidoService.aplicarStock(pedido);
        } else if (next != EstadoPago.CONFIRMADO && pedido.isStockAplicado()
                && !List.of(EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO).contains(pedido.getEstado())) {
            pedidoService.liberarStock(pedido);
        }

        pedido.setEstadoPago(next);
        pedido.setObservacionPago(trimToNull(request.observacion()));
        if (next == EstadoPago.CONFIRMADO) {
            pedido.setPagadoEn(Instant.now());
            if (pedido.getEstado() == EstadoPedido.PENDIENTE) pedido.setEstado(EstadoPedido.CONFIRMADO);
        } else {
            pedido.setPagadoEn(null);
            if (List.of(EstadoPedido.CONFIRMADO, EstadoPedido.PREPARANDO).contains(pedido.getEstado())) {
                pedido.setEstado(EstadoPedido.PENDIENTE);
            }
            if (next == EstadoPago.SOLICITAR_NUEVO_COMPROBANTE) {
                pedido.setObservacionPago(trimToNull(request.observacion()) == null
                        ? "Adjunta un nuevo comprobante para continuar con el pedido"
                        : trimToNull(request.observacion()));
            }
        }
        Pedido saved = repository.save(pedido);
        webPushService.notificarActualizacionPedido(saved.getUsuario().getId());
        return pedidoService.toResponse(saved);
    }

    @PatchMapping("/{id}/estado")
    public PedidoResponse changeOrderStatus(@PathVariable Long id,
                                            @Valid @RequestBody CambiarEstadoRequest request) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        EstadoPedido next;
        try {
            next = EstadoPedido.valueOf(request.estado().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
        }
        boolean requiresConfirmedPayment = List.of(
                EstadoPedido.CONFIRMADO, EstadoPedido.PREPARANDO,
                EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO).contains(next);
        if (requiresConfirmedPayment && pedido.getEstadoPago() != EstadoPago.CONFIRMADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Primero debes confirmar el pago del pedido");
        }
        if (next == EstadoPedido.CANCELADO && pedido.isStockAplicado()
                && !List.of(EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO).contains(pedido.getEstado())) {
            pedidoService.liberarStock(pedido);
        }
        pedido.setEstado(next);
        Pedido saved = repository.save(pedido);
        webPushService.notificarActualizacionPedido(saved.getUsuario().getId());
        return pedidoService.toResponse(saved);
    }

    private ComprobanteResponse uploadPaymentProof(MultipartFile file) throws IOException {
        if (cloudName == null || cloudName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cloudinary aún no está configurado");
        }
        if (file == null || file.isEmpty() || !allowedProofType(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El comprobante debe ser una imagen JPG, PNG o WEBP");
        }
        if (file.getSize() > 5L * 1024L * 1024L) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "El comprobante no puede superar 5 MB");
        }
        byte[] bytes = file.getBytes();
        if (!looksLikeAllowedImage(bytes, file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El contenido del comprobante no corresponde a una imagen válida");
        }
        Map<?, ?> result = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                "folder", "parfum/comprobantes",
                "resource_type", "image",
                "use_filename", false,
                "unique_filename", true,
                "overwrite", false,
                "tags", "parfum,comprobante-pago"
        ));
        return new ComprobanteResponse(String.valueOf(result.get("secure_url")),
                String.valueOf(result.get("public_id")));
    }

    private boolean allowedProofType(String contentType) {
        return contentType != null && List.of("image/jpeg", "image/png", "image/webp")
                .contains(contentType.toLowerCase(Locale.ROOT));
    }

    private boolean looksLikeAllowedImage(byte[] bytes, String contentType) {
        if (bytes == null || bytes.length < 12 || contentType == null) return false;
        String type = contentType.toLowerCase(Locale.ROOT);
        if ("image/jpeg".equals(type)) {
            return (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff;
        }
        if ("image/png".equals(type)) {
            int[] signature = {0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
            for (int i = 0; i < signature.length; i++) if ((bytes[i] & 0xff) != signature[i]) return false;
            return true;
        }
        if ("image/webp".equals(type)) {
            return bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                    && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
        }
        return false;
    }

    private boolean isOwnedPaymentProof(String publicId) {
        return publicId != null && publicId.startsWith("parfum/comprobantes/");
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
