package com.parfum.jpa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "cliente_nombre", length = 100)
    private String clienteNombre;

    @Column(name = "cliente_correo", length = 160)
    private String clienteCorreo;

    @Column(name = "cliente_telefono", length = 30)
    private String clienteTelefono;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(name = "metodo_pago", nullable = false, length = 50)
    private String metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", length = 40)
    private EstadoPago estadoPago = EstadoPago.PENDIENTE_VERIFICACION;

    @Column(name = "numero_operacion", length = 80)
    private String numeroOperacion;

    @Column(name = "comprobante_url", length = 800)
    private String comprobanteUrl;

    @Column(name = "comprobante_public_id", length = 300)
    private String comprobantePublicId;

    @Column(name = "observacion_pago", length = 500)
    private String observacionPago;

    @Column(name = "pagado_en")
    private Instant pagadoEn;

    /**
     * null = pedido histórico creado antes de esta migración. Esos pedidos ya
     * descontaron stock al crearse, por eso null se interpreta como true.
     * Los pedidos nuevos se crean explícitamente con false y descuentan stock
     * únicamente cuando el pago pasa a CONFIRMADO.
     */
    @Column(name = "stock_aplicado")
    private Boolean stockAplicado;

    @Column(name = "guest_access_token_hash", length = 64)
    private String guestAccessTokenHash;

    @Column(name = "guest_access_expires_at")
    private Instant guestAccessExpiresAt;

    @Column(name = "direccion_entrega", nullable = false, length = 350)
    private String direccionEntrega;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetallePedido> detalles = new ArrayList<>();

    public void agregarDetalle(DetallePedido detalle) {
        detalle.setPedido(this);
        detalles.add(detalle);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteCorreo() { return clienteCorreo; }
    public void setClienteCorreo(String clienteCorreo) { this.clienteCorreo = clienteCorreo; }
    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public EstadoPago getEstadoPago() { return estadoPago; }
    public void setEstadoPago(EstadoPago estadoPago) { this.estadoPago = estadoPago; }
    public String getNumeroOperacion() { return numeroOperacion; }
    public void setNumeroOperacion(String numeroOperacion) { this.numeroOperacion = numeroOperacion; }
    public String getComprobanteUrl() { return comprobanteUrl; }
    public void setComprobanteUrl(String comprobanteUrl) { this.comprobanteUrl = comprobanteUrl; }
    public String getComprobantePublicId() { return comprobantePublicId; }
    public void setComprobantePublicId(String comprobantePublicId) { this.comprobantePublicId = comprobantePublicId; }
    public String getObservacionPago() { return observacionPago; }
    public void setObservacionPago(String observacionPago) { this.observacionPago = observacionPago; }
    public Instant getPagadoEn() { return pagadoEn; }
    public void setPagadoEn(Instant pagadoEn) { this.pagadoEn = pagadoEn; }
    public boolean isStockAplicado() { return stockAplicado == null || stockAplicado; }
    public Boolean getStockAplicadoRaw() { return stockAplicado; }
    public void setStockAplicado(boolean stockAplicado) { this.stockAplicado = stockAplicado; }
    public String getGuestAccessTokenHash() { return guestAccessTokenHash; }
    public void setGuestAccessTokenHash(String guestAccessTokenHash) { this.guestAccessTokenHash = guestAccessTokenHash; }
    public Instant getGuestAccessExpiresAt() { return guestAccessExpiresAt; }
    public void setGuestAccessExpiresAt(Instant guestAccessExpiresAt) { this.guestAccessExpiresAt = guestAccessExpiresAt; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) { this.detalles = detalles; }
}
