package com.parfum.controller;

import com.parfum.dto.PushSubscriptionRequest;
import com.parfum.mongo.document.PushSubscription;
import com.parfum.mongo.repository.PushSubscriptionRepository;
import com.parfum.security.AuthenticatedUser;
import com.parfum.service.WebPushService;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private static final Set<String> HOSTS_EXACTOS = Set.of(
            "fcm.googleapis.com",
            "updates.push.services.mozilla.com",
            "push.services.mozilla.com",
            "web.push.apple.com"
    );

    private final PushSubscriptionRepository repository;
    private final WebPushService webPushService;

    public NotificacionController(
            PushSubscriptionRepository repository,
            WebPushService webPushService) {
        this.repository = repository;
        this.webPushService = webPushService;
    }

    @GetMapping("/clave-publica")
    public Map<String, Object> clavePublica() {
        return Map.of(
                "enabled", webPushService.configurado(),
                "publicKey", webPushService.getPublicKey()
        );
    }

    @PostMapping("/suscribir")
    public Map<String, Object> suscribir(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            @RequestBody PushSubscriptionRequest request) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para activar avisos");
        }

        String endpoint = validarEndpoint(request == null ? null : request.endpoint());
        String hash = hash(endpoint);
        Instant ahora = Instant.now();

        PushSubscription subscription = repository.findByEndpointHash(hash)
                .orElseGet(PushSubscription::new);
        if (subscription.getFechaCreacion() == null) subscription.setFechaCreacion(ahora);
        subscription.setUsuarioId(user.id());
        subscription.setEndpointHash(hash);
        subscription.setEndpoint(endpoint);
        subscription.setUserAgent(limitar(userAgent, 300));
        subscription.setFechaActualizacion(ahora);
        repository.save(subscription);

        return Map.of("suscrito", true);
    }

    @DeleteMapping("/suscribir")
    public Map<String, Object> desuscribir(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody PushSubscriptionRequest request) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
        }

        String endpoint = validarEndpoint(request == null ? null : request.endpoint());
        repository.findByEndpointHash(hash(endpoint))
                .filter(subscription -> user.id().equals(subscription.getUsuarioId()))
                .ifPresent(repository::delete);
        return Map.of("suscrito", false);
    }

    private String validarEndpoint(String valor) {
        if (valor == null || valor.isBlank() || valor.length() > 2200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endpoint de notificación no válido");
        }
        try {
            URI uri = URI.create(valor.trim());
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            boolean permitido = HOSTS_EXACTOS.contains(host) || host.endsWith(".notify.windows.com");
            if (!"https".equalsIgnoreCase(uri.getScheme()) || !permitido) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Proveedor de notificaciones no permitido");
            }
            return uri.toString();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Endpoint de notificación no válido");
        }
    }

    private String hash(String endpoint) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(endpoint.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo proteger el endpoint", ex);
        }
    }

    private String limitar(String valor, int maximo) {
        if (valor == null) return null;
        String limpio = valor.replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "").trim();
        return limpio.length() <= maximo ? limpio : limpio.substring(0, maximo);
    }
}
