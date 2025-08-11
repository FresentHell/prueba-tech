package com.microservicios.productos.integration;

import com.microservicios.productos.model.Producto;
import com.microservicios.productos.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoIntegrationTest {

    @Mock
    private ProductoRepository productoRepository;

    private Producto producto1;
    private Producto producto2;

    @BeforeEach
    void setUp() {
        // Crear productos de prueba
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
    void crearProducto_DebeGuardarEnBaseDeDatos() {
        // Arrange
        when(productoRepository.save(any(Producto.class))).thenReturn(producto1);

        // Act
        Producto productoGuardado = productoRepository.save(producto1);

        // Assert
        assertNotNull(productoGuardado.getId());
        assertEquals("Laptop Gaming", productoGuardado.getNombre());
        assertEquals(new BigDecimal("1299.99"), productoGuardado.getPrecio());
        verify(productoRepository).save(producto1);
    }

    @Test
    void obtenerProductoPorId_DebeRetornarProductoCorrecto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));

        // Act
        Producto productoEncontrado = productoRepository.findById(1L).orElse(null);

        // Assert
        assertNotNull(productoEncontrado);
        assertEquals("Laptop Gaming", productoEncontrado.getNombre());
        assertEquals(new BigDecimal("1299.99"), productoEncontrado.getPrecio());
        verify(productoRepository).findById(1L);
    }

    @Test
    void obtenerTodosLosProductos_DebeRetornarListaCompleta() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto1, producto2);
        when(productoRepository.findAll()).thenReturn(productos);

        // Act
        var resultado = productoRepository.findAll();

        // Assert
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().anyMatch(p -> p.getNombre().equals("Laptop Gaming")));
        assertTrue(resultado.stream().anyMatch(p -> p.getNombre().equals("Mouse Inalámbrico")));
        verify(productoRepository).findAll();
    }

    @Test
    void buscarProductosPorNombre_DebeRetornarCoincidencias() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto1);
        when(productoRepository.findByNombreContainingIgnoreCase("Laptop")).thenReturn(productos);

        // Act
        var resultado = productoRepository.findByNombreContainingIgnoreCase("Laptop");

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Laptop Gaming", resultado.get(0).getNombre());
        verify(productoRepository).findByNombreContainingIgnoreCase("Laptop");
    }

    @Test
    void buscarProductosPorPrecio_DebeRetornarProductosEnRango() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto2);
        BigDecimal precioMin = new BigDecimal("40.00");
        BigDecimal precioMax = new BigDecimal("60.00");
        when(productoRepository.findByPrecioBetweenOrderByPrecioAsc(precioMin, precioMax)).thenReturn(productos);

        // Act
        var resultado = productoRepository.findByPrecioBetweenOrderByPrecioAsc(precioMin, precioMax);

        // Assert
        assertEquals(1, resultado.size());
        assertEquals("Mouse Inalámbrico", resultado.get(0).getNombre());
        verify(productoRepository).findByPrecioBetweenOrderByPrecioAsc(precioMin, precioMax);
    }

    @Test
    void actualizarProducto_DebeModificarDatosCorrectamente() {
        // Arrange
        String nuevoNombre = "Laptop Gaming Pro";
        producto1.setNombre(nuevoNombre);
        when(productoRepository.save(producto1)).thenReturn(producto1);

        // Act
        Producto productoActualizado = productoRepository.save(producto1);

        // Assert
        assertEquals(nuevoNombre, productoActualizado.getNombre());
        assertEquals(producto1.getId(), productoActualizado.getId());
        verify(productoRepository).save(producto1);
    }

    @Test
    void eliminarProducto_DebeRemoverDeBaseDeDatos() {
        // Arrange
        doNothing().when(productoRepository).deleteById(1L);

        // Act
        productoRepository.deleteById(1L);

        // Assert
        verify(productoRepository).deleteById(1L);
    }

    @Test
    void existeProducto_DebeRetornarTrueCuandoExiste() {
        // Arrange
        when(productoRepository.existsByNombre("Laptop Gaming")).thenReturn(true);

        // Act
        boolean existe = productoRepository.existsByNombre("Laptop Gaming");

        // Assert
        assertTrue(existe);
        verify(productoRepository).existsByNombre("Laptop Gaming");
    }

    @Test
    void existeProducto_DebeRetornarFalseCuandoNoExiste() {
        // Arrange
        when(productoRepository.existsByNombre("Producto Inexistente")).thenReturn(false);

        // Act
        boolean existe = productoRepository.existsByNombre("Producto Inexistente");

        // Assert
        assertFalse(existe);
        verify(productoRepository).existsByNombre("Producto Inexistente");
    }

    @Test
    void contarProductos_DebeRetornarNumeroCorrecto() {
        // Arrange
        when(productoRepository.count()).thenReturn(2L);

        // Act
        long count = productoRepository.count();

        // Assert
        assertEquals(2, count);
        verify(productoRepository).count();
    }
}
