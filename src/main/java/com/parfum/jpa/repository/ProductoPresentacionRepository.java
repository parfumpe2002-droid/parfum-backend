package com.parfum.jpa.repository;

import com.parfum.jpa.entity.ProductoPresentacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoPresentacionRepository extends JpaRepository<ProductoPresentacion, Long> {
    List<ProductoPresentacion> findByProductoIdOrderByOrdenVisualAscMililitrosAsc(Long productoId);
    Optional<ProductoPresentacion> findByIdAndProductoId(Long id, Long productoId);
}
