package com.microservicios.inventario.service;

import com.microservicios.inventario.client.ProductosClient;
import com.microservicios.inventario.dto.*;
import com.microservicios.inventario.event.InventarioCambiadoEvent;
import com.microservicios.inventario.model.HistorialCompras;
import com.microservicios.inventario.model.Inventario;
import com.microservicios.inventario.repository.HistorialComprasRepository;
import com.microservicios.inventario.repository.InventarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Servicio que maneja la lógica de negocio para inventario.
 * Implementa el patrón Service Layer y el flujo de compras.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@Service
@Transactional
public class InventarioService {

    private static final Logger logger = LoggerFactory.getLogger(InventarioService.class);

    private final InventarioRepository inventarioRepository;
    private final HistorialComprasRepository historialComprasRepository;
    private final ProductosClient productosClient;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public InventarioService(InventarioRepository inventarioRepository,
                           HistorialComprasRepository historialComprasRepository,
                           ProductosClient productosClient,
                           ApplicationEventPublisher eventPublisher) {
        this.inventarioRepository = inventarioRepository;
        this.historialComprasRepository = historialComprasRepository;
        this.productosClient = productosClient;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Consulta el inventario de un producto específico.
     * 
     * @param productoId ID del producto
     * @return InventarioResponse con la información del inventario
     * @throws IllegalArgumentException si el producto no existe
     */
    @Transactional(readOnly = true)
    public InventarioResponse consultarInventario(Long productoId) {
        logger.info("Consultando inventario para producto ID: {}", productoId);

        Optional<Inventario> inventarioOpt = inventarioRepository.findByProductoId(productoId);
        
        if (inventarioOpt.isEmpty()) {
            logger.warn("Inventario no encontrado para producto ID: {}", productoId);
            throw new IllegalArgumentException("Inventario no encontrado para el producto ID: " + productoId);
        }

        Inventario inventario = inventarioOpt.get();
        logger.info("Inventario encontrado: {} unidades para producto ID: {}", 
                   inventario.getCantidad(), productoId);

        return InventarioResponse.fromInventario(inventario);
    }

    /**
     * Actualiza la cantidad disponible de un producto.
     * 
     * @param productoId ID del producto
     * @param request DTO con la nueva cantidad
     * @return InventarioResponse con el inventario actualizado
     * @throws IllegalArgumentException si el producto no existe
     */
    public InventarioResponse actualizarInventario(Long productoId, InventarioRequest request) {
        Integer nuevaCantidad = request.getData().getAttributes().getCantidad();
        
        // Validar cantidad no negativa
        if (nuevaCantidad < 0) {
            logger.error("Cantidad inválida para actualización: {}. No puede ser negativa", nuevaCantidad);
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        
        logger.info("Actualizando inventario para producto ID: {} con cantidad: {}", 
                   productoId, nuevaCantidad);

        Optional<Inventario> inventarioOpt = inventarioRepository.findByProductoId(productoId);
        
        if (inventarioOpt.isEmpty()) {
            logger.warn("Inventario no encontrado para actualizar, producto ID: {}", productoId);
            throw new IllegalArgumentException("Inventario no encontrado para el producto ID: " + productoId);
        }

        Inventario inventario = inventarioOpt.get();
        Integer cantidadAnterior = inventario.getCantidad();

        // Actualizar cantidad
        inventario.setCantidad(nuevaCantidad);
        Inventario inventarioActualizado = inventarioRepository.save(inventario);

        // Emitir evento de cambio de inventario
        eventPublisher.publishEvent(new InventarioCambiadoEvent(
            this, inventarioActualizado, cantidadAnterior, "AJUSTE"
        ));

        logger.info("Inventario actualizado: {} -> {} unidades para producto ID: {}", 
                   cantidadAnterior, nuevaCantidad, productoId);

        return InventarioResponse.fromInventario(inventarioActualizado);
    }

    /**
     * Realiza una compra de productos.
     * Este es el endpoint principal del flujo de compra.
     * 
     * @param request DTO con los datos de la compra
     * @return CompraResponse con los detalles de la compra realizada
     * @throws IllegalArgumentException si el producto no existe o no hay suficiente inventario
     */
    public CompraResponse realizarCompra(CompraRequest request) {
        Long productoId = request.getData().getAttributes().getProductoId();
        Integer cantidadSolicitada = request.getData().getAttributes().getCantidad();

        // Validar cantidad positiva
        if (cantidadSolicitada <= 0) {
            logger.error("Cantidad inválida para compra: {}. Debe ser mayor a 0", cantidadSolicitada);
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }

        logger.info("Iniciando compra: {} unidades del producto ID: {}", cantidadSolicitada, productoId);

        // 1. Verificar que el producto existe
        ProductoInfo productoInfo = productosClient.obtenerProducto(productoId)
                .block(); // Bloquear para operación síncrona

        if (productoInfo == null || productoInfo.getData() == null) {
            logger.error("Producto no encontrado con ID: {}", productoId);
            throw new IllegalArgumentException("Producto no encontrado con ID: " + productoId);
        }

        // 2. Verificar disponibilidad en inventario
        Optional<Inventario> inventarioOpt = inventarioRepository.findByProductoId(productoId);
        
        if (inventarioOpt.isEmpty()) {
            logger.error("Inventario no encontrado para producto ID: {}", productoId);
            throw new IllegalArgumentException("Inventario no encontrado para el producto ID: " + productoId);
        }

        Inventario inventario = inventarioOpt.get();
        
        if (!inventario.tieneStockSuficiente(cantidadSolicitada)) {
            logger.error("Inventario insuficiente para producto ID: {}. Disponible: {}, Solicitado: {}", 
                        productoId, inventario.getCantidad(), cantidadSolicitada);
            throw new IllegalArgumentException(
                "Inventario insuficiente. Disponible: " + inventario.getCantidad() + 
                ", Solicitado: " + cantidadSolicitada
            );
        }

        // 3. Actualizar inventario
        Integer cantidadAnterior = inventario.getCantidad();
        inventario.reducirInventario(cantidadSolicitada);
        Inventario inventarioActualizado = inventarioRepository.save(inventario);

        // 4. Registrar compra en historial
        BigDecimal precioUnitario = productoInfo.getData().getAttributes().getPrecio();
        BigDecimal precioTotal = precioUnitario.multiply(BigDecimal.valueOf(cantidadSolicitada));
        String nombreProducto = productoInfo.getData().getAttributes().getNombre();

        HistorialCompras historialCompra = new HistorialCompras(
            productoId, cantidadSolicitada, precioUnitario, precioTotal, nombreProducto
        );
        HistorialCompras compraGuardada = historialComprasRepository.save(historialCompra);

        // 5. Emitir evento de cambio de inventario
        eventPublisher.publishEvent(new InventarioCambiadoEvent(
            this, inventarioActualizado, cantidadAnterior, "COMPRA"
        ));

        logger.info("Compra realizada exitosamente: {} unidades del producto '{}' por ${}", 
                   cantidadSolicitada, nombreProducto, precioTotal);

        return CompraResponse.fromHistorialCompras(compraGuardada, inventarioActualizado.getCantidad());
    }

    /**
     * Obtiene todos los inventarios.
     * 
     * @return InventarioResponse con la lista de inventarios
     */
    @Transactional(readOnly = true)
    public InventarioResponse obtenerTodosLosInventarios() {
        logger.info("Obteniendo todos los inventarios");

        List<Inventario> inventarios = inventarioRepository.findAllOrderByFechaActualizacionDesc();
        logger.info("Se encontraron {} inventarios", inventarios.size());

        return InventarioResponse.fromInventarios(inventarios);
    }

    /**
     * Obtiene productos con inventario bajo.
     * 
     * @param cantidadMinima Cantidad mínima para considerar inventario bajo
     * @return InventarioResponse con los inventarios bajos
     */
    @Transactional(readOnly = true)
    public InventarioResponse obtenerInventariosBajos(Integer cantidadMinima) {
        logger.info("Obteniendo productos con inventario bajo (menos de {} unidades)", cantidadMinima);

        List<Inventario> inventariosBajos = inventarioRepository.findByCantidadLessThan(cantidadMinima);
        logger.info("Se encontraron {} productos con inventario bajo", inventariosBajos.size());

        return InventarioResponse.fromInventarios(inventariosBajos);
    }

    /**
     * Obtiene productos sin inventario.
     * 
     * @return InventarioResponse con los inventarios sin stock
     */
    @Transactional(readOnly = true)
    public InventarioResponse obtenerProductosSinInventario() {
        logger.info("Obteniendo productos sin inventario");

        List<Inventario> productosSinStock = inventarioRepository.findByCantidadZero();
        logger.info("Se encontraron {} productos sin inventario", productosSinStock.size());

        return InventarioResponse.fromInventarios(productosSinStock);
    }

    /**
     * Crea un nuevo registro de inventario.
     * 
     * @param productoId ID del producto
     * @param cantidad Cantidad inicial
     * @return InventarioResponse con el inventario creado
     */
    public InventarioResponse crearInventario(Long productoId, Integer cantidad) {
        // Validar cantidad no negativa
        if (cantidad < 0) {
            logger.error("Cantidad inválida para crear inventario: {}. No puede ser negativa", cantidad);
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        
        logger.info("Creando inventario para producto ID: {} con cantidad: {}", productoId, cantidad);

        // Verificar que no exista ya un inventario para este producto
        if (inventarioRepository.existsByProductoId(productoId)) {
            logger.warn("Ya existe inventario para producto ID: {}", productoId);
            throw new IllegalArgumentException("Ya existe inventario para el producto ID: " + productoId);
        }

        Inventario inventario = new Inventario(productoId, cantidad);
        Inventario inventarioGuardado = inventarioRepository.save(inventario);

        // Emitir evento de cambio de inventario
        eventPublisher.publishEvent(new InventarioCambiadoEvent(
            this, inventarioGuardado, 0, "CREACION"
        ));

        logger.info("Inventario creado exitosamente para producto ID: {}", productoId);

        return InventarioResponse.fromInventario(inventarioGuardado);
    }

    /**
     * Obtiene estadísticas del inventario.
     * 
     * @return String con estadísticas del inventario
     */
    @Transactional(readOnly = true)
    public String obtenerEstadisticasInventario() {
        long totalProductos = inventarioRepository.countInventarios();
        Integer cantidadTotal = inventarioRepository.sumCantidadTotal();
        long productosSinStock = inventarioRepository.findByCantidadZero().size();

        return String.format(
            "Estadísticas del Inventario:\n" +
            "- Total de productos: %d\n" +
            "- Cantidad total en inventario: %d\n" +
            "- Productos sin stock: %d",
            totalProductos, cantidadTotal, productosSinStock
        );
    }
}
