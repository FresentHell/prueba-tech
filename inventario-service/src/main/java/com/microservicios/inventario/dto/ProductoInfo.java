package com.microservicios.inventario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * DTO para la información del producto que viene del microservicio de productos.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
public class ProductoInfo {

    private Data data;

    public static class Data {
        @JsonProperty("type")
        private String type;

        @JsonProperty("id")
        private String id;

        @JsonProperty("attributes")
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

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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
