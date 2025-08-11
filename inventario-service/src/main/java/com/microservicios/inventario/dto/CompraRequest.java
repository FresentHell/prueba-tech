package com.microservicios.inventario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para las solicitudes de compra.
 * Sigue el estándar JSON API para la estructura de datos.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
public class CompraRequest {

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
            @NotNull(message = "El ID del producto es obligatorio")
            private Long productoId;

            @NotNull(message = "La cantidad es obligatoria")
            @Positive(message = "La cantidad debe ser mayor a cero")
            private Integer cantidad;

            // Getters y Setters
            public Long getProductoId() {
                return productoId;
            }

            public void setProductoId(Long productoId) {
                this.productoId = productoId;
            }

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
