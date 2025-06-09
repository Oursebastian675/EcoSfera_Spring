package com.example.EcoSfera.config;

// DTO para un item de la venta
public class ItemVentaDTO {
    private Long productoId; // O String si tu ID de producto es String
    private int cantidad;
    // Getters y Setters

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
