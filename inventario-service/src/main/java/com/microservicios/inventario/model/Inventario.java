package com.microservicios.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad que representa el inventario de un producto.
 * 
 * Esta entidad contiene la información de inventario:
 * - producto_id: ID del producto (obligatorio)
 * - cantidad: Cantidad disponible en inventario (obligatorio, debe ser >= 0)
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@Entity
@Table(name = "inventario")
@EntityListeners(AuditingEntityListener.class)
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El ID del producto es obligatorio")
    @Column(name = "producto_id", nullable = false, unique = true)
    private Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @PositiveOrZero(message = "La cantidad debe ser mayor o igual a cero")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Version
    @Column(name = "version")
    private Long version;

    // Constructores
    public Inventario() {
    }

    public Inventario(Long productoId, Integer cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Verifica si hay suficiente inventario para una compra.
     * 
     * @param cantidadSolicitada Cantidad que se quiere comprar
     * @return true si hay suficiente inventario, false en caso contrario
     */
    public boolean tieneStockSuficiente(Integer cantidadSolicitada) {
        return this.cantidad >= cantidadSolicitada;
    }

    /**
     * Reduce el inventario por la cantidad especificada.
     * 
     * @param cantidadAReducir Cantidad a reducir del inventario
     * @throws IllegalArgumentException si no hay suficiente inventario
     */
    public void reducirInventario(Integer cantidadAReducir) {
        if (!tieneStockSuficiente(cantidadAReducir)) {
            throw new IllegalArgumentException(
                "No hay suficiente inventario. Disponible: " + this.cantidad + 
                ", Solicitado: " + cantidadAReducir
            );
        }
        this.cantidad -= cantidadAReducir;
    }

    /**
     * Aumenta el inventario por la cantidad especificada.
     * 
     * @param cantidadAAumentar Cantidad a aumentar al inventario
     */
    public void aumentarInventario(Integer cantidadAAumentar) {
        if (cantidadAAumentar < 0) {
            throw new IllegalArgumentException("La cantidad a aumentar debe ser positiva");
        }
        this.cantidad += cantidadAAumentar;
    }

    @Override
    public String toString() {
        return "Inventario{" +
                "id=" + id +
                ", productoId=" + productoId +
                ", cantidad=" + cantidad +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaActualizacion=" + fechaActualizacion +
                ", version=" + version +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Inventario that = (Inventario) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
