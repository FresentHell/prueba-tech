package com.microservicios.inventario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para las solicitudes de actualización de inventario.
 * Sigue el estándar JSON API para la estructura de datos.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
public class InventarioRequest {

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
            private Integer cantidad;

            // Getters y Setters
            public Integer getCantidad() {
                return cantidad;
            }

            public void setCantidad(Integer cantidad) {
                this.cantidad = cantidad;
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
