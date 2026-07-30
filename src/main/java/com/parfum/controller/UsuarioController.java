package com.parfum.controller;

import com.parfum.dto.AuthDtos.ChangePasswordRequest;
import com.parfum.dto.AuthDtos.UpdateProfileRequest;
import com.parfum.dto.AuthDtos.UsuarioResponse;
import com.parfum.jpa.entity.Usuario;
import com.parfum.jpa.repository.UsuarioRepository;
import com.parfum.security.AuthenticatedUser;
import com.parfum.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final AuthService authService;
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioController(AuthService authService, UsuarioRepository repository, PasswordEncoder encoder) {
        this.authService = authService;
        this.repository = repository;
        this.encoder = encoder;
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return authService.toResponse(authService.requireUser(principal.id()));
    }

    @PutMapping("/me")
    public UsuarioResponse update(@AuthenticationPrincipal AuthenticatedUser principal,
                                  @Valid @RequestBody UpdateProfileRequest request) {
        Usuario user = authService.requireUser(principal.id());
        user.setNombre(request.nombre().trim());
        user.setApellido(clean(request.apellido()));
        user.setTelefono(clean(request.telefono()));
        user.setDireccion(clean(request.direccion()));
        return authService.toResponse(repository.save(user));
    }

    @PutMapping("/me/password")
    public Map<String, String> changePassword(@AuthenticationPrincipal AuthenticatedUser principal,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        Usuario user = authService.requireUser(principal.id());
        if (!encoder.matches(request.actual(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta");
        }
        user.setPasswordHash(encoder.encode(request.nueva()));
        repository.save(user);
        return Map.of("message", "Contraseña actualizada");
    }

    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
