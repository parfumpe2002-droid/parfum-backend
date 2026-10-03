package com.parfum.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate limiting ligero por IP para los endpoints públicos más sensibles.
 *
 * En producción Render sirve la aplicación detrás de Cloudflare. Por eso se
 * prioriza CF-Connecting-IP (reescrito por el proxy) y NO se confía en el
 * primer X-Forwarded-For enviado por el cliente.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_BUCKETS = 20_000;
    private final ConcurrentHashMap<String, Window> buckets = new ConcurrentHashMap<>();
    private final AtomicInteger operations = new AtomicInteger();
    private final ObjectMapper objectMapper;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        return policyFor(request) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Policy policy = policyFor(request);
        if (policy == null) {
            filterChain.doFilter(request, response);
            return;
        }

        cleanupOccasionally();
        long now = System.currentTimeMillis();
        String key = policy.name + ":" + clientIp(request);
        Window window = buckets.compute(key, (ignored, current) -> {
            if (current == null || now >= current.expiresAt) {
                return new Window(1, now + policy.windowMillis);
            }
            current.count += 1;
            return current;
        });

        long retryAfterSeconds = Math.max(1L, (window.expiresAt - now + 999L) / 1000L);
        int remaining = Math.max(0, policy.limit - window.count);
        response.setHeader("X-RateLimit-Limit", String.valueOf(policy.limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(window.expiresAt / 1000L));

        if (window.count > policy.limit) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "timestamp", Instant.now(),
                    "status", 429,
                    "message", "Demasiadas solicitudes. Espera un momento e inténtalo nuevamente."
            ));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Policy policyFor(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (!"POST".equalsIgnoreCase(method)) return null;

        if (path.equals("/api/auth/login")) return new Policy("login", 8, 10 * 60_000L);
        if (path.equals("/api/auth/register")) return new Policy("register", 4, 60 * 60_000L);
        if (path.equals("/api/contactos")) return new Policy("contact", 8, 60 * 60_000L);
        if (path.equals("/api/reclamos")) return new Policy("claim", 3, 60 * 60_000L);
        if (path.equals("/api/actividad")) return new Policy("activity", 120, 10 * 60_000L);
        if (path.equals("/api/pedidos")) return new Policy("order", 8, 60 * 60_000L);
        if (path.equals("/api/pedidos/comprobante")) return new Policy("proof-authenticated", 5, 15 * 60_000L);
        if (path.matches("/api/pedidos/\\d+/comprobante")) return new Policy("proof-replace", 5, 15 * 60_000L);
        if (path.matches("/api/pedidos/\\d+/comprobante-invitado")) return new Policy("proof-guest", 3, 30 * 60_000L);
        return null;
    }

    String clientIp(HttpServletRequest request) {
        String cf = cleanIp(request.getHeader("CF-Connecting-IP"));
        if (cf != null) return cf;

        String remote = cleanIp(request.getRemoteAddr());
        return remote == null ? "unknown" : remote;
    }

    private String cleanIp(String value) {
        if (value == null) return null;
        String clean = value.trim();
        if (clean.isBlank() || clean.length() > 64) return null;
        // IPv4 / IPv6 textual forms only. Do not let arbitrary header content
        // create unbounded rate-limit bucket keys.
        return clean.matches("[0-9a-fA-F:.]+") ? clean : null;
    }

    private void cleanupOccasionally() {
        int count = operations.incrementAndGet();
        if (count % 500 != 0 && buckets.size() < MAX_BUCKETS) return;
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> now >= entry.getValue().expiresAt);
        if (buckets.size() > MAX_BUCKETS) buckets.clear();
    }

    private record Policy(String name, int limit, long windowMillis) {}

    private static final class Window {
        private int count;
        private final long expiresAt;

        private Window(int count, long expiresAt) {
            this.count = count;
            this.expiresAt = expiresAt;
        }
    }
}
