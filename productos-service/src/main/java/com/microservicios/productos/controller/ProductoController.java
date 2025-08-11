package com.microservicios.productos.controller;

import com.microservicios.productos.dto.ProductoRequest;
import com.microservicios.productos.dto.ProductoResponse;
import com.microservicios.productos.service.ProductoService;


import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Controlador REST para el microservicio de productos.
 * Implementa endpoints siguiendo el estándar JSON API.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

    private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Crea un nuevo producto.
     * 
     * @param request DTO con los datos del producto
     * @return ResponseEntity con el producto creado
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, 
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductoResponse> crearProducto(
        @Valid @RequestBody ProductoRequest request) {
        logger.info("POST /productos - Creando nuevo producto");
        
        try {
            ProductoResponse response = productoService.crearProducto(request);
            if (response != null && response.getData() != null) {
                logger.info("Producto creado exitosamente con ID: {}", response.getData().getId());
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Error al crear producto: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene un producto por su ID.
     * 
     * @param id ID del producto
     * @return ResponseEntity con el producto encontrado
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductoResponse> obtenerProductoPorId(
        @PathVariable Long id) {
        logger.info("GET /productos/{} - Obteniendo producto por ID", id);
        
        try {
            ProductoResponse response = productoService.obtenerProductoPorId(id);
            if (response != null && response.getData() != null && response.getData().getAttributes() != null) {
                logger.info("Producto encontrado: {}", response.getData().getAttributes().getNombre());
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Error al obtener producto con ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Obtiene todos los productos.
     * 
     * @return ResponseEntity con la lista de productos
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductoResponse> obtenerTodosLosProductos() {
        logger.info("GET /productos - Obteniendo todos los productos");
        
        ProductoResponse response = productoService.obtenerTodosLosProductos();
        logger.info("Se obtuvieron {} productos", response.getDataList().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Busca productos por nombre.
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @return ResponseEntity con los productos encontrados
     */
    @GetMapping(value = "/buscar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductoResponse> buscarProductosPorNombre(@RequestParam String nombre) {
        logger.info("GET /productos/buscar?nombre={} - Buscando productos por nombre", nombre);
        
        ProductoResponse response = productoService.buscarProductosPorNombre(nombre);
        logger.info("Se encontraron {} productos con el nombre: {}", 
                   response.getDataList().size(), nombre);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca productos por rango de precios.
     * 
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return ResponseEntity con los productos encontrados
     */
    @GetMapping(value = "/precio", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductoResponse> buscarProductosPorPrecio(
            @RequestParam BigDecimal precioMin, 
            @RequestParam BigDecimal precioMax) {
        logger.info("GET /productos/precio?precioMin={}&precioMax={} - Buscando productos por precio", 
                   precioMin, precioMax);
        
        ProductoResponse response = productoService.buscarProductosPorPrecio(precioMin, precioMax);
        logger.info("Se encontraron {} productos en el rango de precios", 
                   response.getDataList().size());
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza un producto existente.
     * 
     * @param id ID del producto a actualizar
     * @param request DTO con los nuevos datos del producto
     * @return ResponseEntity con el producto actualizado
     */
    @PutMapping(value = "/{id}", 
               consumes = MediaType.APPLICATION_JSON_VALUE, 
               produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable Long id, 
            @Valid @RequestBody ProductoRequest request) {
        logger.info("PUT /productos/{} - Actualizando producto", id);
        
        try {
            ProductoResponse response = productoService.actualizarProducto(id, request);
            logger.info("Producto actualizado exitosamente: {}", 
                       response.getData().getAttributes().getNombre());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Error al actualizar producto con ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Elimina un producto por su ID.
     * 
     * @param id ID del producto a eliminar
     * @return ResponseEntity sin contenido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        logger.info("DELETE /productos/{} - Eliminando producto", id);
        
        try {
            productoService.eliminarProducto(id);
            logger.info("Producto eliminado exitosamente con ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Error al eliminar producto con ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Verifica si un producto existe.
     * 
     * @param id ID del producto
     * @return ResponseEntity con el estado de existencia
     */
    @GetMapping(value = "/{id}/existe", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> existeProducto(@PathVariable Long id) {
        logger.info("GET /productos/{}/existe - Verificando existencia del producto", id);
        
        boolean existe = productoService.existeProducto(id);
        logger.info("Producto con ID {} existe: {}", id, existe);
        return ResponseEntity.ok(existe);
    }

    /**
     * Obtiene el número total de productos.
     * 
     * @return ResponseEntity con el conteo de productos
     */
    @GetMapping(value = "/contar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> contarProductos() {
        logger.info("GET /productos/contar - Contando productos");
        
        long total = productoService.contarProductos();
        logger.info("Total de productos: {}", total);
        return ResponseEntity.ok(total);
    }

    /**
     * Health check endpoint.
     * 
     * @return ResponseEntity con el estado del servicio
     */
    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> health() {
        logger.debug("GET /productos/health - Health check");
        return ResponseEntity.ok("Productos Service is running");
    }
}
