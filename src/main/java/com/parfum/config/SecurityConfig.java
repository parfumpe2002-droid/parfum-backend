package com.parfum.config;

import com.parfum.security.TokenAuthFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private static final List<String> DEFAULT_ORIGIN_PATTERNS = List.of(
            "https://parfum-store-app.netlify.app",
            "https://*.netlify.app",
            "http://localhost:8383",
            "http://localhost:5500",
            "http://localhost:8080",
            "http://127.0.0.1:5500",
            "http://127.0.0.1:8383",
            "http://127.0.0.1:8080"
    );

    private final TokenAuthFilter tokenAuthFilter;

    public SecurityConfig(TokenAuthFilter tokenAuthFilter) {
        this.tokenAuthFilter = tokenAuthFilter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // El navegador envía OPTIONS antes de POST/PUT/PATCH/DELETE.
                        // Debe pasar sin autenticación para que CORS pueda responder.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/health", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/contactos",
                                "/api/actividad",
                                "/api/pedidos",
                                "/api/pedidos/comprobante").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/productos/**",
                                "/api/resenas/producto/**",
                                "/api/decants/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/pedidos",
                                "/api/contactos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/pedidos/**",
                                "/api/contactos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/contactos", "/api/contactos/**", "/api/pedidos/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**", "/api/imagenes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(tokenAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors-origins:}") String configuredOrigins) {

        Set<String> originPatterns = new LinkedHashSet<>(DEFAULT_ORIGIN_PATTERNS);

        if (configuredOrigins != null && !configuredOrigins.isBlank()) {
            Arrays.stream(configuredOrigins.split(","))
                    .map(String::trim)
                    .filter(origin -> !origin.isBlank())
                    .forEach(originPatterns::add);
        }

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(new ArrayList<>(originPatterns));
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Location", "Authorization"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
