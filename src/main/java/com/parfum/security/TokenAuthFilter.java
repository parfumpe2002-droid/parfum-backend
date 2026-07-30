package com.parfum.security;

import com.parfum.jpa.entity.AuthToken;
import com.parfum.jpa.entity.Usuario;
import com.parfum.jpa.repository.AuthTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TokenAuthFilter extends OncePerRequestFilter {
    private final AuthTokenRepository tokenRepository;

    public TokenAuthFilter(AuthTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            String raw = header.substring(7).trim();
            AuthToken stored = tokenRepository.findById(raw).orElse(null);
            if (stored != null) {
                if (stored.getExpiraEn().isAfter(Instant.now()) && stored.getUsuario().isActivo()) {
                    Usuario user = stored.getUsuario();
                    AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getEmail(), user.getNombre(), user.getRol().name());
                    var auth = new UsernamePasswordAuthenticationToken(
                            principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol().name())));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    tokenRepository.deleteById(raw);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
