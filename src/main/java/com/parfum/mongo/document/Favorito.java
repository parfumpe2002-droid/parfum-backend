package com.parfum.mongo.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("favoritos_variantes")
@CompoundIndex(name = "favorito_usuario_variante", def = "{'usuarioId': 1, 'varianteClave': 1}", unique = true)
public class Favorito {
    @Id private String id;
    private Long usuarioId;
    private Long productoId;
    private Long presentacionId;
    private Long productoDecantId;
    private String tipoItem;
    private String varianteClave;
    private Integer mililitros;
    private String presentacion;
    private String nombre;
    private String marca;
    private Double precio;
    private Integer stock;
    private String imagenUrl;
    private Instant creadoEn = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public Long getPresentacionId() { return presentacionId; }
    public void setPresentacionId(Long presentacionId) { this.presentacionId = presentacionId; }
    public Long getProductoDecantId() { return productoDecantId; }
    public void setProductoDecantId(Long productoDecantId) { this.productoDecantId = productoDecantId; }
    public String getTipoItem() { return tipoItem; }
    public void setTipoItem(String tipoItem) { this.tipoItem = tipoItem; }
    public String getVarianteClave() { return varianteClave; }
    public void setVarianteClave(String varianteClave) { this.varianteClave = varianteClave; }
    public Integer getMililitros() { return mililitros; }
    public void setMililitros(Integer mililitros) { this.mililitros = mililitros; }
    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
