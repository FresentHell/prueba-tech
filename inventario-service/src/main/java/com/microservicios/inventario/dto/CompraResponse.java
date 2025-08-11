package com.microservicios.inventario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microservicios.inventario.model.HistorialCompras;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para las respuestas de compra siguiendo el estándar JSON API.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
public class CompraResponse {

    private Data data;

    public static class Data {
        @JsonProperty("type")
        private String type = "compras";

        @JsonProperty("id")
        private String id;

        @JsonProperty("attributes")
        private Attributes attributes;

        public static class Attributes {
            private Long productoId;
            private String nombreProducto;
            private Integer cantidad;
            private BigDecimal precioUnitario;
            private BigDecimal precioTotal;
            private LocalDateTime fechaCompra;
            private Integer inventarioRestante;

            // Getters y Setters
            public Long getProductoId() {
                return productoId;
            }

            public void setProductoId(Long productoId) {
                this.productoId = productoId;
            }

            public String getNombreProducto() {
                return nombreProducto;
            }

            public void setNombreProducto(String nombreProducto) {
                this.nombreProducto = nombreProducto;
            }

            public Integer getCantidad() {
                return cantidad;
            }

            public void setCantidad(Integer cantidad) {
                this.cantidad = cantidad;
            }

            public BigDecimal getPrecioUnitario() {
                return precioUnitario;
            }

            public void setPrecioUnitario(BigDecimal precioUnitario) {
                this.precioUnitario = precioUnitario;
            }

            public BigDecimal getPrecioTotal() {
                return precioTotal;
            }

            public void setPrecioTotal(BigDecimal precioTotal) {
                this.precioTotal = precioTotal;
            }

            public LocalDateTime getFechaCompra() {
                return fechaCompra;
            }

            public void setFechaCompra(LocalDateTime fechaCompra) {
                this.fechaCompra = fechaCompra;
            }

            public Integer getInventarioRestante() {
                return inventarioRestante;
            }

            public void setInventarioRestante(Integer inventarioRestante) {
                this.inventarioRestante = inventarioRestante;
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

    // Métodos de construcción estáticos
    public static CompraResponse fromHistorialCompras(HistorialCompras historialCompras, Integer inventarioRestante) {
        CompraResponse response = new CompraResponse();
        Data data = new Data();
        Data.Attributes attributes = new Data.Attributes();

        data.setId(String.valueOf(historialCompras.getId()));
        attributes.setProductoId(historialCompras.getProductoId());
        attributes.setNombreProducto(historialCompras.getNombreProducto());
        attributes.setCantidad(historialCompras.getCantidad());
        attributes.setPrecioUnitario(historialCompras.getPrecioUnitario());
        attributes.setPrecioTotal(historialCompras.getPrecioTotal());
        attributes.setFechaCompra(historialCompras.getFechaCompra());
        attributes.setInventarioRestante(inventarioRestante);

        data.setAttributes(attributes);
        response.setData(data);

        return response;
    }

    // Getters y Setters
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
