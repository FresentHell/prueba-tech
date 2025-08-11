package com.microservicios.productos.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manejador global de excepciones para el microservicio de productos.
 * Proporciona respuestas consistentes siguiendo el estándar JSON API.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de validación de argumentos.
     * 
     * @param ex Excepción de validación
     * @return ResponseEntity con errores de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.warn("Error de validación: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                "Los datos proporcionados no son válidos",
                LocalDateTime.now(),
                errors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja excepciones de argumentos ilegales.
     * 
     * @param ex Excepción de argumento ilegal
     * @return ResponseEntity con el error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Argumento ilegal: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Argumento ilegal",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja excepciones de tipo de argumento incorrecto.
     * 
     * @param ex Excepción de tipo de argumento
     * @return ResponseEntity con el error
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        logger.warn("Tipo de argumento incorrecto: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Tipo de argumento incorrecto",
                "El parámetro '" + ex.getName() + "' debe ser de tipo " + ex.getRequiredType().getSimpleName(),
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja excepciones de mensaje HTTP no legible.
     * 
     * @param ex Excepción de mensaje no legible
     * @return ResponseEntity con el error
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        logger.warn("Mensaje HTTP no legible: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Mensaje no legible",
                "El cuerpo de la solicitud no es válido",
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja excepciones de recurso no encontrado.
     * 
     * @param ex Excepción de recurso no encontrado
     * @return ResponseEntity con el error
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Recurso no encontrado: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja excepciones de conflicto de recursos.
     * 
     * @param ex Excepción de conflicto
     * @return ResponseEntity con el error
     */
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponse> handleResourceConflictException(ResourceConflictException ex) {
        logger.warn("Conflicto de recurso: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Maneja excepciones genéricas no manejadas.
     * 
     * @param ex Excepción genérica
     * @return ResponseEntity con el error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Error interno del servidor: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                "Ha ocurrido un error inesperado",
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Clase interna para representar respuestas de error siguiendo JSON API.
     */
    public static class ErrorResponse {
        private List<Error> errors;

        public ErrorResponse(int status, String title, String detail, LocalDateTime timestamp) {
            this.errors = new ArrayList<>();
            this.errors.add(new Error(status, title, detail, timestamp));
        }

        public ErrorResponse(int status, String title, String detail, LocalDateTime timestamp, 
                           Map<String, String> errors) {
            this.errors = new ArrayList<>();
            this.errors.add(new Error(status, title, detail, timestamp, errors));
        }

        public List<Error> getErrors() {
            return errors;
        }

        public void setErrors(List<Error> errors) {
            this.errors = errors;
        }

        /**
         * Clase interna para representar un error individual.
         */
        public static class Error {
            private int status;
            private String title;
            private String detail;
            private LocalDateTime timestamp;
            private Map<String, String> errors;

            public Error(int status, String title, String detail, LocalDateTime timestamp) {
                this.status = status;
                this.title = title;
                this.detail = detail;
                this.timestamp = timestamp;
            }

            public Error(int status, String title, String detail, LocalDateTime timestamp, 
                        Map<String, String> errors) {
                this.status = status;
                this.title = title;
                this.detail = detail;
                this.timestamp = timestamp;
                this.errors = errors;
            }

            // Getters y Setters
            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getDetail() {
                return detail;
            }

            public void setDetail(String detail) {
                this.detail = detail;
            }

            public LocalDateTime getTimestamp() {
                return timestamp;
            }

            public void setTimestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
            }

            public Map<String, String> getErrors() {
                return errors;
            }

            public void setErrors(Map<String, String> errors) {
                this.errors = errors;
            }
        }
    }
}
