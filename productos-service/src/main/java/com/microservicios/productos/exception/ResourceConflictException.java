package com.microservicios.productos.exception;

/**
 * Excepción lanzada cuando hay un conflicto con un recurso.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }

    public ResourceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
