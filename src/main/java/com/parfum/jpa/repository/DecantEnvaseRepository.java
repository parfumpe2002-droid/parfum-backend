package com.parfum.jpa.repository;

import com.parfum.jpa.entity.DecantEnvase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecantEnvaseRepository extends JpaRepository<DecantEnvase, Long> {
    List<DecantEnvase> findByActivoTrueOrderByMililitrosAscOrdenVisualAsc();
}
