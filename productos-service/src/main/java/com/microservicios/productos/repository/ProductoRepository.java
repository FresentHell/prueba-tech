package com.microservicios.productos.repository;

import com.microservicios.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Producto.
 * Proporciona métodos de acceso a datos para productos.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca un producto por su ID.
     * 
     * @param id ID del producto
     * @return Optional con el producto si existe
     */
    Optional<Producto> findById(Long id);

    /**
     * Busca productos por nombre (búsqueda parcial, case-insensitive).
     * 
     * @param nombre Nombre o parte del nombre del producto
     * @return Lista de productos que coinciden con el nombre
     */
    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Producto> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Busca productos por rango de precios.
     * 
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Lista de productos dentro del rango de precios
     */
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMin AND :precioMax ORDER BY p.precio")
    List<Producto> findByPrecioBetween(@Param("precioMin") java.math.BigDecimal precioMin, 
                                      @Param("precioMax") java.math.BigDecimal precioMax);

    /**
     * Verifica si existe un producto con el nombre especificado.
     * 
     * @param nombre Nombre del producto
     * @return true si existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);

    /**
     * Busca productos ordenados por fecha de creación (más recientes primero).
     * 
     * @return Lista de productos ordenados por fecha de creación
     */
    @Query("SELECT p FROM Producto p ORDER BY p.fechaCreacion DESC")
    List<Producto> findAllOrderByFechaCreacionDesc();

    /**
     * Busca productos por rango de precios ordenados por precio ascendente.
     * 
     * @param precioMin Precio mínimo
     * @param precioMax Precio máximo
     * @return Lista de productos dentro del rango de precios ordenados
     */
    @Query("SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMin AND :precioMax ORDER BY p.precio ASC")
    List<Producto> findByPrecioBetweenOrderByPrecioAsc(@Param("precioMin") java.math.BigDecimal precioMin, 
                                                       @Param("precioMax") java.math.BigDecimal precioMax);

    /**
     * Cuenta el número total de productos.
     * 
     * @return Número total de productos
     */
    @Query("SELECT COUNT(p) FROM Producto p")
    long countProductos();
}
