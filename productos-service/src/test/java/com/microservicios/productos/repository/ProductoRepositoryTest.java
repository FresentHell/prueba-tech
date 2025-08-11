package com.microservicios.productos.repository;

import com.microservicios.productos.model.Producto;
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
class ProductoRepositoryTest {

    @Mock
    private ProductoRepository productoRepository;

    private Producto producto1;
    private Producto producto2;
    private Producto producto3;

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

        producto3 = new Producto();
        producto3.setId(3L);
        producto3.setNombre("Teclado Mecánico");
        producto3.setPrecio(new BigDecimal("89.99"));
        producto3.setDescripcion("Teclado mecánico con switches Cherry MX");
        producto3.setFechaCreacion(LocalDateTime.now());
        producto3.setFechaActualizacion(LocalDateTime.now());
        producto3.setVersion(1L);
    }

    @Test
    void save_DebeGuardarProductoCorrectamente() {
        // Arrange
        when(productoRepository.save(any(Producto.class))).thenReturn(producto1);

        // Act
        Producto productoGuardado = productoRepository.save(producto1);

        // Assert
        assertNotNull(productoGuardado.getId());
        assertEquals(producto1.getNombre(), productoGuardado.getNombre());
        assertEquals(producto1.getPrecio(), productoGuardado.getPrecio());
        verify(productoRepository).save(producto1);
    }

    @Test
    void findById_DebeRetornarProductoCuandoExiste() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));

        // Act
        Optional<Producto> resultado = productoRepository.findById(1L);

        // Assert
        assertTrue(resultado.isPresent());
        assertEquals(producto1.getNombre(), resultado.get().getNombre());
        verify(productoRepository).findById(1L);
    }

    @Test
    void findById_DebeRetornarEmptyCuandoNoExiste() {
        // Arrange
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Producto> resultado = productoRepository.findById(999L);

        // Assert
        assertFalse(resultado.isPresent());
        verify(productoRepository).findById(999L);
    }

    @Test
    void findAllByOrderByFechaCreacionDesc_DebeRetornarProductosOrdenados() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto1, producto2, producto3);
        when(productoRepository.findAllOrderByFechaCreacionDesc()).thenReturn(productos);

        // Act
        List<Producto> resultado = productoRepository.findAllOrderByFechaCreacionDesc();

        // Assert
        assertEquals(3, resultado.size());
        verify(productoRepository).findAllOrderByFechaCreacionDesc();
    }

    @Test
    void findByNombreContainingIgnoreCase_DebeRetornarProductosCoincidentes() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto1);
        when(productoRepository.findByNombreContainingIgnoreCase("laptop")).thenReturn(productos);

        // Act
        List<Producto> resultado = productoRepository.findByNombreContainingIgnoreCase("laptop");

        // Assert
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getNombre().toLowerCase().contains("laptop"));
        verify(productoRepository).findByNombreContainingIgnoreCase("laptop");
    }

    @Test
    void findByNombreContainingIgnoreCase_DebeRetornarVacioSiNoHayCoincidencias() {
        // Arrange
        when(productoRepository.findByNombreContainingIgnoreCase("inexistente")).thenReturn(Arrays.asList());

        // Act
        List<Producto> resultado = productoRepository.findByNombreContainingIgnoreCase("inexistente");

        // Assert
        assertEquals(0, resultado.size());
        verify(productoRepository).findByNombreContainingIgnoreCase("inexistente");
    }

    @Test
    void findByPrecioBetweenOrderByPrecioAsc_DebeRetornarProductosEnRango() {
        // Arrange
        BigDecimal precioMin = new BigDecimal("50.00");
        BigDecimal precioMax = new BigDecimal("100.00");
        List<Producto> productos = Arrays.asList(producto2, producto3);
        when(productoRepository.findByPrecioBetweenOrderByPrecioAsc(precioMin, precioMax)).thenReturn(productos);

        // Act
        List<Producto> resultado = productoRepository.findByPrecioBetweenOrderByPrecioAsc(precioMin, precioMax);

        // Assert
        assertEquals(2, resultado.size());
        verify(productoRepository).findByPrecioBetweenOrderByPrecioAsc(precioMin, precioMax);
    }

    @Test
    void existsByNombre_DebeRetornarTrueCuandoExiste() {
        // Arrange
        when(productoRepository.existsByNombre("Laptop Gaming")).thenReturn(true);

        // Act
        boolean existe = productoRepository.existsByNombre("Laptop Gaming");

        // Assert
        assertTrue(existe);
        verify(productoRepository).existsByNombre("Laptop Gaming");
    }

    @Test
    void existsByNombre_DebeRetornarFalseCuandoNoExiste() {
        // Arrange
        when(productoRepository.existsByNombre("Producto Inexistente")).thenReturn(false);

        // Act
        boolean existe = productoRepository.existsByNombre("Producto Inexistente");

        // Assert
        assertFalse(existe);
        verify(productoRepository).existsByNombre("Producto Inexistente");
    }

    @Test
    void count_DebeRetornarNumeroCorrecto() {
        // Arrange
        when(productoRepository.count()).thenReturn(2L);

        // Act
        long count = productoRepository.count();

        // Assert
        assertEquals(2, count);
        verify(productoRepository).count();
    }

    @Test
    void deleteById_DebeEliminarProducto() {
        // Arrange
        doNothing().when(productoRepository).deleteById(1L);

        // Act
        productoRepository.deleteById(1L);

        // Assert
        verify(productoRepository).deleteById(1L);
    }

    @Test
    void save_DebeActualizarProductoExistente() {
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
    void findByNombreContainingIgnoreCase_DebeSerCaseInsensitive() {
        // Arrange
        List<Producto> productos = Arrays.asList(producto1);
        when(productoRepository.findByNombreContainingIgnoreCase("LAPTOP")).thenReturn(productos);
        when(productoRepository.findByNombreContainingIgnoreCase("laptop")).thenReturn(productos);
        when(productoRepository.findByNombreContainingIgnoreCase("Laptop")).thenReturn(productos);

        // Act
        List<Producto> productos1 = productoRepository.findByNombreContainingIgnoreCase("LAPTOP");
        List<Producto> productos2 = productoRepository.findByNombreContainingIgnoreCase("laptop");
        List<Producto> productos3 = productoRepository.findByNombreContainingIgnoreCase("Laptop");

        // Assert
        assertEquals(1, productos1.size());
        assertEquals(1, productos2.size());
        assertEquals(1, productos3.size());
        assertEquals(productos1.get(0).getId(), productos2.get(0).getId());
        assertEquals(productos2.get(0).getId(), productos3.get(0).getId());
    }
}
