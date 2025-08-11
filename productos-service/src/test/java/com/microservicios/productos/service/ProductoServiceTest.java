package com.microservicios.productos.service;

import com.microservicios.productos.dto.ProductoRequest;
import com.microservicios.productos.dto.ProductoResponse;
import com.microservicios.productos.model.Producto;
import com.microservicios.productos.repository.ProductoRepository;
import com.microservicios.productos.exception.ResourceNotFoundException;
import com.microservicios.productos.exception.ResourceConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    void setUp() {
        producto1 = new Producto();
        producto1.setId(1L);
        producto1.setNombre("Laptop Gaming");
        producto1.setPrecio(new BigDecimal("1299.99"));
        producto1.setDescripcion("Laptop para gaming de alto rendimiento");
        producto1.setFechaCreacion(LocalDateTime.now());
        producto1.setFechaActualizacion(LocalDateTime.now());
        producto1.setVersion(1L);

        producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Mouse Inalámbrico");
        producto2.setPrecio(new BigDecimal("49.99"));
        producto2.setDescripcion("Mouse inalámbrico ergonómico");
        producto2.setFechaCreacion(LocalDateTime.now());
        producto2.setFechaActualizacion(LocalDateTime.now());
        producto2.setVersion(1L);
    }

    @Test
    void crearProducto_DebeRetornarProductoCreado() {
        // Arrange
        ProductoRequest request = new ProductoRequest();
        request.setData(new ProductoRequest.Data());
        request.getData().setType("productos");
        request.getData().setAttributes(new ProductoRequest.Data.Attributes());
        request.getData().getAttributes().setNombre(producto1.getNombre());
        request.getData().getAttributes().setPrecio(producto1.getPrecio());
        request.getData().getAttributes().setDescripcion(producto1.getDescripcion());
        
        when(productoRepository.existsByNombre(producto1.getNombre())).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenReturn(producto1);

        // Act
        ProductoResponse resultado = productoService.crearProducto(request);

        // Assert
        assertNotNull(resultado);
        assertEquals(producto1.getNombre(), resultado.getData().getAttributes().getNombre());
        assertEquals(producto1.getPrecio(), resultado.getData().getAttributes().getPrecio());
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void crearProducto_DebeLanzarExcepcionSiNombreExiste() {
        // Arrange
        ProductoRequest request = new ProductoRequest();
        request.setData(new ProductoRequest.Data());
        request.getData().setType("productos");
        request.getData().setAttributes(new ProductoRequest.Data.Attributes());
        request.getData().getAttributes().setNombre(producto1.getNombre());
        request.getData().getAttributes().setPrecio(producto1.getPrecio());
        request.getData().getAttributes().setDescripcion(producto1.getDescripcion());
        
        when(productoRepository.existsByNombre(producto1.getNombre())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productoService.crearProducto(request);
        });
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void obtenerProductoPorId_DebeRetornarProductoCuandoExiste() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));

        // Act
        ProductoResponse resultado = productoService.obtenerProductoPorId(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(producto1.getId().toString(), resultado.getData().getId());
        assertEquals(producto1.getNombre(), resultado.getData().getAttributes().getNombre());
    }

    @Test
    void obtenerProductoPorId_DebeLanzarExcepcionCuandoNoExiste() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productoService.obtenerProductoPorId(999L);
        });
    }

    @Test
    void obtenerTodosLosProductos_DebeRetornarListaDeProductos() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto1, producto2);
        when(productoRepository.findAllOrderByFechaCreacionDesc()).thenReturn(productos);

        // Act
        ProductoResponse resultado = productoService.obtenerTodosLosProductos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getDataList().size());
        assertEquals(producto1.getNombre(), resultado.getDataList().get(0).getAttributes().getNombre());
        assertEquals(producto2.getNombre(), resultado.getDataList().get(1).getAttributes().getNombre());
    }

    @Test
    void buscarProductosPorNombre_DebeRetornarProductosCoincidentes() {
        // Arrange
        String nombreBusqueda = "Laptop";
        List<Producto> productos = Arrays.asList(producto1);
        when(productoRepository.findByNombreContainingIgnoreCase(nombreBusqueda)).thenReturn(productos);

        // Act
        ProductoResponse resultado = productoService.buscarProductosPorNombre(nombreBusqueda);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getDataList().size());
        assertTrue(resultado.getDataList().get(0).getAttributes().getNombre().toLowerCase().contains(nombreBusqueda.toLowerCase()));
    }

    @Test
    void buscarProductosPorPrecio_DebeRetornarProductosEnRango() {
        // Arrange
        BigDecimal precioMin = new BigDecimal("50.00");
        BigDecimal precioMax = new BigDecimal("1500.00");
        List<Producto> productos = Arrays.asList(producto1, producto2);
        when(productoRepository.findByPrecioBetween(precioMin, precioMax)).thenReturn(productos);

        // Act
        ProductoResponse resultado = productoService.buscarProductosPorPrecio(precioMin, precioMax);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.getDataList().size());
        assertTrue(resultado.getDataList().get(0).getAttributes().getPrecio().compareTo(precioMin) >= 0);
        assertTrue(resultado.getDataList().get(1).getAttributes().getPrecio().compareTo(precioMax) <= 0);
    }

    @Test
    void actualizarProducto_DebeRetornarProductoActualizado() {
        // Arrange
        Producto productoActualizado = new Producto();
        productoActualizado.setId(1L);
        productoActualizado.setNombre("Laptop Gaming Pro");
        productoActualizado.setPrecio(new BigDecimal("1499.99"));
        productoActualizado.setDescripcion("Laptop gaming profesional actualizada");

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoActualizado);

        ProductoRequest request = new ProductoRequest();
        request.setData(new ProductoRequest.Data());
        request.getData().setType("productos");
        request.getData().setAttributes(new ProductoRequest.Data.Attributes());
        request.getData().getAttributes().setNombre(productoActualizado.getNombre());
        request.getData().getAttributes().setPrecio(productoActualizado.getPrecio());
        request.getData().getAttributes().setDescripcion(productoActualizado.getDescripcion());

        // Act
        ProductoResponse resultado = productoService.actualizarProducto(1L, request);

        // Assert
        assertNotNull(resultado);
        assertEquals(productoActualizado.getNombre(), resultado.getData().getAttributes().getNombre());
        assertEquals(productoActualizado.getPrecio(), resultado.getData().getAttributes().getPrecio());
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void actualizarProducto_DebeLanzarExcepcionCuandoNoExiste() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ProductoRequest request = new ProductoRequest();
        request.setData(new ProductoRequest.Data());
        request.getData().setType("productos");
        request.getData().setAttributes(new ProductoRequest.Data.Attributes());
        request.getData().getAttributes().setNombre(producto1.getNombre());
        request.getData().getAttributes().setPrecio(producto1.getPrecio());
        request.getData().getAttributes().setDescripcion(producto1.getDescripcion());

        assertThrows(ResourceNotFoundException.class, () -> {
            productoService.actualizarProducto(999L, request);
        });
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void eliminarProducto_DebeEliminarProductoExitosamente() {
        // Arrange
        when(productoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productoRepository).deleteById(1L);

        // Act
        productoService.eliminarProducto(1L);

        // Assert
        verify(productoRepository).deleteById(1L);
    }

    @Test
    void eliminarProducto_DebeLanzarExcepcionCuandoNoExiste() {
        // Arrange
        when(productoRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productoService.eliminarProducto(999L);
        });
        verify(productoRepository, never()).deleteById(anyLong());
    }

    @Test
    void existeProducto_DebeRetornarTrueCuandoExiste() {
        // Arrange
        when(productoRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean resultado = productoService.existeProducto(1L);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void existeProducto_DebeRetornarFalseCuandoNoExiste() {
        // Arrange
        when(productoRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean resultado = productoService.existeProducto(999L);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void contarProductos_DebeRetornarNumeroCorrecto() {
        // Arrange
        when(productoRepository.countProductos()).thenReturn(2L);

        // Act
        long resultado = productoService.contarProductos();

        // Assert
        assertEquals(2L, resultado);
    }

    @Test
    void crearProducto_DebeValidarDatosRequeridos() {
        // Arrange
        ProductoRequest request = new ProductoRequest();
        request.setData(new ProductoRequest.Data());
        request.getData().setType("productos");
        request.getData().setAttributes(new ProductoRequest.Data.Attributes());
        request.getData().getAttributes().setNombre("Test");
        request.getData().getAttributes().setPrecio(new BigDecimal("-100")); // Precio negativo
        request.getData().getAttributes().setDescripcion("Test");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productoService.crearProducto(request);
        });
    }

    @Test
    void buscarProductosPorPrecio_DebeValidarRangoDePrecios() {
        // Arrange
        BigDecimal precioMin = new BigDecimal("100.00");
        BigDecimal precioMax = new BigDecimal("50.00"); // Máximo menor que mínimo

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            productoService.buscarProductosPorPrecio(precioMin, precioMax);
        });
    }
}
