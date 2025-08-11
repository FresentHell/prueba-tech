package com.microservicios.inventario.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservicios.inventario.dto.*;
import com.microservicios.inventario.exception.GlobalExceptionHandler;
import com.microservicios.inventario.model.Inventario;
import com.microservicios.inventario.service.InventarioService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private InventarioController inventarioController;

    private ObjectMapper objectMapper;
    private Inventario inventario;
    private CompraRequest compraRequest;
    private InventarioRequest inventarioRequest;
    private InventarioResponse inventarioResponse;
    private CompraResponse compraResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(inventarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProductoId(1L);
        inventario.setCantidad(10);
        inventario.setFechaCreacion(LocalDateTime.now());
        inventario.setFechaActualizacion(LocalDateTime.now());
        inventario.setVersion(1L);

        // Configurar InventarioResponse
        inventarioResponse = new InventarioResponse();
        InventarioResponse.Data data = new InventarioResponse.Data();
        InventarioResponse.Data.Attributes attributes = new InventarioResponse.Data.Attributes();
        attributes.setProductoId(1L);
        attributes.setCantidad(10);
        data.setId("1");
        data.setType("inventario");
        data.setAttributes(attributes);
        inventarioResponse.setData(data);

        // Configurar CompraResponse
        compraResponse = new CompraResponse();
        CompraResponse.Data compraData = new CompraResponse.Data();
        CompraResponse.Data.Attributes compraAttributes = new CompraResponse.Data.Attributes();
        compraAttributes.setProductoId(1L);
        compraAttributes.setCantidad(2);
        compraAttributes.setPrecioUnitario(new BigDecimal("1299.99"));
        compraAttributes.setPrecioTotal(new BigDecimal("2599.98"));
        compraAttributes.setInventarioRestante(8);
        compraData.setId("1");
        compraData.setType("compras");
        compraData.setAttributes(compraAttributes);
        compraResponse.setData(compraData);

        // Configurar CompraRequest
        compraRequest = new CompraRequest();
        CompraRequest.Data compraReqData = new CompraRequest.Data();
        CompraRequest.Data.Attributes compraReqAttributes = new CompraRequest.Data.Attributes();
        compraReqAttributes.setProductoId(1L);
        compraReqAttributes.setCantidad(2);
        compraReqData.setAttributes(compraReqAttributes);
        compraReqData.setType("compras");
        compraRequest.setData(compraReqData);

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
    void consultarInventario_DebeRetornar200YInventario() throws Exception {
        // Arrange
        when(inventarioService.consultarInventario(1L)).thenReturn(inventarioResponse);

        // Act & Assert
        mockMvc.perform(get("/inventario/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("inventario"))
                .andExpect(jsonPath("$.data.attributes.productoId").value(1))
                .andExpect(jsonPath("$.data.attributes.cantidad").value(10));
    }

    @Test
    void consultarInventario_DebeRetornar400SiNoExiste() throws Exception {
        // Arrange
        when(inventarioService.consultarInventario(999L)).thenThrow(new IllegalArgumentException("Inventario no encontrado"));

        // Act & Assert
        mockMvc.perform(get("/inventario/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarInventario_DebeRetornar200YInventarioActualizado() throws Exception {
        // Arrange
        when(inventarioService.actualizarInventario(eq(1L), any(InventarioRequest.class))).thenReturn(inventarioResponse);

        // Act & Assert
        mockMvc.perform(patch("/inventario/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventarioRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("inventario"));
    }

    @Test
    void actualizarInventario_DebeRetornar400SiCantidadNegativa() throws Exception {
        // Arrange
        InventarioRequest request = new InventarioRequest();
        InventarioRequest.Data data = new InventarioRequest.Data();
        InventarioRequest.Data.Attributes attributes = new InventarioRequest.Data.Attributes();
        attributes.setCantidad(-5);
        data.setAttributes(attributes);
        data.setType("inventario");
        request.setData(data);

        when(inventarioService.actualizarInventario(eq(1L), any(InventarioRequest.class)))
                .thenThrow(new IllegalArgumentException("La cantidad no puede ser negativa"));

        // Act & Assert
        mockMvc.perform(patch("/inventario/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void realizarCompra_DebeRetornar200YCompraRealizada() throws Exception {
        // Arrange
        when(inventarioService.realizarCompra(any(CompraRequest.class))).thenReturn(compraResponse);

        // Act & Assert
        mockMvc.perform(post("/inventario/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("compras"))
                .andExpect(jsonPath("$.data.attributes.productoId").value(1))
                .andExpect(jsonPath("$.data.attributes.cantidad").value(2));
    }

    @Test
    void realizarCompra_DebeRetornar400SiProductoNoExiste() throws Exception {
        // Arrange
        when(inventarioService.realizarCompra(any(CompraRequest.class)))
                .thenThrow(new IllegalArgumentException("Producto no encontrado"));

        // Act & Assert
        mockMvc.perform(post("/inventario/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void realizarCompra_DebeRetornar400SiInventarioInsuficiente() throws Exception {
        // Arrange
        when(inventarioService.realizarCompra(any(CompraRequest.class)))
                .thenThrow(new IllegalArgumentException("Inventario insuficiente"));

        // Act & Assert
        mockMvc.perform(post("/inventario/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compraRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void realizarCompra_DebeRetornar400SiCantidadNegativa() throws Exception {
        // Arrange
        CompraRequest request = new CompraRequest();
        CompraRequest.Data data = new CompraRequest.Data();
        CompraRequest.Data.Attributes attributes = new CompraRequest.Data.Attributes();
        attributes.setProductoId(1L);
        attributes.setCantidad(-1);
        data.setAttributes(attributes);
        data.setType("compras");
        request.setData(data);

        // Act & Assert
        // La validación se hace en el servicio, no necesitamos mock
        mockMvc.perform(post("/inventario/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerTodosLosInventarios_DebeRetornar200YLista() throws Exception {
        // Arrange
        InventarioResponse response = new InventarioResponse();
        response.setDataList(Arrays.asList(inventarioResponse.getData()));
        when(inventarioService.obtenerTodosLosInventarios()).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/inventario")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList").isArray())
                .andExpect(jsonPath("$.dataList[0].type").value("inventario"));
    }

    @Test
    void obtenerInventariosBajos_DebeRetornar200YResultados() throws Exception {
        // Arrange
        InventarioResponse response = new InventarioResponse();
        response.setDataList(Arrays.asList(inventarioResponse.getData()));
        when(inventarioService.obtenerInventariosBajos(anyInt())).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/inventario/bajos?cantidad=5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList").isArray());
    }

    @Test
    void obtenerProductosSinInventario_DebeRetornar200YResultados() throws Exception {
        // Arrange
        InventarioResponse response = new InventarioResponse();
        response.setDataList(Arrays.asList(inventarioResponse.getData()));
        when(inventarioService.obtenerProductosSinInventario()).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/inventario/sin-stock")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataList").isArray());
    }

    @Test
    void crearInventario_DebeRetornar201YInventarioCreado() throws Exception {
        // Arrange
        when(inventarioService.crearInventario(eq(1L), eq(10))).thenReturn(inventarioResponse);

        // Act & Assert
        mockMvc.perform(post("/inventario/1")
                .param("cantidad", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.type").value("inventario"));
    }

    @Test
    void obtenerEstadisticasInventario_DebeRetornar200YEstadisticas() throws Exception {
        // Arrange
        when(inventarioService.obtenerEstadisticasInventario()).thenReturn("Estadísticas del inventario");

        // Act & Assert
        mockMvc.perform(get("/inventario/estadisticas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Estadísticas del inventario"));
    }

    @Test
    void consultarInventario_DebeValidarIdProducto() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/inventario/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarInventario_DebeValidarDatosRequeridos() throws Exception {
        // Arrange
        InventarioRequest request = new InventarioRequest();
        request.setData(null); // Data es null

        // Act & Assert
        mockMvc.perform(patch("/inventario/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void realizarCompra_DebeValidarDatosRequeridos() throws Exception {
        // Arrange
        CompraRequest request = new CompraRequest();
        request.setData(null); // Data es null

        // Act & Assert
        mockMvc.perform(post("/inventario/compras")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
