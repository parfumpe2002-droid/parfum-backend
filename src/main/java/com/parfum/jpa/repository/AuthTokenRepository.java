package com.parfum.jpa.repository;

import com.parfum.jpa.entity.AuthToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {
    void deleteByUsuarioId(Long usuarioId);
    long deleteByExpiraEnBefore(Instant instant);
}
