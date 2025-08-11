package com.microservicios.inventario.exception;

/**
 * Excepción lanzada cuando un producto no se encuentra.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException(String message) {
        super(message);
    }

    public ProductoNoEncontradoException(String message, Throwable cause) {
        super(message, cause);
    }
}
