package com.microservicios.inventario.repository;

import com.microservicios.inventario.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Inventario.
 * Proporciona métodos de acceso a datos para inventario.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    /**
     * Busca inventario por ID del producto.
     * 
     * @param productoId ID del producto
     * @return Optional con el inventario si existe
     */
    Optional<Inventario> findByProductoId(Long productoId);

    /**
     * Verifica si existe inventario para un producto.
     * 
     * @param productoId ID del producto
     * @return true si existe, false en caso contrario
     */
    boolean existsByProductoId(Long productoId);

    /**
     * Busca productos con inventario bajo (menos de la cantidad especificada).
     * 
     * @param cantidadMinima Cantidad mínima para considerar inventario bajo
     * @return Lista de inventarios con stock bajo
     */
    @Query("SELECT i FROM Inventario i WHERE i.cantidad < :cantidadMinima ORDER BY i.cantidad")
    List<Inventario> findByCantidadLessThan(@Param("cantidadMinima") Integer cantidadMinima);

    /**
     * Busca productos sin inventario (cantidad = 0).
     * 
     * @return Lista de inventarios sin stock
     */
    @Query("SELECT i FROM Inventario i WHERE i.cantidad = 0")
    List<Inventario> findByCantidadZero();

    /**
     * Cuenta el número total de productos en inventario.
     * 
     * @return Número total de productos en inventario
     */
    @Query("SELECT COUNT(i) FROM Inventario i")
    long countInventarios();

    /**
     * Obtiene la cantidad total de productos en inventario.
     * 
     * @return Suma total de cantidades en inventario
     */
    @Query("SELECT COALESCE(SUM(i.cantidad), 0) FROM Inventario i")
    Integer sumCantidadTotal();

    /**
     * Busca inventarios ordenados por cantidad (de menor a mayor).
     * 
     * @return Lista de inventarios ordenados por cantidad
     */
    @Query("SELECT i FROM Inventario i ORDER BY i.cantidad ASC")
    List<Inventario> findAllOrderByCantidadAsc();

    /**
     * Busca inventarios ordenados por fecha de actualización (más recientes primero).
     * 
     * @return Lista de inventarios ordenados por fecha de actualización
     */
    @Query("SELECT i FROM Inventario i ORDER BY i.fechaActualizacion DESC")
    List<Inventario> findAllOrderByFechaActualizacionDesc();
}
