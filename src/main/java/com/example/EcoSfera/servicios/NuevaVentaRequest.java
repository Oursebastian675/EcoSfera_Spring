package com.example.EcoSfera.servicios;

import java.util.List;

public class NuevaVentaRequest {
    private Long usuarioId;
    private List<ItemVenta> items;

    public static class ItemVenta {
        private Long productoId;
        private Integer cantidad;

        // Getters and setters for ItemVenta
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

    // Getters and setters for NuevaVentaRequest
    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<ItemVenta> getItems() {
        return items;
    }

    public void setItems(List<ItemVenta> items) {
        this.items = items;
    }
}