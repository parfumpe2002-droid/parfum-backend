package com.parfum.jpa.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "decant_envases")
public class DecantEnvase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    private Integer mililitros;

    @Column(length = 400)
    private String descripcion;

    @Column(name = "imagen_url", length = 600)
    private String imagenUrl;

    @Column(name = "imagen_public_id", length = 300)
    private String imagenPublicId;

    @Column(name = "fallback_image", length = 300)
    private String fallbackImage;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "orden_visual", nullable = false)
    private Integer ordenVisual = 0;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn = Instant.now();

    @PreUpdate
    public void preUpdate() { actualizadoEn = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getMililitros() { return mililitros; }
    public void setMililitros(Integer mililitros) { this.mililitros = mililitros; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getImagenPublicId() { return imagenPublicId; }
    public void setImagenPublicId(String imagenPublicId) { this.imagenPublicId = imagenPublicId; }
    public String getFallbackImage() { return fallbackImage; }
    public void setFallbackImage(String fallbackImage) { this.fallbackImage = fallbackImage; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Integer getOrdenVisual() { return ordenVisual; }
    public void setOrdenVisual(Integer ordenVisual) { this.ordenVisual = ordenVisual; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Instant actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
