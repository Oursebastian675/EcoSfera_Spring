package com.example.EcoSfera.modelos;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime fechaVenta;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonBackReference
    private Usuario usuario;

    private Double totalVenta;

    // Nuevos campos para información de envío y pago
    private String nombreCliente; // Nombre + Apellido del DTO de envío
    private String direccionEnvio;
    private String telefonoEnvio;
    private String tipoDocumentoEnvio;
    private String numeroDocumentoEnvio;
    private String metodoPago;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Double getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(Double totalVenta) {
        this.totalVenta = totalVenta;
    }

    // Getters y Setters para los nuevos campos
    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getDireccionEnvio() {
        return direccionEnvio;
    }

    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public String getTelefonoEnvio() {
        return telefonoEnvio;
    }

    public void setTelefonoEnvio(String telefonoEnvio) {
        this.telefonoEnvio = telefonoEnvio;
    }

    public String getTipoDocumentoEnvio() {
        return tipoDocumentoEnvio;
    }

    public void setTipoDocumentoEnvio(String tipoDocumentoEnvio) {
        this.tipoDocumentoEnvio = tipoDocumentoEnvio;
    }

    public String getNumeroDocumentoEnvio() {
        return numeroDocumentoEnvio;
    }

    public void setNumeroDocumentoEnvio(String numeroDocumentoEnvio) {
        this.numeroDocumentoEnvio = numeroDocumentoEnvio;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}