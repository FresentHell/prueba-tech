package com.microservicios.inventario.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener de eventos para cambios en el inventario.
 * Implementa el patrón Observer para procesar eventos de manera asíncrona.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@Component
public class InventarioEventListener {

    private static final Logger logger = LoggerFactory.getLogger(InventarioEventListener.class);

    /**
     * Escucha eventos de cambio de inventario.
     * 
     * @param event Evento de cambio de inventario
     */
    @EventListener
    @Async
    public void handleInventarioCambiadoEvent(InventarioCambiadoEvent event) {
        logger.info("Evento de cambio de inventario procesado:");
        logger.info("  - Producto ID: {}", event.getProductoId());
        logger.info("  - Cantidad anterior: {}", event.getCantidadAnterior());
        logger.info("  - Cantidad nueva: {}", event.getCantidadNueva());
        logger.info("  - Tipo de operación: {}", event.getTipoOperacion());
        logger.info("  - Timestamp: {}", event.getTimestamp());

        // Aquí se pueden implementar acciones adicionales como:
        // - Notificaciones a otros servicios
        // - Actualización de caché
        // - Envío de alertas
        // - Registro en sistemas de auditoría
        // - Integración con sistemas externos

        procesarCambioInventario(event);
    }

    /**
     * Procesa el cambio de inventario según el tipo de operación.
     * 
     * @param event Evento de cambio de inventario
     */
    private void procesarCambioInventario(InventarioCambiadoEvent event) {
        switch (event.getTipoOperacion()) {
            case "COMPRA":
                procesarCompra(event);
                break;
            case "AJUSTE":
                procesarAjuste(event);
                break;
            case "CREACION":
                procesarCreacion(event);
                break;
            default:
                logger.warn("Tipo de operación no reconocido: {}", event.getTipoOperacion());
        }
    }

    /**
     * Procesa eventos de compra.
     * 
     * @param event Evento de compra
     */
    private void procesarCompra(InventarioCambiadoEvent event) {
        logger.info("Procesando evento de compra para producto ID: {}", event.getProductoId());
        
        // Verificar si el inventario quedó bajo después de la compra
        if (event.getCantidadNueva() <= 5) {
            logger.warn("¡ALERTA! Inventario bajo después de compra. Producto ID: {}, Cantidad restante: {}", 
                       event.getProductoId(), event.getCantidadNueva());
            
            // Aquí se podría enviar una notificación al sistema de alertas
            // enviarAlertaInventarioBajo(event.getProductoId(), event.getCantidadNueva());
        }

        // Registrar métricas de venta
        registrarMetricasVenta(event);
    }

    /**
     * Procesa eventos de ajuste de inventario.
     * 
     * @param event Evento de ajuste
     */
    private void procesarAjuste(InventarioCambiadoEvent event) {
        logger.info("Procesando evento de ajuste para producto ID: {}", event.getProductoId());
        
        // Verificar si el ajuste fue significativo
        int diferencia = Math.abs(event.getDiferencia());
        if (diferencia > 50) {
            logger.warn("Ajuste significativo de inventario detectado. Producto ID: {}, Diferencia: {}", 
                       event.getProductoId(), diferencia);
            
            // Aquí se podría enviar una notificación de ajuste significativo
            // enviarAlertaAjusteSignificativo(event.getProductoId(), diferencia);
        }
    }

    /**
     * Procesa eventos de creación de inventario.
     * 
     * @param event Evento de creación
     */
    private void procesarCreacion(InventarioCambiadoEvent event) {
        logger.info("Procesando evento de creación de inventario para producto ID: {}", event.getProductoId());
        
        // Aquí se podría enviar una notificación de nuevo producto en inventario
        // enviarNotificacionNuevoProducto(event.getProductoId(), event.getCantidadNueva());
    }

    /**
     * Registra métricas de venta.
     * 
     * @param event Evento de compra
     */
    private void registrarMetricasVenta(InventarioCambiadoEvent event) {
        // Aquí se podrían registrar métricas para análisis de ventas
        logger.debug("Registrando métricas de venta para producto ID: {}", event.getProductoId());
        
        // Ejemplo de métricas que se podrían registrar:
        // - Cantidad vendida
        // - Frecuencia de compras
        // - Patrones de venta por hora/día
        // - Productos más vendidos
    }

    /**
     * Envía alerta de inventario bajo (método placeholder).
     * 
     * @param productoId ID del producto
     * @param cantidadRestante Cantidad restante en inventario
     */
    private void enviarAlertaInventarioBajo(Long productoId, Integer cantidadRestante) {
        // Implementación placeholder para envío de alertas
        logger.warn("ALERTA: Inventario bajo - Producto ID: {}, Cantidad restante: {}", 
                   productoId, cantidadRestante);
    }

    /**
     * Envía alerta de ajuste significativo (método placeholder).
     * 
     * @param productoId ID del producto
     * @param diferencia Diferencia en el ajuste
     */
    private void enviarAlertaAjusteSignificativo(Long productoId, Integer diferencia) {
        // Implementación placeholder para envío de alertas
        logger.warn("ALERTA: Ajuste significativo - Producto ID: {}, Diferencia: {}", 
                   productoId, diferencia);
    }

    /**
     * Envía notificación de nuevo producto (método placeholder).
     * 
     * @param productoId ID del producto
     * @param cantidadInicial Cantidad inicial en inventario
     */
    private void enviarNotificacionNuevoProducto(Long productoId, Integer cantidadInicial) {
        // Implementación placeholder para notificaciones
        logger.info("NOTIFICACIÓN: Nuevo producto en inventario - Producto ID: {}, Cantidad inicial: {}", 
                   productoId, cantidadInicial);
    }
}
