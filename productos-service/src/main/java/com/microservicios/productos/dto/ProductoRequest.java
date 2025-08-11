package com.microservicios.productos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para las solicitudes de creación y actualización de productos.
 * Sigue el estándar JSON API para la estructura de datos.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
public class ProductoRequest {

    @NotNull(message = "El objeto data es obligatorio")
    @Valid
    private Data data;

    public static class Data {
        @JsonProperty("type")
        private String type;

        @JsonProperty("attributes")
        @Valid
        private Attributes attributes;

        public static class Attributes {
            private String nombre;
            private BigDecimal precio;
            private String descripcion;

            // Getters y Setters
            public String getNombre() {
                return nombre;
            }

            public void setNombre(String nombre) {
                this.nombre = nombre;
            }

            public BigDecimal getPrecio() {
                return precio;
            }

            public void setPrecio(BigDecimal precio) {
                this.precio = precio;
            }

            public String getDescripcion() {
                return descripcion;
            }

            public void setDescripcion(String descripcion) {
                this.descripcion = descripcion;
            }
        }

        // Getters y Setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Attributes getAttributes() {
            return attributes;
        }

        public void setAttributes(Attributes attributes) {
            this.attributes = attributes;
        }
    }

    // Getters y Setters
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
