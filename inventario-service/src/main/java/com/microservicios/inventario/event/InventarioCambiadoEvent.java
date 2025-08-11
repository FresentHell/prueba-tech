package com.microservicios.inventario.event;

import com.microservicios.inventario.model.Inventario;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Evento que se emite cuando cambia el inventario de un producto.
 * Implementa el patrón Observer para notificar cambios de inventario.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
public class InventarioCambiadoEvent extends ApplicationEvent {

    private final Long productoId;
    private final Integer cantidadAnterior;
    private final Integer cantidadNueva;
    private final String tipoOperacion;
    private final LocalDateTime timestamp;

    /**
     * Constructor del evento.
     * 
     * @param source Objeto que originó el evento
     * @param productoId ID del producto
     * @param cantidadAnterior Cantidad anterior en inventario
     * @param cantidadNueva Nueva cantidad en inventario
     * @param tipoOperacion Tipo de operación realizada (COMPRA, AJUSTE, etc.)
     */
    public InventarioCambiadoEvent(Object source, Long productoId, Integer cantidadAnterior, 
                                 Integer cantidadNueva, String tipoOperacion) {
        super(source);
        this.productoId = productoId;
        this.cantidadAnterior = cantidadAnterior;
        this.cantidadNueva = cantidadNueva;
        this.tipoOperacion = tipoOperacion;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor alternativo que toma un objeto Inventario.
     * 
     * @param source Objeto que originó el evento
     * @param inventario Objeto Inventario
     * @param cantidadAnterior Cantidad anterior
     * @param tipoOperacion Tipo de operación
     */
    public InventarioCambiadoEvent(Object source, Inventario inventario, 
                                 Integer cantidadAnterior, String tipoOperacion) {
        this(source, inventario.getProductoId(), cantidadAnterior, 
             inventario.getCantidad(), tipoOperacion);
    }

    // Getters
    public Long getProductoId() {
        return productoId;
    }

    public Integer getCantidadAnterior() {
        return cantidadAnterior;
    }

    public Integer getCantidadNueva() {
        return cantidadNueva;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public LocalDateTime getEventTimestamp() {
        return timestamp;
    }

    /**
     * Calcula la diferencia en la cantidad.
     * 
     * @return Diferencia entre cantidad nueva y anterior
     */
    public Integer getDiferencia() {
        return cantidadNueva - cantidadAnterior;
    }

    /**
     * Verifica si el inventario aumentó.
     * 
     * @return true si aumentó, false en caso contrario
     */
    public boolean isAumento() {
        return cantidadNueva > cantidadAnterior;
    }

    /**
     * Verifica si el inventario disminuyó.
     * 
     * @return true si disminuyó, false en caso contrario
     */
    public boolean isDisminucion() {
        return cantidadNueva < cantidadAnterior;
    }

    @Override
    public String toString() {
        return "InventarioCambiadoEvent{" +
                "productoId=" + productoId +
                ", cantidadAnterior=" + cantidadAnterior +
                ", cantidadNueva=" + cantidadNueva +
                ", tipoOperacion='" + tipoOperacion + '\'' +
                ", eventTimestamp=" + timestamp +
                '}';
    }
}
