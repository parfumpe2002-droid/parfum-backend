package com.parfum.jpa.repository;

import com.parfum.jpa.entity.ProductoDecant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoDecantRepository extends JpaRepository<ProductoDecant, Long> {
    List<ProductoDecant> findByProductoIdOrderByOrdenVisualAsc(Long productoId);
    Optional<ProductoDecant> findByIdAndProductoId(Long id, Long productoId);
}
