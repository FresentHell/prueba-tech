package com.microservicios.inventario.controller;

import com.microservicios.inventario.dto.*;
import com.microservicios.inventario.service.InventarioService;


import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el microservicio de inventario.
 * Implementa endpoints siguiendo el estándar JSON API.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/inventario")
@CrossOrigin(origins = "*")
public class InventarioController {

    private static final Logger logger = LoggerFactory.getLogger(InventarioController.class);

    private final InventarioService inventarioService;

    @Autowired
    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    /**
     * Consulta el inventario de un producto específico.
     * 
     * @param productoId ID del producto
     * @return ResponseEntity con la información del inventario
     */
    @GetMapping(value = "/{productoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InventarioResponse> consultarInventario(@PathVariable Long productoId) {
        logger.info("GET /inventario/{} - Consultando inventario", productoId);
        
        try {
            InventarioResponse response = inventarioService.consultarInventario(productoId);
            logger.info("Inventario consultado: {} unidades para producto ID: {}", 
                       response.getData().getAttributes().getCantidad(), productoId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Error al consultar inventario para producto ID {}: {}", productoId, e.getMessage());
            throw e;
        }
    }

    /**
     * Actualiza la cantidad disponible de un producto.
     * 
     * @param productoId ID del producto
     * @param request DTO con la nueva cantidad
     * @return ResponseEntity con el inventario actualizado
     */
    @PatchMapping(value = "/{productoId}", 
                 consumes = MediaType.APPLICATION_JSON_VALUE, 
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InventarioResponse> actualizarInventario(
            @PathVariable Long productoId, 
            @Valid @RequestBody InventarioRequest request) {
        logger.info("PATCH /inventario/{} - Actualizando inventario", productoId);
        
        try {
            InventarioResponse response = inventarioService.actualizarInventario(productoId, request);
            logger.info("Inventario actualizado: {} unidades para producto ID: {}", 
                       response.getData().getAttributes().getCantidad(), productoId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Error al actualizar inventario para producto ID {}: {}", productoId, e.getMessage());
            throw e;
        }
    }

    /**
     * Realiza una compra de productos.
     * Este es el endpoint principal del flujo de compra.
     * 
     * @param request DTO con los datos de la compra
     * @return ResponseEntity con los detalles de la compra realizada
     */
    @PostMapping(value = "/compras", 
                consumes = MediaType.APPLICATION_JSON_VALUE, 
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompraResponse> realizarCompra(@Valid @RequestBody CompraRequest request) {
        Long productoId = request.getData().getAttributes().getProductoId();
        Integer cantidad = request.getData().getAttributes().getCantidad();
        
        logger.info("POST /inventario/compras - Realizando compra: {} unidades del producto ID: {}", 
                   cantidad, productoId);
        
        try {
            CompraResponse response = inventarioService.realizarCompra(request);
            logger.info("Compra realizada exitosamente: {} unidades del producto '{}' por ${}", 
                       cantidad, response.getData().getAttributes().getNombreProducto(), 
                       response.getData().getAttributes().getPrecioTotal());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Error al realizar compra para producto ID {}: {}", productoId, e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene todos los inventarios.
     * 
     * @return ResponseEntity con la lista de inventarios
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InventarioResponse> obtenerTodosLosInventarios() {
        logger.info("GET /inventario - Obteniendo todos los inventarios");
        
        InventarioResponse response = inventarioService.obtenerTodosLosInventarios();
        logger.info("Se obtuvieron {} inventarios", response.getDataList().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene productos con inventario bajo.
     * 
     * @param cantidadMinima Cantidad mínima para considerar inventario bajo
     * @return ResponseEntity con los inventarios bajos
     */
    @GetMapping(value = "/bajos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InventarioResponse> obtenerInventariosBajos(
            @RequestParam(defaultValue = "10") Integer cantidadMinima) {
        logger.info("GET /inventario/bajos?cantidadMinima={} - Obteniendo inventarios bajos", cantidadMinima);
        
        InventarioResponse response = inventarioService.obtenerInventariosBajos(cantidadMinima);
        logger.info("Se encontraron {} productos con inventario bajo", response.getDataList().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene productos sin inventario.
     * 
     * @return ResponseEntity con los inventarios sin stock
     */
    @GetMapping(value = "/sin-stock", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InventarioResponse> obtenerProductosSinInventario() {
        logger.info("GET /inventario/sin-stock - Obteniendo productos sin inventario");
        
        InventarioResponse response = inventarioService.obtenerProductosSinInventario();
        logger.info("Se encontraron {} productos sin inventario", response.getDataList().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Crea un nuevo registro de inventario.
     * 
     * @param productoId ID del producto
     * @param cantidad Cantidad inicial
     * @return ResponseEntity con el inventario creado
     */
    @PostMapping(value = "/{productoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InventarioResponse> crearInventario(
            @PathVariable Long productoId, 
            @RequestParam Integer cantidad) {
        logger.info("POST /inventario/{}?cantidad={} - Creando inventario", productoId, cantidad);
        
        try {
            InventarioResponse response = inventarioService.crearInventario(productoId, cantidad);
            logger.info("Inventario creado exitosamente: {} unidades para producto ID: {}", 
                       cantidad, productoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Error al crear inventario para producto ID {}: {}", productoId, e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene estadísticas del inventario.
     * 
     * @return ResponseEntity con las estadísticas del inventario
     */
    @GetMapping(value = "/estadisticas", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> obtenerEstadisticasInventario() {
        logger.info("GET /inventario/estadisticas - Obteniendo estadísticas del inventario");
        
        String estadisticas = inventarioService.obtenerEstadisticasInventario();
        logger.info("Estadísticas obtenidas exitosamente");
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Health check endpoint.
     * 
     * @return ResponseEntity con el estado del servicio
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> health() {
        logger.debug("GET /inventario/health - Health check");
        return ResponseEntity.ok("{\"status\": \"UP\", \"service\": \"inventario-service\"}");
    }
}
