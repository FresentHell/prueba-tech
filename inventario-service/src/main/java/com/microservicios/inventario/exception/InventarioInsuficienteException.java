package com.microservicios.inventario.exception;

/**
 * Excepción lanzada cuando no hay suficiente inventario para realizar una operación.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
public class InventarioInsuficienteException extends RuntimeException {

    public InventarioInsuficienteException(String message) {
        super(message);
    }

    public InventarioInsuficienteException(String message, Throwable cause) {
        super(message, cause);
    }
}
