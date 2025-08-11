package com.microservicios.productos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microservicios.productos.model.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para las respuestas de productos siguiendo el estándar JSON API.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
public class ProductoResponse {

    private Data data;
    private List<Data> dataList;

    public static class Data {
        @JsonProperty("type")
        private String type = "productos";

        @JsonProperty("id")
        private String id;

        @JsonProperty("attributes")
        private Attributes attributes;

        public static class Attributes {
            private String nombre;
            private BigDecimal precio;
            private String descripcion;
            private LocalDateTime fechaCreacion;
            private LocalDateTime fechaActualizacion;

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

            public LocalDateTime getFechaCreacion() {
                return fechaCreacion;
            }

            public void setFechaCreacion(LocalDateTime fechaCreacion) {
                this.fechaCreacion = fechaCreacion;
            }

            public LocalDateTime getFechaActualizacion() {
                return fechaActualizacion;
            }

            public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
                this.fechaActualizacion = fechaActualizacion;
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
    public static ProductoResponse fromProducto(Producto producto) {
        ProductoResponse response = new ProductoResponse();
        Data data = new Data();
        Data.Attributes attributes = new Data.Attributes();

        data.setId(String.valueOf(producto.getId()));
        attributes.setNombre(producto.getNombre());
        attributes.setPrecio(producto.getPrecio());
        attributes.setDescripcion(producto.getDescripcion());
        attributes.setFechaCreacion(producto.getFechaCreacion());
        attributes.setFechaActualizacion(producto.getFechaActualizacion());

        data.setAttributes(attributes);
        response.setData(data);

        return response;
    }

    public static ProductoResponse fromProductos(List<Producto> productos) {
        ProductoResponse response = new ProductoResponse();
        List<Data> dataList = productos.stream()
                .map(producto -> {
                    Data data = new Data();
                    Data.Attributes attributes = new Data.Attributes();

                    data.setId(String.valueOf(producto.getId()));
                    attributes.setNombre(producto.getNombre());
                    attributes.setPrecio(producto.getPrecio());
                    attributes.setDescripcion(producto.getDescripcion());
                    attributes.setFechaCreacion(producto.getFechaCreacion());
                    attributes.setFechaActualizacion(producto.getFechaActualizacion());

                    data.setAttributes(attributes);
                    return data;
                })
                .toList();

        response.setDataList(dataList);
        return response;
    }

    // Getters y Setters
    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }
}
