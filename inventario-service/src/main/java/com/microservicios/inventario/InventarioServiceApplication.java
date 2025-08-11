package com.microservicios.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Clase principal del microservicio de inventario.
 * 
 * Este microservicio es responsable de:
 * - Gestión de inventario de productos
 * - Proceso de compras con verificación de disponibilidad
 * - Comunicación con el microservicio de productos
 * - Emisión de eventos de cambio de inventario
 * - Registro de historial de compras
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class InventarioServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventarioServiceApplication.class, args);
    }
}
