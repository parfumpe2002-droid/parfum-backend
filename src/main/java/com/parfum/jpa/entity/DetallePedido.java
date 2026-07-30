package com.parfum.jpa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;
    @Column(name = "producto_id", nullable = false)
    private Long productoId;
    @Column(name = "presentacion_id")
    private Long presentacionId;
    @Column(name = "producto_decant_id")
    private Long productoDecantId;
    @Column(name = "tipo_item", length = 20)
    private String tipoItem = "BOTELLA";
    @Column(nullable = false)
    private boolean regalo = false;
    @Column(name = "mililitros")
    private Integer mililitros;
    @Column(name = "presentacion", length = 60)
    private String presentacion;
    @Column(name = "nombre_producto", nullable = false, length = 160)
    private String nombreProducto;
    @Column(name = "imagen_url", length = 600)
    private String imagenUrl;
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;
    @Column(nullable = false)
    private Integer cantidad;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public Long getPresentacionId() { return presentacionId; }
    public void setPresentacionId(Long presentacionId) { this.presentacionId = presentacionId; }
    public Long getProductoDecantId() { return productoDecantId; }
    public void setProductoDecantId(Long productoDecantId) { this.productoDecantId = productoDecantId; }
    public String getTipoItem() { return tipoItem; }
    public void setTipoItem(String tipoItem) { this.tipoItem = tipoItem; }
    public boolean isRegalo() { return regalo; }
    public void setRegalo(boolean regalo) { this.regalo = regalo; }
    public Integer getMililitros() { return mililitros; }
    public void setMililitros(Integer mililitros) { this.mililitros = mililitros; }
    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
