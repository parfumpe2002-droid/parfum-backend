package com.parfum.controller;

import com.parfum.dto.ActividadDtos.ActividadRequest;
import com.parfum.dto.ActividadDtos.ActividadResponse;
import com.parfum.mongo.document.ActividadSitio;
import com.parfum.mongo.repository.ActividadSitioRepository;
import com.parfum.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/actividad")
public class ActividadController {
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "PAGE_VIEW", "PRODUCT_VIEW", "LOGIN", "REGISTER", "ADD_CART",
            "FAVORITE", "CONTACT", "ORDER", "SEARCH", "OTHER"
    );

    private final ActividadSitioRepository repository;

    public ActividadController(ActividadSitioRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActividadResponse create(@AuthenticationPrincipal AuthenticatedUser user,
                                    @Valid @RequestBody ActividadRequest request) {
        ActividadSitio activity = new ActividadSitio();
        String type = clean(request.tipo()).toUpperCase(Locale.ROOT);
        activity.setTipo(ALLOWED_TYPES.contains(type) ? type : "OTHER");
        activity.setPagina(clean(request.pagina()));
        activity.setRuta(clean(request.ruta()));
        activity.setSessionId(clean(request.sessionId()));
        activity.setProductoId(request.productoId());
        activity.setProductoNombre(clean(request.productoNombre()));
        activity.setDetalle(clean(request.detalle()));
        activity.setDispositivo(clean(request.dispositivo()));
        if (user != null) {
            activity.setUsuarioId(user.id());
            activity.setUsuarioEmail(user.email());
        }
        return toResponse(repository.save(activity));
    }

    public static ActividadResponse toResponse(ActividadSitio activity) {
        return new ActividadResponse(
                activity.getId(), activity.getTipo(), activity.getPagina(), activity.getRuta(),
                activity.getSessionId(), activity.getUsuarioId(), activity.getUsuarioEmail(),
                activity.getProductoId(), activity.getProductoNombre(), activity.getDetalle(),
                activity.getDispositivo(), activity.getCreadoEn()
        );
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
