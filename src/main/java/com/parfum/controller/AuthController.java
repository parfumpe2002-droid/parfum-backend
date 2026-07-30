package com.parfum.controller;

import com.parfum.dto.AuthDtos.AuthResponse;
import com.parfum.dto.AuthDtos.LoginRequest;
import com.parfum.dto.AuthDtos.RegisterRequest;
import com.parfum.dto.AuthDtos.UsuarioResponse;
import com.parfum.security.AuthenticatedUser;
import com.parfum.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) { return authService.register(request); }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return authService.login(request); }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        authService.logout(authorization);
        return Map.of("message", "Sesión cerrada");
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return authService.toResponse(authService.requireUser(principal.id()));
    }
}
