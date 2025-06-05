// C:/Users/USUARIO/Documents/Sebastian/Trabajos/EcoSfera_Spring/src/main/java/com/example/EcoSfera/config/LoginRequestDTO.java
package com.example.EcoSfera.config;

public class LoginRequestDTO {
    private String credencial; // Este campo recibirá el email o el nombre de usuario
    private String contrasena;

    // Getters y Setters
    public String getCredencial() {
        return credencial;
    }

    public void setCredencial(String credencial) {
        this.credencial = credencial;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
