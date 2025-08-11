package com.microservicios.productos.exception;

/**
 * Excepción lanzada cuando un recurso no se encuentra.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
