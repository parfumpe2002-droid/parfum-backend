package com.parfum.jpa.repository;

import com.parfum.jpa.entity.Rol;
import com.parfum.jpa.entity.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    long countByActivoTrue();
    List<Usuario> findByRolAndActivoTrue(Rol rol);
}
