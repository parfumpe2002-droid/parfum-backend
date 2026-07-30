package com.parfum.service;

import com.parfum.dto.AuthDtos.AuthResponse;
import com.parfum.dto.AuthDtos.LoginRequest;
import com.parfum.dto.AuthDtos.RegisterRequest;
import com.parfum.dto.AuthDtos.UsuarioResponse;
import com.parfum.jpa.entity.AuthToken;
import com.parfum.jpa.entity.Rol;
import com.parfum.jpa.entity.Usuario;
import com.parfum.jpa.repository.AuthTokenRepository;
import com.parfum.jpa.repository.UsuarioRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final AuthTokenRepository tokenRepository;
    private final PasswordEncoder encoder;
    private final int tokenDays;

    public AuthService(UsuarioRepository usuarioRepository, AuthTokenRepository tokenRepository,
                       PasswordEncoder encoder, @Value("${app.token-days}") int tokenDays) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.encoder = encoder;
        this.tokenDays = tokenDays;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        Usuario user = new Usuario();
        user.setNombre(request.nombre().trim());
        user.setApellido(clean(request.apellido()));
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(request.password()));
        user.setTelefono(clean(request.telefono()));
        user.setRol(Rol.USER);
        usuarioRepository.save(user);
        return issueToken(user);
    }

    public AuthResponse login(LoginRequest request) {
        Usuario user = usuarioRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));
        if (!user.isActivo() || !encoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }
        return issueToken(user);
    }

    public void logout(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            tokenRepository.deleteById(authorization.substring(7).trim());
        }
    }

    public Usuario requireUser(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no disponible"));
    }

    public UsuarioResponse toResponse(Usuario user) {
        return new UsuarioResponse(user.getId(), user.getNombre(), user.getApellido(), user.getEmail(),
                user.getTelefono(), user.getDireccion(), user.getRol().name(), user.isActivo());
    }

    private AuthResponse issueToken(Usuario user) {
        String raw = UUID.randomUUID() + "." + UUID.randomUUID();
        AuthToken token = new AuthToken();
        token.setToken(raw);
        token.setUsuario(user);
        token.setExpiraEn(Instant.now().plus(tokenDays, ChronoUnit.DAYS));
        tokenRepository.save(token);
        return new AuthResponse(raw, toResponse(user));
    }

    private static String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
