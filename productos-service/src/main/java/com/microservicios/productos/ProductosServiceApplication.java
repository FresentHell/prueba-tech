package com.microservicios.productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Clase principal del microservicio de productos.
 * 
 * Este microservicio es responsable de:
 * - Gestión CRUD de productos
 * - Validación de datos de productos
 * - Persistencia en base de datos PostgreSQL
 * - Comunicación con el microservicio de inventario
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
public class ProductosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductosServiceApplication.class, args);
    }
}
