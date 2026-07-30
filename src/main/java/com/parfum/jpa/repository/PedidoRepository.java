package com.parfum.jpa.repository;

import com.parfum.jpa.entity.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @EntityGraph(attributePaths = {"detalles", "usuario"})
    List<Pedido> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);

    @Override
    @EntityGraph(attributePaths = {"detalles", "usuario"})
    Optional<Pedido> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"detalles", "usuario"})
    List<Pedido> findAll();
}
