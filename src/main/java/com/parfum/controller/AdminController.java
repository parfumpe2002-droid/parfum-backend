package com.parfum.controller;

import com.parfum.dto.ActividadDtos.ActividadResponse;
import com.parfum.dto.AuthDtos.UsuarioResponse;
import com.parfum.dto.ProductoDtos.ProductoResponse;
import com.parfum.jpa.entity.EstadoPago;
import com.parfum.jpa.entity.EstadoPedido;
import com.parfum.jpa.entity.Pedido;
import com.parfum.jpa.entity.Producto;
import com.parfum.jpa.entity.Rol;
import com.parfum.jpa.entity.Usuario;
import com.parfum.jpa.repository.PedidoRepository;
import com.parfum.jpa.repository.ProductoRepository;
import com.parfum.jpa.repository.UsuarioRepository;
import com.parfum.mongo.document.ActividadSitio;
import com.parfum.mongo.repository.ActividadSitioRepository;
import com.parfum.mongo.repository.ContactoRepository;
import com.parfum.mongo.repository.ResenaRepository;
import com.parfum.service.AuthService;
import com.parfum.service.ProductoMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Lima");
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM", new Locale("es", "PE"));

    private final UsuarioRepository usuarios;
    private final ProductoRepository productos;
    private final PedidoRepository pedidos;
    private final ContactoRepository contactos;
    private final ResenaRepository resenas;
    private final ActividadSitioRepository actividad;
    private final AuthService authService;
    private final ProductoMapper productoMapper;

    public AdminController(UsuarioRepository usuarios,
                           ProductoRepository productos,
                           PedidoRepository pedidos,
                           ContactoRepository contactos,
                           ResenaRepository resenas,
                           ActividadSitioRepository actividad,
                           AuthService authService,
                           ProductoMapper productoMapper) {
        this.usuarios = usuarios;
        this.productos = productos;
        this.pedidos = pedidos;
        this.contactos = contactos;
        this.resenas = resenas;
        this.actividad = actividad;
        this.authService = authService;
        this.productoMapper = productoMapper;
    }

    @GetMapping("/resumen")
    public Map<String, Object> summary() {
        BigDecimal ventas = pedidos.findAll().stream()
                .filter(p -> p.getEstadoPago() == EstadoPago.CONFIRMADO)
                .filter(p -> p.getEstado() != EstadoPedido.CANCELADO)
                .map(Pedido::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        Instant todayStart = today.atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant sevenDaysStart = today.minusDays(6).atStartOfDay(BUSINESS_ZONE).toInstant();
        List<ActividadSitio> lastSevenDays = actividad.findByCreadoEnAfterOrderByCreadoEnDesc(sevenDaysStart);

        long visitsToday = lastSevenDays.stream()
                .filter(item -> "PAGE_VIEW".equals(item.getTipo()))
                .filter(item -> !item.getCreadoEn().isBefore(todayStart))
                .count();
        long uniqueToday = lastSevenDays.stream()
                .filter(item -> "PAGE_VIEW".equals(item.getTipo()))
                .filter(item -> !item.getCreadoEn().isBefore(todayStart))
                .map(ActividadSitio::getSessionId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        long productViewsToday = lastSevenDays.stream()
                .filter(item -> "PRODUCT_VIEW".equals(item.getTipo()))
                .filter(item -> !item.getCreadoEn().isBefore(todayStart))
                .count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("usuarios", usuarios.countByActivoTrue());
        result.put("productos", productos.countByActivoTrue());
        result.put("pedidos", pedidos.count());
        result.put("pagosPendientes", pedidos.findAll().stream().filter(p -> p.getEstadoPago() == null || p.getEstadoPago() == EstadoPago.PENDIENTE_VERIFICACION).count());
        result.put("ventas", ventas);
        result.put("mensajesNuevos", contactos.countByEstado("NUEVO"));
        result.put("resenas", resenas.count());
        result.put("visitasHoy", visitsToday);
        result.put("visitantesHoy", uniqueToday);
        result.put("productosVistosHoy", productViewsToday);
        result.put("visitas7Dias", lastSevenDays.stream().filter(item -> "PAGE_VIEW".equals(item.getTipo())).count());
        result.put("serie7Dias", buildDailySeries(today, lastSevenDays));
        result.put("actividadReciente", lastSevenDays.stream().limit(12).map(ActividadController::toResponse).toList());
        return result;
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> users() {
        return usuarios.findAll().stream()
                .sorted(Comparator.comparing(Usuario::getCreadoEn).reversed())
                .map(authService::toResponse)
                .toList();
    }

    @PatchMapping("/usuarios/{id}/rol")
    public UsuarioResponse role(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Usuario user = usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        try {
            user.setRol(Rol.valueOf(String.valueOf(body.get("rol")).toUpperCase(Locale.ROOT)));
        } catch (Exception error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido");
        }
        return authService.toResponse(usuarios.save(user));
    }

    @PatchMapping("/usuarios/{id}/activo")
    public UsuarioResponse active(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Usuario user = usuarios.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        user.setActivo(Boolean.TRUE.equals(body.get("activo")));
        return authService.toResponse(usuarios.save(user));
    }

    @GetMapping("/productos")
    public List<ProductoResponse> products() {
        return productos.findAll().stream()
                .sorted(Comparator.comparing(Producto::getActualizadoEn).reversed())
                .map(productoMapper::toResponse)
                .toList();
    }

    @GetMapping("/categorias")
    public List<String> categories() {
        Set<String> values = productos.findAll().stream()
                .map(Producto::getCategoria)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .filter(value -> !"Decant".equalsIgnoreCase(value))
                .collect(Collectors.toSet());
        values.addAll(List.of("Diseñador", "Nicho", "Árabe"));
        return values.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    @GetMapping("/actividad")
    public List<ActividadResponse> activity(@RequestParam(defaultValue = "150") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        return actividad.findByOrderByCreadoEnDesc(PageRequest.of(0, safeLimit)).stream()
                .map(ActividadController::toResponse)
                .toList();
    }

    @DeleteMapping("/actividad/{id}")
    public Map<String, Object> deleteActivity(@PathVariable String id) {
        if (!actividad.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de actividad no encontrado");
        }
        actividad.deleteById(id);
        return Map.of("eliminados", 1);
    }

    @DeleteMapping("/actividad")
    public Map<String, Object> deleteActivityBatch(
            @RequestParam(defaultValue = "false") boolean all,
            @RequestParam(required = false) Integer olderThanDays) {
        long deleted;
        if (all) {
            deleted = actividad.count();
            actividad.deleteAll();
        } else if (olderThanDays != null) {
            int safeDays = Math.min(Math.max(olderThanDays, 1), 3650);
            Instant limit = Instant.now().minus(safeDays, ChronoUnit.DAYS);
            deleted = actividad.deleteByCreadoEnBefore(limit);
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Indica all=true u olderThanDays para limpiar la actividad"
            );
        }
        return Map.of("eliminados", deleted);
    }

    private List<Map<String, Object>> buildDailySeries(LocalDate today, List<ActividadSitio> activity) {
        List<Map<String, Object>> series = new ArrayList<>();
        for (int offset = 6; offset >= 0; offset--) {
            LocalDate date = today.minusDays(offset);
            List<ActividadSitio> dayItems = activity.stream()
                    .filter(item -> item.getCreadoEn() != null)
                    .filter(item -> item.getCreadoEn().atZone(BUSINESS_ZONE).toLocalDate().equals(date))
                    .toList();
            long visits = dayItems.stream().filter(item -> "PAGE_VIEW".equals(item.getTipo())).count();
            long visitors = dayItems.stream()
                    .filter(item -> "PAGE_VIEW".equals(item.getTipo()))
                    .map(ActividadSitio::getSessionId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("fecha", date.toString());
            point.put("etiqueta", date.format(DAY_FORMAT));
            point.put("visitas", visits);
            point.put("visitantes", visitors);
            series.add(point);
        }
        return series;
    }
}
