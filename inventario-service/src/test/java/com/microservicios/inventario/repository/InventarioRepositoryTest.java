package com.microservicios.inventario.repository;

import com.microservicios.inventario.model.Inventario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class InventarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InventarioRepository inventarioRepository;

    private Inventario inventario;

    @BeforeEach
    void setUp() {
        inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidad(10);
        inventario.setFechaCreacion(LocalDateTime.now());
        inventario.setFechaActualizacion(LocalDateTime.now());
    }

    @Test
    void save_DebeGuardarInventarioCorrectamente() {
        // Act
        Inventario saved = inventarioRepository.save(inventario);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(1L, saved.getProductoId());
        assertEquals(10, saved.getCantidad());
    }

    @Test
    void save_DebeActualizarInventarioExistente() {
        // Arrange
        Inventario saved = inventarioRepository.save(inventario);
        saved.setCantidad(20);

        // Act
        Inventario updated = inventarioRepository.save(saved);

        // Assert
        assertEquals(20, updated.getCantidad());
        assertEquals(saved.getId(), updated.getId());
    }

    @Test
    void findById_DebeRetornarInventarioCuandoExiste() {
        // Arrange
        Inventario saved = inventarioRepository.save(inventario);

        // Act
        Optional<Inventario> found = inventarioRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_DebeRetornarEmptyCuandoNoExiste() {
        // Act
        Optional<Inventario> found = inventarioRepository.findById(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void findByProductoId_DebeRetornarInventarioCuandoExiste() {
        // Arrange
        inventarioRepository.save(inventario);

        // Act
        Optional<Inventario> found = inventarioRepository.findByProductoId(1L);

        // Assert
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getProductoId());
    }

    @Test
    void findByProductoId_DebeRetornarEmptyCuandoNoExiste() {
        // Act
        Optional<Inventario> found = inventarioRepository.findByProductoId(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void findAll_DebeRetornarTodosLosInventarios() {
        // Arrange
        inventarioRepository.save(inventario);
        
        Inventario inventario2 = new Inventario();
        inventario2.setProductoId(2L);
        inventario2.setCantidad(5);
        inventario2.setFechaCreacion(LocalDateTime.now());
        inventario2.setFechaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario2);

        // Act
        List<Inventario> all = inventarioRepository.findAll();

        // Assert
        assertTrue(all.size() >= 2);
    }

    @Test
    void delete_DebeEliminarInventario() {
        // Arrange
        Inventario saved = inventarioRepository.save(inventario);

        // Act
        inventarioRepository.delete(saved);

        // Assert
        Optional<Inventario> found = inventarioRepository.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void existsByProductoId_DebeRetornarTrueCuandoExiste() {
        // Arrange
        inventarioRepository.save(inventario);

        // Act
        boolean exists = inventarioRepository.existsByProductoId(1L);

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByProductoId_DebeRetornarFalseCuandoNoExiste() {
        // Act
        boolean exists = inventarioRepository.existsByProductoId(999L);

        // Assert
        assertFalse(exists);
    }

    @Test
    void countInventarios_DebeRetornarNumeroCorrecto() {
        // Arrange
        inventarioRepository.save(inventario);
        
        Inventario inventario2 = new Inventario();
        inventario2.setProductoId(2L);
        inventario2.setCantidad(5);
        inventario2.setFechaCreacion(LocalDateTime.now());
        inventario2.setFechaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario2);

        // Act
        long count = inventarioRepository.countInventarios();

        // Assert
        assertTrue(count >= 2);
    }

    @Test
    void sumCantidadTotal_DebeRetornarSumaCorrecta() {
        // Arrange
        inventarioRepository.save(inventario);
        
        Inventario inventario2 = new Inventario();
        inventario2.setProductoId(2L);
        inventario2.setCantidad(5);
        inventario2.setFechaCreacion(LocalDateTime.now());
        inventario2.setFechaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario2);

        // Act
        Integer sum = inventarioRepository.sumCantidadTotal();

        // Assert
        assertTrue(sum >= 15); // 10 + 5
    }

    @Test
    void findByCantidadLessThan_DebeRetornarInventariosConStockBajo() {
        // Arrange
        inventarioRepository.save(inventario);
        
        Inventario inventarioBajo = new Inventario();
        inventarioBajo.setProductoId(2L);
        inventarioBajo.setCantidad(3);
        inventarioBajo.setFechaCreacion(LocalDateTime.now());
        inventarioBajo.setFechaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventarioBajo);

        // Act
        List<Inventario> bajos = inventarioRepository.findByCantidadLessThan(5);

        // Assert
        assertTrue(bajos.size() >= 1);
        assertTrue(bajos.stream().allMatch(inv -> inv.getCantidad() < 5));
    }

    @Test
    void findByCantidadZero_DebeRetornarInventariosSinStock() {
        // Arrange
        Inventario inventarioSinStock = new Inventario();
        inventarioSinStock.setProductoId(2L);
        inventarioSinStock.setCantidad(0);
        inventarioSinStock.setFechaCreacion(LocalDateTime.now());
        inventarioSinStock.setFechaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventarioSinStock);

        // Act
        List<Inventario> sinStock = inventarioRepository.findByCantidadZero();

        // Assert
        assertTrue(sinStock.size() >= 1);
        assertTrue(sinStock.stream().allMatch(inv -> inv.getCantidad() == 0));
    }

    @Test
    void findAllOrderByFechaActualizacionDesc_DebeRetornarOrdenados() {
        // Arrange
        inventarioRepository.save(inventario);
        
        Inventario inventario2 = new Inventario();
        inventario2.setProductoId(2L);
        inventario2.setCantidad(5);
        inventario2.setFechaCreacion(LocalDateTime.now());
        inventario2.setFechaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario2);

        // Act
        List<Inventario> ordenados = inventarioRepository.findAllOrderByFechaActualizacionDesc();

        // Assert
        assertTrue(ordenados.size() >= 2);
        // Verificar que están ordenados por fecha de actualización descendente
        for (int i = 0; i < ordenados.size() - 1; i++) {
            assertTrue(ordenados.get(i).getFechaActualizacion()
                    .isAfter(ordenados.get(i + 1).getFechaActualizacion()) ||
                    ordenados.get(i).getFechaActualizacion()
                            .isEqual(ordenados.get(i + 1).getFechaActualizacion()));
        }
    }

    @Test
    void findAllOrderByCantidadAsc_DebeRetornarOrdenados() {
        // Arrange
        inventarioRepository.save(inventario);
        
        Inventario inventario2 = new Inventario();
        inventario2.setProductoId(2L);
        inventario2.setCantidad(5);
        inventario2.setFechaCreacion(LocalDateTime.now());
        inventario2.setFechaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inventario2);

        // Act
        List<Inventario> ordenados = inventarioRepository.findAllOrderByCantidadAsc();

        // Assert
        assertTrue(ordenados.size() >= 2);
        // Verificar que están ordenados por cantidad ascendente
        for (int i = 0; i < ordenados.size() - 1; i++) {
            assertTrue(ordenados.get(i).getCantidad() <= ordenados.get(i + 1).getCantidad());
        }
    }
}
