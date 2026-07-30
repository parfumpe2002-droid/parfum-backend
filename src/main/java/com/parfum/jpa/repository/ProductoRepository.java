package com.parfum.jpa.repository;

import com.parfum.jpa.entity.Producto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    @Query("""
        select p from Producto p
        where p.activo = true
          and (
              :q = ''
              or lower(p.nombre) like concat('%', :q, '%')
              or lower(p.marca) like concat('%', :q, '%')
              or lower(p.categoria) like concat('%', :q, '%')
              or lower(p.genero) like concat('%', :q, '%')
              or lower(coalesce(p.concentracion, '')) like concat('%', :q, '%')
              or lower(p.sku) like concat('%', :q, '%')
          )
          and (:categoria = '' or lower(p.categoria) = :categoria)
          and (:marca = '' or lower(p.marca) = :marca)
          and (:minPrecio is null or p.precio >= :minPrecio)
          and (:maxPrecio is null or p.precio <= :maxPrecio)
        """)
    Page<Producto> buscar(
            @Param("q") String q,
            @Param("categoria") String categoria,
            @Param("marca") String marca,
            @Param("minPrecio") BigDecimal minPrecio,
            @Param("maxPrecio") BigDecimal maxPrecio,
            Pageable pageable
    );

    List<Producto> findTop15ByActivoTrueAndDestacadoTrueOrderByActualizadoEnDesc();

    Optional<Producto> findBySkuIgnoreCase(String sku);

    Optional<Producto> findBySlugIgnoreCase(String slug);

    Optional<Producto> findByNombreIgnoreCaseAndMarcaIgnoreCase(String nombre, String marca);

    long countByActivoTrue();
}
