package com.microservicios.inventario.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microservicios.inventario.model.Inventario;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para las respuestas de inventario siguiendo el estándar JSON API.
 * 
 * @author Microservicios Team
 * @version 1.0.0
 */
public class InventarioResponse {

    private Data data;
    private List<Data> dataList;

    public static class Data {
        @JsonProperty("type")
        private String type = "inventario";

        @JsonProperty("id")
        private String id;

        @JsonProperty("attributes")
        private Attributes attributes;

        public static class Attributes {
            private Long productoId;
            private Integer cantidad;
            private LocalDateTime fechaCreacion;
            private LocalDateTime fechaActualizacion;

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
    public static InventarioResponse fromInventario(Inventario inventario) {
        InventarioResponse response = new InventarioResponse();
        Data data = new Data();
        Data.Attributes attributes = new Data.Attributes();

        data.setId(String.valueOf(inventario.getId()));
        attributes.setProductoId(inventario.getProductoId());
        attributes.setCantidad(inventario.getCantidad());
        attributes.setFechaCreacion(inventario.getFechaCreacion());
        attributes.setFechaActualizacion(inventario.getFechaActualizacion());

        data.setAttributes(attributes);
        response.setData(data);

        return response;
    }

    public static InventarioResponse fromInventarios(List<Inventario> inventarios) {
        InventarioResponse response = new InventarioResponse();
        List<Data> dataList = inventarios.stream()
                .map(inventario -> {
                    Data data = new Data();
                    Data.Attributes attributes = new Data.Attributes();

                    data.setId(String.valueOf(inventario.getId()));
                    attributes.setProductoId(inventario.getProductoId());
                    attributes.setCantidad(inventario.getCantidad());
                    attributes.setFechaCreacion(inventario.getFechaCreacion());
                    attributes.setFechaActualizacion(inventario.getFechaActualizacion());

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
