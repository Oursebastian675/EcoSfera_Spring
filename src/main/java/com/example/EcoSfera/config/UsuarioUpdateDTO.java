package com.example.EcoSfera.config;

import lombok.Data;

@Data
public class UsuarioUpdateDTO {
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String edad;
    private String usuario;
    private String contrasena;
}