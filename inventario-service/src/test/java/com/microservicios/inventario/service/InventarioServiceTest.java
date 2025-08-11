package com.microservicios.inventario.service;

import com.microservicios.inventario.client.ProductosClient;
import com.microservicios.inventario.dto.*;
import com.microservicios.inventario.event.InventarioCambiadoEvent;
import com.microservicios.inventario.model.HistorialCompras;
import com.microservicios.inventario.model.Inventario;
import com.microservicios.inventario.repository.HistorialComprasRepository;
import com.microservicios.inventario.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private HistorialComprasRepository historialComprasRepository;

    @Mock
    private ProductosClient productosClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private InventarioService inventarioService;

    private Inventario inventario;
    private ProductoInfo productoInfo;
    private HistorialCompras historialCompra;
    private CompraRequest compraRequest;
    private InventarioRequest inventarioRequest;

    @BeforeEach
    void setUp() {
        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProductoId(1L);
        inventario.setCantidad(10);
        inventario.setFechaCreacion(LocalDateTime.now());
        inventario.setFechaActualizacion(LocalDateTime.now());
        inventario.setVersion(1L);

        productoInfo = new ProductoInfo();
        ProductoInfo.Data data = new ProductoInfo.Data();
        ProductoInfo.Data.Attributes attributes = new ProductoInfo.Data.Attributes();
        
        attributes.setNombre("Laptop Gaming");
        attributes.setPrecio(new BigDecimal("1299.99"));
        attributes.setDescripcion("Laptop para gaming de alto rendimiento");
        
        data.setId("1");
        data.setType("productos");
        data.setAttributes(attributes);
        
        productoInfo.setData(data);

        historialCompra = new HistorialCompras();
        historialCompra.setId(1L);
        historialCompra.setProductoId(1L);
        historialCompra.setCantidad(2);
        historialCompra.setPrecioUnitario(new BigDecimal("1299.99"));
        historialCompra.setPrecioTotal(new BigDecimal("2599.98"));
        historialCompra.setFechaCompra(LocalDateTime.now());
        historialCompra.setNombreProducto("Laptop Gaming");

        // Configurar CompraRequest
        compraRequest = new CompraRequest();
        CompraRequest.Data compraData = new CompraRequest.Data();
        CompraRequest.Data.Attributes compraAttributes = new CompraRequest.Data.Attributes();
        compraAttributes.setProductoId(1L);
        compraAttributes.setCantidad(2);
        compraData.setAttributes(compraAttributes);
        compraData.setType("compras");
        compraRequest.setData(compraData);

        // Configurar InventarioRequest
        inventarioRequest = new InventarioRequest();
        InventarioRequest.Data inventarioData = new InventarioRequest.Data();
        InventarioRequest.Data.Attributes inventarioAttributes = new InventarioRequest.Data.Attributes();
        inventarioAttributes.setCantidad(20);
        inventarioData.setAttributes(inventarioAttributes);
        inventarioData.setType("inventario");
        inventarioRequest.setData(inventarioData);
    }

    @Test
    void realizarCompra_DebeRealizarCompraExitosamente() {
        // Arrange
        when(productosClient.obtenerProducto(1L)).thenReturn(Mono.just(productoInfo));
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);
        when(historialComprasRepository.save(any(HistorialCompras.class))).thenReturn(historialCompra);

        // Act
        CompraResponse resultado = inventarioService.realizarCompra(compraRequest);

        // Assert
        assertNotNull(resultado);
        verify(productosClient).obtenerProducto(1L);
        verify(inventarioRepository).save(any(Inventario.class));
        verify(historialComprasRepository).save(any(HistorialCompras.class));
        verify(eventPublisher).publishEvent(any(InventarioCambiadoEvent.class));
    }

    @Test
    void realizarCompra_DebeLanzarExcepcionSiProductoNoExiste() {
        // Arrange
        when(productosClient.obtenerProducto(999L)).thenReturn(Mono.empty());

        CompraRequest request = new CompraRequest();
        CompraRequest.Data data = new CompraRequest.Data();
        CompraRequest.Data.Attributes attributes = new CompraRequest.Data.Attributes();
        attributes.setProductoId(999L);
        attributes.setCantidad(2);
        data.setAttributes(attributes);
        request.setData(data);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.realizarCompra(request);
        });
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(historialComprasRepository, never()).save(any(HistorialCompras.class));
    }

    @Test
    void realizarCompra_DebeLanzarExcepcionSiInventarioInsuficiente() {
        // Arrange
        inventario.setCantidad(1); // Solo 1 disponible
        when(productosClient.obtenerProducto(1L)).thenReturn(Mono.just(productoInfo));
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.realizarCompra(compraRequest);
        });
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(historialComprasRepository, never()).save(any(HistorialCompras.class));
    }

    @Test
    void realizarCompra_DebeLanzarExcepcionSiProductoNoTieneInventario() {
        // Arrange
        when(productosClient.obtenerProducto(1L)).thenReturn(Mono.just(productoInfo));
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.realizarCompra(compraRequest);
        });
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(historialComprasRepository, never()).save(any(HistorialCompras.class));
    }

    @Test
    void consultarInventario_DebeRetornarInventarioCuandoExiste() {
        // Arrange
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));

        // Act
        InventarioResponse resultado = inventarioService.consultarInventario(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(inventario.getCantidad(), resultado.getData().getAttributes().getCantidad());
        assertEquals(inventario.getProductoId(), Long.valueOf(resultado.getData().getId()));
    }

    @Test
    void consultarInventario_DebeLanzarExcepcionCuandoNoExiste() {
        // Arrange
        when(inventarioRepository.findByProductoId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.consultarInventario(999L);
        });
    }

    @Test
    void actualizarInventario_DebeActualizarInventarioExitosamente() {
        // Arrange
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // Act
        InventarioResponse resultado = inventarioService.actualizarInventario(1L, inventarioRequest);

        // Assert
        assertNotNull(resultado);
        verify(inventarioRepository).save(any(Inventario.class));
        verify(eventPublisher).publishEvent(any(InventarioCambiadoEvent.class));
    }

    @Test
    void actualizarInventario_DebeLanzarExcepcionSiNoExiste() {
        // Arrange
        when(inventarioRepository.findByProductoId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.actualizarInventario(999L, inventarioRequest);
        });
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void obtenerTodosLosInventarios_DebeRetornarListaDeInventarios() {
        // Arrange
        List<Inventario> inventarios = Arrays.asList(inventario);
        when(inventarioRepository.findAllOrderByFechaActualizacionDesc()).thenReturn(inventarios);

        // Act
        InventarioResponse resultado = inventarioService.obtenerTodosLosInventarios();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getDataList().size());
    }

    @Test
    void obtenerInventariosBajos_DebeRetornarInventariosConStockBajo() {
        // Arrange
        List<Inventario> inventariosBajos = Arrays.asList(inventario);
        when(inventarioRepository.findByCantidadLessThan(5)).thenReturn(inventariosBajos);

        // Act
        InventarioResponse resultado = inventarioService.obtenerInventariosBajos(5);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getDataList().size());
    }

    @Test
    void obtenerProductosSinInventario_DebeRetornarInventariosConStockCero() {
        // Arrange
        List<Inventario> inventariosSinStock = Arrays.asList(inventario);
        when(inventarioRepository.findByCantidadZero()).thenReturn(inventariosSinStock);

        // Act
        InventarioResponse resultado = inventarioService.obtenerProductosSinInventario();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.getDataList().size());
    }

    @Test
    void crearInventario_DebeCrearInventarioExitosamente() {
        // Arrange
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // Act
        InventarioResponse resultado = inventarioService.crearInventario(1L, 10);

        // Assert
        assertNotNull(resultado);
        verify(inventarioRepository).save(any(Inventario.class));
    }

    @Test
    void obtenerEstadisticasInventario_DebeRetornarEstadisticas() {
        // Arrange
        when(inventarioRepository.countInventarios()).thenReturn(10L);
        when(inventarioRepository.sumCantidadTotal()).thenReturn(100);
        when(inventarioRepository.findByCantidadZero()).thenReturn(Arrays.asList());

        // Act
        String estadisticas = inventarioService.obtenerEstadisticasInventario();

        // Assert
        assertNotNull(estadisticas);
        assertTrue(estadisticas.contains("Total de productos"));
        assertTrue(estadisticas.contains("Cantidad total en inventario"));
    }

    @Test
    void realizarCompra_DebeValidarCantidadPositiva() {
        // Arrange
        CompraRequest request = new CompraRequest();
        CompraRequest.Data data = new CompraRequest.Data();
        CompraRequest.Data.Attributes attributes = new CompraRequest.Data.Attributes();
        attributes.setProductoId(1L);
        attributes.setCantidad(-1); // Cantidad negativa
        data.setAttributes(attributes);
        request.setData(data);

        // Act & Assert
        // Ahora el servicio valida cantidades negativas y debe lanzar excepción
        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.realizarCompra(request);
        });
        
        // Verificar que no se llamó al cliente ni al repositorio
        verify(productosClient, never()).obtenerProducto(anyLong());
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(historialComprasRepository, never()).save(any(HistorialCompras.class));
    }

    @Test
    void actualizarInventario_DebeValidarCantidadNoNegativa() {
        // Arrange
        InventarioRequest request = new InventarioRequest();
        InventarioRequest.Data data = new InventarioRequest.Data();
        InventarioRequest.Data.Attributes attributes = new InventarioRequest.Data.Attributes();
        attributes.setCantidad(-5); // Cantidad negativa
        data.setAttributes(attributes);
        request.setData(data);

        // Act & Assert
        // Ahora el servicio valida cantidades negativas y debe lanzar excepción
        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.actualizarInventario(1L, request);
        });
        
        // Verificar que no se guardó el inventario
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void realizarCompra_DebeCalcularPrecioTotalCorrectamente() {
        // Arrange
        BigDecimal precioUnitario = new BigDecimal("100.00");
        productoInfo.getData().getAttributes().setPrecio(precioUnitario);
        
        when(productosClient.obtenerProducto(1L)).thenReturn(Mono.just(productoInfo));
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);
        when(historialComprasRepository.save(any(HistorialCompras.class))).thenReturn(historialCompra);

        // Act
        inventarioService.realizarCompra(compraRequest);

        // Assert
        verify(historialComprasRepository).save(argThat(historial -> 
            historial.getPrecioTotal().equals(precioUnitario.multiply(BigDecimal.valueOf(2))) &&
            historial.getCantidad().equals(2) &&
            historial.getPrecioUnitario().equals(precioUnitario)
        ));
    }

    @Test
    void crearInventario_DebeValidarCantidadNoNegativa() {
        // Act & Assert
        // El servicio ahora valida cantidades negativas y debe lanzar excepción
        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.crearInventario(1L, -10);
        });
        
        // Verificar que no se guardó el inventario
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void realizarCompra_DebeValidarCantidadCero() {
        // Arrange
        CompraRequest request = new CompraRequest();
        CompraRequest.Data data = new CompraRequest.Data();
        CompraRequest.Data.Attributes attributes = new CompraRequest.Data.Attributes();
        attributes.setProductoId(1L);
        attributes.setCantidad(0); // Cantidad cero
        data.setAttributes(attributes);
        request.setData(data);

        // Act & Assert
        // El servicio valida que la cantidad sea mayor a 0
        assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.realizarCompra(request);
        });
        
        // Verificar que no se llamó al cliente ni al repositorio
        verify(productosClient, never()).obtenerProducto(anyLong());
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(historialComprasRepository, never()).save(any(HistorialCompras.class));
    }
}
