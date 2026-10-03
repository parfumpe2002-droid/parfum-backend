package com.parfum.config;

import com.parfum.security.RateLimitFilter;
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

    private static final List<String> PRODUCTION_ORIGINS = List.of(
            "https://parfum.com.pe",
            "https://www.parfum.com.pe",
            "https://parfum-store-app.netlify.app"
    );

    private final TokenAuthFilter tokenAuthFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(TokenAuthFilter tokenAuthFilter, RateLimitFilter rateLimitFilter) {
        this.tokenAuthFilter = tokenAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/health", "/api/health/**", "/api/seo/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/notificaciones/clave-publica").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/contactos",
                                "/api/actividad",
                                "/api/pedidos",
                                "/api/pedidos/*/comprobante").permitAll()
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
                .addFilterBefore(rateLimitFilter, TokenAuthFilter.class)
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors-origins:}") String configuredOrigins) {

        Set<String> originPatterns = new LinkedHashSet<>(PRODUCTION_ORIGINS);
        if (configuredOrigins != null && !configuredOrigins.isBlank()) {
            Arrays.stream(configuredOrigins.split(","))
                    .map(String::trim)
                    .filter(origin -> !origin.isBlank())
                    .forEach(originPatterns::add);
        }

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(new ArrayList<>(originPatterns));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Parfum-Guest-Token"));
        config.setExposedHeaders(List.of("Location"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
