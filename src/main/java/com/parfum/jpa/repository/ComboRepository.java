package com.parfum.jpa.repository;

import com.parfum.jpa.entity.Combo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComboRepository extends JpaRepository<Combo, Long> {
    List<Combo> findByPublicadoTrueOrderByActualizadoEnDesc();
    List<Combo> findAllByOrderByActualizadoEnDesc();
}
