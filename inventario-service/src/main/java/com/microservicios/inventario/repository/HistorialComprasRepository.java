package com.microservicios.inventario.repository;

import com.microservicios.inventario.model.HistorialCompras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad HistorialCompras.
 * Proporciona métodos de acceso a datos para el historial de compras.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@Repository
public interface HistorialComprasRepository extends JpaRepository<HistorialCompras, Long> {

    /**
     * Busca compras por ID del producto.
     * 
     * @param productoId ID del producto
     * @return Lista de compras del producto
     */
    List<HistorialCompras> findByProductoIdOrderByFechaCompraDesc(Long productoId);

    /**
     * Busca compras en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de compras en el rango de fechas
     */
    @Query("SELECT h FROM HistorialCompras h WHERE h.fechaCompra BETWEEN :fechaInicio AND :fechaFin ORDER BY h.fechaCompra DESC")
    List<HistorialCompras> findByFechaCompraBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                                   @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Busca compras por nombre del producto (búsqueda parcial, case-insensitive).
     * 
     * @param nombreProducto Nombre o parte del nombre del producto
     * @return Lista de compras que coinciden con el nombre
     */
    @Query("SELECT h FROM HistorialCompras h WHERE LOWER(h.nombreProducto) LIKE LOWER(CONCAT('%', :nombreProducto, '%')) ORDER BY h.fechaCompra DESC")
    List<HistorialCompras> findByNombreProductoContainingIgnoreCase(@Param("nombreProducto") String nombreProducto);

    /**
     * Cuenta el número total de compras.
     * 
     * @return Número total de compras
     */
    @Query("SELECT COUNT(h) FROM HistorialCompras h")
    long countCompras();

    /**
     * Obtiene el total de ventas en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Suma total de precios de compras en el rango
     */
    @Query("SELECT COALESCE(SUM(h.precioTotal), 0) FROM HistorialCompras h WHERE h.fechaCompra BETWEEN :fechaInicio AND :fechaFin")
    java.math.BigDecimal sumPrecioTotalByFechaCompraBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                                           @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Obtiene las compras más recientes.
     * 
     * @param limit Número máximo de compras a retornar
     * @return Lista de las compras más recientes
     */
    @Query("SELECT h FROM HistorialCompras h ORDER BY h.fechaCompra DESC")
    List<HistorialCompras> findTopByOrderByFechaCompraDesc(@Param("limit") int limit);

    /**
     * Busca compras con precio total mayor a un valor.
     * 
     * @param precioMinimo Precio mínimo
     * @return Lista de compras con precio total mayor al especificado
     */
    @Query("SELECT h FROM HistorialCompras h WHERE h.precioTotal > :precioMinimo ORDER BY h.precioTotal DESC")
    List<HistorialCompras> findByPrecioTotalGreaterThan(@Param("precioMinimo") java.math.BigDecimal precioMinimo);

    /**
     * Obtiene estadísticas de compras por producto.
     * 
     * @return Lista de estadísticas agrupadas por producto
     */
    @Query("SELECT h.productoId, h.nombreProducto, COUNT(h) as totalCompras, " +
           "SUM(h.cantidad) as cantidadTotal, SUM(h.precioTotal) as precioTotal " +
           "FROM HistorialCompras h GROUP BY h.productoId, h.nombreProducto " +
           "ORDER BY precioTotal DESC")
    List<Object[]> getEstadisticasPorProducto();
}
