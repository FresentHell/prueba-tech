package com.microservicios.inventario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para habilitar la ejecución asíncrona en el microservicio de inventario.
 * Permite el procesamiento asíncrono de eventos y tareas en segundo plano.
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
        executor.setThreadNamePrefix("InventarioAsync-");
        
        // Configurar el comportamiento de rechazo de tareas
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // Inicializar el executor
        executor.initialize();
        
        return executor;
    }

    /**
     * Configura el executor para eventos de inventario.
     * 
     * @return Executor configurado para eventos
     */
    @Bean(name = "eventExecutor")
    public Executor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Configurar el número de hilos del pool para eventos
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        
        // Configurar el nombre de los hilos para debugging
        executor.setThreadNamePrefix("InventarioEvent-");
        
        // Configurar el comportamiento de rechazo de tareas
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        // Inicializar el executor
        executor.initialize();
        
        return executor;
    }
}
