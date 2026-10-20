package com.parfum.jpa.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "combos")
public class Combo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 1200)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio = BigDecimal.ZERO;

    @Column(name = "imagen_url", length = 600)
    private String imagenUrl;

    @Column(nullable = false)
    private boolean publicado = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "combo_componentes", joinColumns = @JoinColumn(name = "combo_id"))
    @OrderColumn(name = "orden")
    private List<ComboComponente> componentes = new ArrayList<>();

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn = Instant.now();

    @PreUpdate
    void onUpdate() { actualizadoEn = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long value) { id = value; }
    public String getNombre() { return nombre; }
    public void setNombre(String value) { nombre = value; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String value) { descripcion = value; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal value) { precio = value; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String value) { imagenUrl = value; }
    public boolean isPublicado() { return publicado; }
    public void setPublicado(boolean value) { publicado = value; }
    public List<ComboComponente> getComponentes() { return componentes; }
    public void setComponentes(List<ComboComponente> values) {
        componentes.clear();
        if (values != null) componentes.addAll(values);
    }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
}
