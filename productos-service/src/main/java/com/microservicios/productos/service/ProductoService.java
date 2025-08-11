package com.microservicios.productos.service;

import com.microservicios.productos.dto.ProductoRequest;
import com.microservicios.productos.dto.ProductoResponse;
import com.microservicios.productos.exception.ResourceNotFoundException;
import com.microservicios.productos.model.Producto;
import com.microservicios.productos.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio que maneja la lógica de negocio para productos.
 * Implementa el patrón Service Layer para centralizar la lógica de negocio.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@Service
@Transactional
public class ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /**
     * Crea un nuevo producto.
     * 
     * @param request DTO con los datos del producto a crear
     * @return ProductoResponse con el producto creado
     * @throws IllegalArgumentException si el producto ya existe
     */
    public ProductoResponse crearProducto(ProductoRequest request) {
        logger.info("Creando nuevo producto: {}", request.getData().getAttributes().getNombre());

        ProductoRequest.Data.Attributes attributes = request.getData().getAttributes();
        
        // Validar que el producto no exista ya
        if (productoRepository.existsByNombre(attributes.getNombre())) {
            logger.warn("Intento de crear producto con nombre duplicado: {}", attributes.getNombre());
            throw new IllegalArgumentException("Ya existe un producto con el nombre: " + attributes.getNombre());
        }

        // Crear el producto
        Producto producto = new Producto();
        producto.setNombre(attributes.getNombre());
        producto.setPrecio(attributes.getPrecio());
        producto.setDescripcion(attributes.getDescripcion());

        Producto productoGuardado = productoRepository.save(producto);
        
        if (productoGuardado == null || productoGuardado.getId() == null) {
            logger.error("Error al guardar el producto: el producto guardado es null o no tiene ID");
            throw new IllegalArgumentException("Error al crear el producto");
        }
        
        logger.info("Producto creado exitosamente con ID: {}", productoGuardado.getId());

        return ProductoResponse.fromProducto(productoGuardado);
    }

    /**
     * Obtiene un producto por su ID.
     * 
     * @param id ID del producto
     * @return ProductoResponse con el producto encontrado
     * @throws IllegalArgumentException si el producto no existe
     */
    @Transactional(readOnly = true)
    public ProductoResponse obtenerProductoPorId(Long id) {
        logger.info("Buscando producto con ID: {}", id);

        Optional<Producto> productoOpt = productoRepository.findById(id);
        
        if (productoOpt.isEmpty()) {
            logger.warn("Producto no encontrado con ID: {}", id);
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + id);
        }

        Producto producto = productoOpt.get();
        logger.info("Producto encontrado: {}", producto.getNombre());

        return ProductoResponse.fromProducto(producto);
    }

    /**
     * Obtiene todos los productos.
     * 
     * @return ProductoResponse con la lista de productos
     */
    @Transactional(readOnly = true)
    public ProductoResponse obtenerTodosLosProductos() {
        logger.info("Obteniendo todos los productos");

        List<Producto> productos = productoRepository.findAllOrderByFechaCreacionDesc();
        logger.info("Se encontraron {} productos", productos.size());

        return ProductoResponse.fromProductos(productos);
    }

    /**
     * Busca productos por nombre.
     * 
     * @param nombre Nombre o parte del nombre a buscar
     * @return ProductoResponse con los productos encontrados
     */
    @Transactional(readOnly = true)
    public ProductoResponse buscarProductosPorNombre(String nombre) {
        logger.info("Buscando productos con nombre: {}", nombre);

        List<Producto> productos = productoRepository.findByNombreContainingIgnoreCase(nombre);
        logger.info("Se encontraron {} productos con el nombre: {}", productos.size(), nombre);

        return ProductoResponse.fromProductos(productos);
    }

    /**
     * Busca productos por rango de precios.
     * 
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return ProductoResponse con los productos encontrados
     */
    @Transactional(readOnly = true)
    public ProductoResponse buscarProductosPorPrecio(java.math.BigDecimal precioMin, java.math.BigDecimal precioMax) {
        logger.info("Buscando productos con precio entre {} y {}", precioMin, precioMax);

        // Validar que el precio mínimo sea menor o igual al precio máximo
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            logger.warn("Rango de precios inválido: precio mínimo ({}) es mayor que precio máximo ({})", precioMin, precioMax);
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor que el precio máximo");
        }

        List<Producto> productos = productoRepository.findByPrecioBetween(precioMin, precioMax);
        logger.info("Se encontraron {} productos en el rango de precios", productos.size());

        return ProductoResponse.fromProductos(productos);
    }

    /**
     * Actualiza un producto existente.
     * 
     * @param id ID del producto a actualizar
     * @param request DTO con los nuevos datos del producto
     * @return ProductoResponse con el producto actualizado
     * @throws IllegalArgumentException si el producto no existe
     */
    public ProductoResponse actualizarProducto(Long id, ProductoRequest request) {
        logger.info("Actualizando producto con ID: {}", id);

        Optional<Producto> productoOpt = productoRepository.findById(id);
        
        if (productoOpt.isEmpty()) {
            logger.warn("Producto no encontrado para actualizar con ID: {}", id);
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + id);
        }

        Producto producto = productoOpt.get();
        ProductoRequest.Data.Attributes attributes = request.getData().getAttributes();

        // Actualizar campos
        if (attributes.getNombre() != null) {
            producto.setNombre(attributes.getNombre());
        }
        if (attributes.getPrecio() != null) {
            producto.setPrecio(attributes.getPrecio());
        }
        if (attributes.getDescripcion() != null) {
            producto.setDescripcion(attributes.getDescripcion());
        }

        Producto productoActualizado = productoRepository.save(producto);
        logger.info("Producto actualizado exitosamente: {}", productoActualizado.getNombre());

        return ProductoResponse.fromProducto(productoActualizado);
    }

    /**
     * Elimina un producto por su ID.
     * 
     * @param id ID del producto a eliminar
     * @throws IllegalArgumentException si el producto no existe
     */
    public void eliminarProducto(Long id) {
        logger.info("Eliminando producto con ID: {}", id);

        if (!productoRepository.existsById(id)) {
            logger.warn("Producto no encontrado para eliminar con ID: {}", id);
            throw new ResourceNotFoundException("Producto no encontrado con ID: " + id);
        }

        productoRepository.deleteById(id);
        logger.info("Producto eliminado exitosamente con ID: {}", id);
    }

    /**
     * Verifica si un producto existe.
     * 
     * @param id ID del producto
     * @return true si existe, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean existeProducto(Long id) {
        return productoRepository.existsById(id);
    }

    /**
     * Obtiene el número total de productos.
     * 
     * @return Número total de productos
     */
    @Transactional(readOnly = true)
    public long contarProductos() {
        return productoRepository.countProductos();
    }
}
