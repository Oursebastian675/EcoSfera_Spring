package com.example.EcoSfera.config;

import java.util.List;
// Considera añadir anotaciones de validación (javax.validation.constraints o jakarta.validation.constraints)
// import jakarta.validation.Valid;
// import jakarta.validation.constraints.NotEmpty;
// import jakarta.validation.constraints.NotNull;

// DTO principal para la solicitud de nueva venta
public class NuevaVentaRequestDTO {

    // @NotNull // Ejemplo de validación
    private Long userId; // Descomentado para que el servicio lo pueda usar

    // @Valid // Para validar los objetos anidados
    // @NotNull
    private InformacionEnvioDTO informacionEnvio;

    // @NotEmpty // Para asegurar que la lista de items no esté vacía
    // @Valid
    private List<ItemVentaDTO> items;
    private String metodoPago;

    // Getters y Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public InformacionEnvioDTO getInformacionEnvio() {
        return informacionEnvio;
    }

    public void setInformacionEnvio(InformacionEnvioDTO informacionEnvio) {
        this.informacionEnvio = informacionEnvio;
    }

    public List<ItemVentaDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemVentaDTO> items) {
        this.items = items;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}