package com.parfum.jpa.repository;

import com.parfum.jpa.entity.Pedido;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @EntityGraph(attributePaths = {"detalles", "usuario"})
    List<Pedido> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);

    @Override
    @EntityGraph(attributePaths = {"detalles", "usuario"})
    Optional<Pedido> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"detalles", "usuario"})
    List<Pedido> findAll();

    @Query("""
        select coalesce(sum(p.total), 0)
        from Pedido p
        where p.estadoPago = com.parfum.jpa.entity.EstadoPago.CONFIRMADO
          and p.estado <> com.parfum.jpa.entity.EstadoPedido.CANCELADO
        """)
    BigDecimal sumVentasConfirmadas();

    @Query("""
        select count(p)
        from Pedido p
        where p.estadoPago is null
           or p.estadoPago = com.parfum.jpa.entity.EstadoPago.PENDIENTE_VERIFICACION
        """)
    long countPagosPendientes();
}
