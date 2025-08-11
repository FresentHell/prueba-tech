package com.microservicios.inventario.client;

import com.microservicios.inventario.dto.ProductoInfo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Cliente HTTP para comunicarse con el microservicio de productos.
 * Implementa Circuit Breaker y Retry para resiliencia.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@Component
public class ProductosClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductosClient.class);

    private final WebClient webClient;
    private final String apiKey;

    public ProductosClient(@Value("${productos.service.url}") String productosServiceUrl,
                          @Value("${productos.service.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(productosServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-API-Key", apiKey)
                .build();
    }

    /**
     * Obtiene información de un producto por su ID.
     * 
     * @param productoId ID del producto
     * @return Mono con la información del producto
     */
    @CircuitBreaker(name = "productosService", fallbackMethod = "obtenerProductoFallback")
    @Retry(name = "productosService")
    public Mono<ProductoInfo> obtenerProducto(Long productoId) {
        logger.debug("Consultando producto con ID: {}", productoId);

        return webClient.get()
                .uri("/productos/{id}", productoId)
                .retrieve()
                .bodyToMono(ProductoInfo.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(producto -> logger.debug("Producto obtenido: {}", producto.getData().getAttributes().getNombre()))
                .doOnError(error -> logger.error("Error al obtener producto con ID {}: {}", productoId, error.getMessage()));
    }

    /**
     * Verifica si un producto existe.
     * 
     * @param productoId ID del producto
     * @return Mono con true si existe, false en caso contrario
     */
    @CircuitBreaker(name = "productosService", fallbackMethod = "existeProductoFallback")
    @Retry(name = "productosService")
    public Mono<Boolean> existeProducto(Long productoId) {
        logger.debug("Verificando existencia del producto con ID: {}", productoId);

        return webClient.get()
                .uri("/productos/{id}/existe", productoId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(existe -> logger.debug("Producto con ID {} existe: {}", productoId, existe))
                .doOnError(error -> logger.error("Error al verificar existencia del producto con ID {}: {}", productoId, error.getMessage()));
    }

    /**
     * Método de fallback para obtenerProducto cuando el Circuit Breaker está abierto.
     * 
     * @param productoId ID del producto
     * @param exception Excepción que causó el fallback
     * @return Mono con un ProductoInfo vacío
     */
    public Mono<ProductoInfo> obtenerProductoFallback(Long productoId, Exception exception) {
        logger.warn("Fallback: No se pudo obtener el producto con ID {}: {}", productoId, exception.getMessage());
        
        // Retornar un ProductoInfo vacío para que el flujo continúe
        ProductoInfo productoInfo = new ProductoInfo();
        ProductoInfo.Data data = new ProductoInfo.Data();
        ProductoInfo.Data.Attributes attributes = new ProductoInfo.Data.Attributes();
        
        data.setId(String.valueOf(productoId));
        attributes.setNombre("Producto no disponible");
        attributes.setPrecio(java.math.BigDecimal.ZERO);
        attributes.setDescripcion("Información del producto no disponible temporalmente");
        
        data.setAttributes(attributes);
        productoInfo.setData(data);
        
        return Mono.just(productoInfo);
    }

    /**
     * Método de fallback para existeProducto cuando el Circuit Breaker está abierto.
     * 
     * @param productoId ID del producto
     * @param exception Excepción que causó el fallback
     * @return Mono con false (asumiendo que no existe)
     */
    public Mono<Boolean> existeProductoFallback(Long productoId, Exception exception) {
        logger.warn("Fallback: No se pudo verificar la existencia del producto con ID {}: {}", productoId, exception.getMessage());
        return Mono.just(false);
    }

    /**
     * Maneja errores específicos de WebClient.
     * 
     * @param error Error de WebClient
     * @return Mono con el error procesado
     */
    private Mono<Throwable> handleWebClientError(Throwable error) {
        if (error instanceof WebClientResponseException) {
            WebClientResponseException wcre = (WebClientResponseException) error;
            logger.error("Error HTTP {} al comunicarse con productos service: {}", 
                        wcre.getStatusCode(), wcre.getResponseBodyAsString());
            
            if (wcre.getStatusCode().is4xxClientError()) {
                return Mono.error(new IllegalArgumentException("Producto no encontrado"));
            } else if (wcre.getStatusCode().is5xxServerError()) {
                return Mono.error(new RuntimeException("Error interno del servicio de productos"));
            }
        }
        
        return Mono.error(error);
    }
}
