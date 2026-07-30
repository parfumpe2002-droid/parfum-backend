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
    private final String cloudName;

    public PedidoController(PedidoRepository repository,
                            PedidoService pedidoService,
                            AuthService authService,
                            Cloudinary cloudinary,
                            @Value("${cloudinary.cloud-name}") String cloudName) {
        this.repository = repository;
        this.pedidoService = pedidoService;
        this.authService = authService;
        this.cloudinary = cloudinary;
        this.cloudName = cloudName;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse create(@AuthenticationPrincipal AuthenticatedUser user,
                                 @Valid @RequestBody CrearPedidoRequest request) {
        return pedidoService.crear(user == null ? null : authService.requireUser(user.id()), request);
    }

    @PostMapping(value = "/comprobante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ComprobanteResponse uploadProof(@RequestParam("file") MultipartFile file) throws IOException {
        return uploadPaymentProof(file);
    }

    @PostMapping(value = "/{id}/comprobante", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PedidoResponse replaceProof(@PathVariable Long id,
                                       @AuthenticationPrincipal AuthenticatedUser authenticated,
                                       @RequestParam("file") MultipartFile file,
                                       @RequestParam("numeroOperacion") String numeroOperacion) throws IOException {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        boolean owner = authenticated != null && pedido.getUsuario().getId().equals(authenticated.id());
        boolean admin = authenticated != null && "ADMIN".equalsIgnoreCase(authenticated.rol());
        if (!owner && !admin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes modificar este pedido");
        }
        if (pedido.getEstado() == EstadoPedido.ENTREGADO || pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este pedido ya no admite otro comprobante");
        }
        if (numeroOperacion == null || numeroOperacion.isBlank() || numeroOperacion.trim().length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa un número de operación válido");
        }

        ComprobanteResponse uploaded = uploadPaymentProof(file);
        String previousPublicId = pedido.getComprobantePublicId();
        pedido.setNumeroOperacion(numeroOperacion.trim());
        pedido.setComprobanteUrl(uploaded.url());
        pedido.setComprobantePublicId(uploaded.publicId());
        pedido.setEstadoPago(EstadoPago.PENDIENTE_VERIFICACION);
        pedido.setObservacionPago(null);
        pedido.setPagadoEn(null);
        if (pedido.getEstado() == EstadoPedido.CONFIRMADO) pedido.setEstado(EstadoPedido.PENDIENTE);
        Pedido saved = repository.save(pedido);

        if (previousPublicId != null && !previousPublicId.isBlank()
                && !previousPublicId.equals(uploaded.publicId())) {
            try {
                cloudinary.uploader().destroy(previousPublicId, ObjectUtils.emptyMap());
            } catch (Exception ignored) {
            }
        }
        return pedidoService.toResponse(saved);
    }

    @GetMapping("/me")
    public List<PedidoResponse> mine(@AuthenticationPrincipal AuthenticatedUser user) {
        return repository.findByUsuarioIdOrderByCreadoEnDesc(user.id()).stream()
                .map(pedidoService::toResponse)
                .toList();
    }

    @GetMapping
    public List<PedidoResponse> all() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Pedido::getCreadoEn).reversed())
                .map(pedidoService::toResponse)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        String publicId = pedido.getComprobantePublicId();
        repository.delete(pedido);
        if (publicId != null && !publicId.isBlank()) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            } catch (Exception ignored) {
            }
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
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puedes rechazar el pago de un pedido que ya fue enviado o entregado"
            );
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
        return pedidoService.toResponse(repository.save(pedido));
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
                EstadoPedido.CONFIRMADO,
                EstadoPedido.PREPARANDO,
                EstadoPedido.ENVIADO,
                EstadoPedido.ENTREGADO
        ).contains(next);
        if (requiresConfirmedPayment && pedido.getEstadoPago() != EstadoPago.CONFIRMADO) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Primero debes confirmar el pago del pedido"
            );
        }
        pedido.setEstado(next);
        return pedidoService.toResponse(repository.save(pedido));
    }

    private ComprobanteResponse uploadPaymentProof(MultipartFile file) throws IOException {
        if (cloudName == null || cloudName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cloudinary aún no está configurado");
        }
        if (file == null || file.isEmpty() || file.getContentType() == null
                || !file.getContentType().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una imagen válida del comprobante");
        }
        if (file.getSize() > 8L * 1024L * 1024L) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "El comprobante no puede superar 8 MB");
        }
        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "parfum/comprobantes",
                "resource_type", "image",
                "use_filename", false,
                "unique_filename", true,
                "overwrite", false,
                "tags", "parfum,comprobante-pago"
        ));
        return new ComprobanteResponse(
                String.valueOf(result.get("secure_url")),
                String.valueOf(result.get("public_id"))
        );
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
