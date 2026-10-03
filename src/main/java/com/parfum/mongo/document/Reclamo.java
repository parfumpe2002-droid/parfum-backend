package com.parfum.mongo.document;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("reclamos")
public class Reclamo {
    @Id private String id;
    private String tipo;
    private String nombre;
    private String documento;
    private String correo;
    private String telefono;
    private String detalle;
    private String pedidoReferencia;
    private String pedidoConsumidor;
    private String estado = "RECIBIDO";
    private Instant creadoEn = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getPedidoReferencia() { return pedidoReferencia; }
    public void setPedidoReferencia(String pedidoReferencia) { this.pedidoReferencia = pedidoReferencia; }
    public String getPedidoConsumidor() { return pedidoConsumidor; }
    public void setPedidoConsumidor(String pedidoConsumidor) { this.pedidoConsumidor = pedidoConsumidor; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
}
