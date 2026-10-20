package com.parfum.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ComboComponente {
    @Column(name = "producto_id", nullable = false)
    private Long productoId;
    @Column(name = "variante_id")
    private Long varianteId;
    @Column(name = "tipo_item", nullable = false, length = 12)
    private String tipoItem;
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    public ComboComponente() {}
    public ComboComponente(Long productoId, Long varianteId, String tipoItem, Integer cantidad) {
        this.productoId = productoId;
        this.varianteId = varianteId;
        this.tipoItem = tipoItem;
        this.cantidad = cantidad;
    }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long value) { productoId = value; }
    public Long getVarianteId() { return varianteId; }
    public void setVarianteId(Long value) { varianteId = value; }
    public String getTipoItem() { return tipoItem; }
    public void setTipoItem(String value) { tipoItem = value; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer value) { cantidad = value; }
}
