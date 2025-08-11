package com.microservicios.productos.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Configuración para WebClient y Circuit Breaker en el microservicio de productos.
 * Proporciona configuración para comunicación HTTP resiliente con otros servicios.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@Configuration
public class WebClientConfig {

    @Value("${inventario.service.url}")
    private String inventarioServiceUrl;

    @Value("${inventario.service.api-key}")
    private String inventarioServiceApiKey;

    /**
     * Configura WebClient para comunicación con el microservicio de inventario.
     * 
     * @return WebClient configurado
     */
    @Bean
    public WebClient inventarioWebClient() {
        return WebClient.builder()
                .baseUrl(inventarioServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-API-Key", inventarioServiceApiKey)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB
                .build();
    }

    /**
     * Configura el Circuit Breaker Factory con configuración personalizada.
     * 
     * @return Customizer para el Circuit Breaker Factory
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .minimumNumberOfCalls(5)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .slowCallRateThreshold(50)
                        .slowCallDurationThreshold(Duration.ofSeconds(2))
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(5))
                        .build())
                .build());
    }

    /**
     * Configuración específica para el Circuit Breaker del servicio de inventario.
     * 
     * @return Customizer para el Circuit Breaker del servicio de inventario
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> inventarioServiceCustomizer() {
        return factory -> factory.configure(builder -> builder
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .minimumNumberOfCalls(5)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .permittedNumberOfCallsInHalfOpenState(3)
                        .slowCallRateThreshold(50)
                        .slowCallDurationThreshold(Duration.ofSeconds(3))
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(5))
                        .build())
                .build(), "inventarioService");
    }
}
