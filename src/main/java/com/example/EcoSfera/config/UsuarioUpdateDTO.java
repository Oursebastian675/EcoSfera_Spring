package com.example.EcoSfera.config;

import lombok.Data;

@Data
public class UsuarioUpdateDTO {
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String edad;
    private String usuario; // Nombre de usuario
    private String contrasena; // Nueva contraseña (opcional)
    // Si quisieras forzar la verificación de la contraseña actual antes de cambiarla,
    // podrías añadir: private String contrasenaActual;
}