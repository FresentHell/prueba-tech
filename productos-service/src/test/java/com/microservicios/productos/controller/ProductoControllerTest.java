package com.microservicios.productos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservicios.productos.dto.ProductoRequest;
import com.microservicios.productos.dto.ProductoResponse;
import com.microservicios.productos.model.Producto;
import com.microservicios.productos.service.ProductoService;
import com.microservicios.productos.exception.ResourceNotFoundException;
import com.microservicios.productos.exception.ResourceConflictException;
import com.microservicios.productos.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    private ObjectMapper objectMapper;

    private Producto producto;
    private ProductoRequest productoRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        objectMapper = new ObjectMapper();

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop Gaming");
        producto.setPrecio(new BigDecimal("1299.99"));
        producto.setDescripcion("Laptop para gaming de alto rendimiento");
        producto.setFechaCreacion(LocalDateTime.now());
        producto.setFechaActualizacion(LocalDateTime.now());
        producto.setVersion(1L);

        productoRequest = new ProductoRequest();
        productoRequest.setData(new ProductoRequest.Data());
        productoRequest.getData().setType("productos");
        productoRequest.getData().setAttributes(new ProductoRequest.Data.Attributes());
        productoRequest.getData().getAttributes().setNombre("Laptop Gaming");
        productoRequest.getData().getAttributes().setPrecio(new BigDecimal("1299.99"));
        productoRequest.getData().getAttributes().setDescripcion("Laptop para gaming de alto rendimiento");
    }

    @Test
    void crearProducto_DebeRetornar201YProductoCreado() throws Exception {
        // Arrange
        when(productoService.crearProducto(any(ProductoRequest.class))).thenReturn(ProductoResponse.fromProducto(producto));

        // Act & Assert
        mockMvc.perform(post("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("productos"))
                .andExpect(jsonPath("$.data.attributes.nombre").value("Laptop Gaming"))
                .andExpect(jsonPath("$.data.attributes.precio").value(1299.99));
    }

    @Test
    void crearProducto_DebeRetornar409SiConflicto() throws Exception {
        // Arrange
        when(productoService.crearProducto(any(ProductoRequest.class)))
                .thenThrow(new ResourceConflictException("El producto ya existe"));

        // Act & Assert
        mockMvc.perform(post("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0].status").value("409"))
                .andExpect(jsonPath("$.errors[0].title").value("Conflict"));
    }

    @Test
    void obtenerProductoPorId_DebeRetornar200YProducto() throws Exception {
        // Arrange
        when(productoService.obtenerProductoPorId(1L)).thenReturn(ProductoResponse.fromProducto(producto));

        // Act & Assert
        mockMvc.perform(get("/productos/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("productos"))
                .andExpect(jsonPath("$.data.attributes.nombre").value("Laptop Gaming"));
    }

    @Test
    void obtenerProductoPorId_DebeRetornar404SiNoExiste() throws Exception {
        // Arrange
        when(productoService.obtenerProductoPorId(999L))
                .thenThrow(new ResourceNotFoundException("Producto no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/productos/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].status").value("404"))
                .andExpect(jsonPath("$.errors[0].title").value("Not Found"));
    }

    @Test
    void obtenerTodosLosProductos_DebeRetornar200YLista() throws Exception {
        // Arrange
        List<Producto> productos = Arrays.asList(producto);
        when(productoService.obtenerTodosLosProductos()).thenReturn(ProductoResponse.fromProductos(productos));

        // Act & Assert
        mockMvc.perform(get("/productos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList").isArray())
                .andExpect(jsonPath("$.dataList[0].id").value("1"))
                .andExpect(jsonPath("$.dataList[0].type").value("productos"));
    }

    @Test
    void buscarProductosPorNombre_DebeRetornar200YResultados() throws Exception {
        // Arrange
        List<Producto> productos = Arrays.asList(producto);
        when(productoService.buscarProductosPorNombre("Laptop")).thenReturn(ProductoResponse.fromProductos(productos));

        // Act & Assert
        mockMvc.perform(get("/productos/buscar")
                .param("nombre", "Laptop")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList").isArray())
                .andExpect(jsonPath("$.dataList[0].attributes.nombre").value("Laptop Gaming"));
    }

    @Test
    void buscarProductosPorPrecio_DebeRetornar200YResultados() throws Exception {
        // Arrange
        List<Producto> productos = Arrays.asList(producto);
        when(productoService.buscarProductosPorPrecio(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(ProductoResponse.fromProductos(productos));

        // Act & Assert
        mockMvc.perform(get("/productos/precio")
                .param("precioMin", "1000")
                .param("precioMax", "1500")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList").isArray())
                .andExpect(jsonPath("$.dataList[0].attributes.precio").value(1299.99));
    }

    @Test
    void actualizarProducto_DebeRetornar200YProductoActualizado() throws Exception {
        // Arrange
        Producto productoActualizado = new Producto();
        productoActualizado.setId(1L);
        productoActualizado.setNombre("Laptop Gaming Pro");
        productoActualizado.setPrecio(new BigDecimal("1499.99"));
        productoActualizado.setDescripcion("Laptop para gaming de alto rendimiento - Versión Pro");
        productoActualizado.setFechaCreacion(LocalDateTime.now());
        productoActualizado.setFechaActualizacion(LocalDateTime.now());
        productoActualizado.setVersion(2L);

        when(productoService.actualizarProducto(eq(1L), any(ProductoRequest.class)))
                .thenReturn(ProductoResponse.fromProducto(productoActualizado));

        // Act & Assert
        mockMvc.perform(put("/productos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.attributes.nombre").value("Laptop Gaming Pro"))
                .andExpect(jsonPath("$.data.attributes.precio").value(1499.99));
    }

    @Test
    void actualizarProducto_DebeRetornar404SiNoExiste() throws Exception {
        // Arrange
        when(productoService.actualizarProducto(eq(999L), any(ProductoRequest.class)))
                .thenThrow(new ResourceNotFoundException("Producto no encontrado"));

        // Act & Assert
        mockMvc.perform(put("/productos/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].status").value("404"))
                .andExpect(jsonPath("$.errors[0].title").value("Not Found"));
    }

    @Test
    void eliminarProducto_DebeRetornar204() throws Exception {
        // Arrange
        doNothing().when(productoService).eliminarProducto(1L);

        // Act & Assert
        mockMvc.perform(delete("/productos/1"))
                .andExpect(status().isNoContent());

        verify(productoService).eliminarProducto(1L);
    }

    @Test
    void eliminarProducto_DebeRetornar404SiNoExiste() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Producto no encontrado"))
                .when(productoService).eliminarProducto(999L);

        // Act & Assert
        mockMvc.perform(delete("/productos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0].status").value("404"))
                .andExpect(jsonPath("$.errors[0].title").value("Not Found"));
    }

    @Test
    void existeProducto_DebeRetornar200YTrue() throws Exception {
        // Arrange
        when(productoService.existeProducto(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/productos/1/existe"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void existeProducto_DebeRetornar200YFalse() throws Exception {
        // Arrange
        when(productoService.existeProducto(999L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/productos/999/existe"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void contarProductos_DebeRetornar200YNumero() throws Exception {
        // Arrange
        when(productoService.contarProductos()).thenReturn(5L);

        // Act & Assert
        mockMvc.perform(get("/productos/contar"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void health_DebeRetornar200() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/productos/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Productos Service is running"));
    }

    @Test
    void crearProducto_DebeValidarDatosRequeridos() throws Exception {
        // Arrange
        ProductoRequest requestInvalido = new ProductoRequest();
        requestInvalido.setData(new ProductoRequest.Data());
        requestInvalido.getData().setType("productos");
        requestInvalido.getData().setAttributes(new ProductoRequest.Data.Attributes());
        // Sin nombre ni precio

        when(productoService.crearProducto(any(ProductoRequest.class)))
                .thenThrow(new IllegalArgumentException("El nombre y precio son requeridos"));

        // Act & Assert
        mockMvc.perform(post("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearProducto_DebeValidarPrecioPositivo() throws Exception {
        // Arrange
        ProductoRequest requestInvalido = new ProductoRequest();
        requestInvalido.setData(new ProductoRequest.Data());
        requestInvalido.getData().setType("productos");
        requestInvalido.getData().setAttributes(new ProductoRequest.Data.Attributes());
        requestInvalido.getData().getAttributes().setNombre("Producto Test");
        requestInvalido.getData().getAttributes().setPrecio(new BigDecimal("-10.00")); // Precio negativo

        when(productoService.crearProducto(any(ProductoRequest.class)))
                .thenThrow(new IllegalArgumentException("El precio debe ser positivo"));

        // Act & Assert
        mockMvc.perform(post("/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }
}
