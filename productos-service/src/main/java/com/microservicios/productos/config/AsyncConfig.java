package com.microservicios.productos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para habilitar la ejecución asíncrona en el microservicio de productos.
 * Permite el procesamiento asíncrono de tareas en segundo plano.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Configura el executor para tareas asíncronas.
     * 
     * @return Executor configurado para tareas asíncronas
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Configurar el número de hilos del pool
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        
        // Configurar el nombre de los hilos para debugging
        executor.setThreadNamePrefix("ProductosAsync-");
        
        // Configurar el comportamiento de rechazo de tareas
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // Inicializar el executor
        executor.initialize();
        
        return executor;
    }
}
