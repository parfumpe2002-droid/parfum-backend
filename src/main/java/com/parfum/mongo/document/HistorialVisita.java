package com.parfum.mongo.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("historial")
public class HistorialVisita {
    @Id private String id;
    private Long usuarioId;
    private Long productoId;
    private String nombre;
    private String marca;
    private String imagenUrl;
    private Instant vistoEn = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public Instant getVistoEn() { return vistoEn; }
    public void setVistoEn(Instant vistoEn) { this.vistoEn = vistoEn; }
}
