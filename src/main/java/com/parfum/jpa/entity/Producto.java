package com.parfum.jpa.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40)
    private String sku;

    @Column(length = 180)
    private String slug;

    @Column(nullable = false, length = 140)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String marca;

    @Column(nullable = false, length = 60)
    private String categoria;

    @Column(length = 30)
    private String genero = "Hombre";

    @Column(length = 80)
    private String concentracion;

    @Column(length = 2000)
    private String descripcion;

    @Column(name = "familia_olfativa", length = 120)
    private String familiaOlfativa;

    @Column(name = "ano_lanzamiento")
    private Integer anoLanzamiento;

    @Column(length = 300)
    private String perfumista;

    @Column(name = "notas_salida", length = 1200)
    private String notasSalida;

    @Column(name = "notas_corazon", length = 1200)
    private String notasCorazon;

    @Column(name = "notas_fondo", length = 1200)
    private String notasFondo;

    @Column(name = "acordes_principales", length = 1200)
    private String acordesPrincipales;

    @Column(length = 160)
    private String duracion;

    @Column(length = 220)
    private String proyeccion;

    @Column(length = 300)
    private String estaciones;

    @Column(length = 400)
    private String ocasiones;

    @Column(length = 500)
    private String estilo;

    @Column(name = "fuente_referencia", length = 700)
    private String fuenteReferencia;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "imagen_url", length = 600)
    private String imagenUrl;

    @Column(name = "imagen_public_id", length = 300)
    private String imagenPublicId;

    @Column(name = "fallback_image", length = 300)
    private String fallbackImage;

    @Column(nullable = false)
    private boolean destacado;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "decant_disponible", nullable = false)
    private boolean decantDisponible = true;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("ordenVisual ASC, mililitros ASC")
    private List<ProductoPresentacion> presentaciones = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("ordenVisual ASC, id ASC")
    private List<ProductoDecant> decants = new ArrayList<>();

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn = Instant.now();

    @PreUpdate
    public void preUpdate() { actualizadoEn = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getConcentracion() { return concentracion; }
    public void setConcentracion(String concentracion) { this.concentracion = concentracion; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getFamiliaOlfativa() { return familiaOlfativa; }
    public void setFamiliaOlfativa(String familiaOlfativa) { this.familiaOlfativa = familiaOlfativa; }
    public Integer getAnoLanzamiento() { return anoLanzamiento; }
    public void setAnoLanzamiento(Integer anoLanzamiento) { this.anoLanzamiento = anoLanzamiento; }
    public String getPerfumista() { return perfumista; }
    public void setPerfumista(String perfumista) { this.perfumista = perfumista; }
    public String getNotasSalida() { return notasSalida; }
    public void setNotasSalida(String notasSalida) { this.notasSalida = notasSalida; }
    public String getNotasCorazon() { return notasCorazon; }
    public void setNotasCorazon(String notasCorazon) { this.notasCorazon = notasCorazon; }
    public String getNotasFondo() { return notasFondo; }
    public void setNotasFondo(String notasFondo) { this.notasFondo = notasFondo; }
    public String getAcordesPrincipales() { return acordesPrincipales; }
    public void setAcordesPrincipales(String acordesPrincipales) { this.acordesPrincipales = acordesPrincipales; }
    public String getDuracion() { return duracion; }
    public void setDuracion(String duracion) { this.duracion = duracion; }
    public String getProyeccion() { return proyeccion; }
    public void setProyeccion(String proyeccion) { this.proyeccion = proyeccion; }
    public String getEstaciones() { return estaciones; }
    public void setEstaciones(String estaciones) { this.estaciones = estaciones; }
    public String getOcasiones() { return ocasiones; }
    public void setOcasiones(String ocasiones) { this.ocasiones = ocasiones; }
    public String getEstilo() { return estilo; }
    public void setEstilo(String estilo) { this.estilo = estilo; }
    public String getFuenteReferencia() { return fuenteReferencia; }
    public void setFuenteReferencia(String fuenteReferencia) { this.fuenteReferencia = fuenteReferencia; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public String getImagenPublicId() { return imagenPublicId; }
    public void setImagenPublicId(String imagenPublicId) { this.imagenPublicId = imagenPublicId; }
    public String getFallbackImage() { return fallbackImage; }
    public void setFallbackImage(String fallbackImage) { this.fallbackImage = fallbackImage; }
    public boolean isDestacado() { return destacado; }
    public void setDestacado(boolean destacado) { this.destacado = destacado; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public boolean isDecantDisponible() { return decantDisponible; }
    public void setDecantDisponible(boolean decantDisponible) { this.decantDisponible = decantDisponible; }
    public List<ProductoPresentacion> getPresentaciones() { return presentaciones; }
    public void setPresentaciones(List<ProductoPresentacion> presentaciones) {
        this.presentaciones.clear();
        if (presentaciones != null) presentaciones.forEach(this::agregarPresentacion);
    }
    public void agregarPresentacion(ProductoPresentacion presentacion) {
        presentacion.setProducto(this);
        presentaciones.add(presentacion);
    }
    public void limpiarPresentaciones() { presentaciones.clear(); }
    public List<ProductoDecant> getDecants() { return decants; }
    public void setDecants(List<ProductoDecant> decants) {
        this.decants.clear();
        if (decants != null) decants.forEach(this::agregarDecant);
    }
    public void agregarDecant(ProductoDecant decant) {
        decant.setProducto(this);
        decants.add(decant);
    }
    public void limpiarDecants() { decants.clear(); }
    public void recalcularResumenComercial() {
        List<ProductoPresentacion> disponibles = presentaciones.stream()
                .filter(ProductoPresentacion::isActivo)
                .sorted(Comparator.comparing(ProductoPresentacion::getMililitros, Comparator.nullsLast(Integer::compareTo)))
                .toList();
        if (presentaciones.isEmpty()) return;
        if (disponibles.isEmpty()) {
            this.stock = 0;
            this.precio = BigDecimal.ZERO;
            return;
        }
        this.stock = disponibles.stream()
                .map(ProductoPresentacion::getStock)
                .filter(value -> value != null && value > 0)
                .mapToInt(Integer::intValue)
                .sum();
        this.precio = disponibles.stream()
                .map(ProductoPresentacion::getPrecio)
                .filter(value -> value != null && value.signum() > 0)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Instant actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
