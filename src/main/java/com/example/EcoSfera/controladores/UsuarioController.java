package com.example.EcoSfera.controladores;

import com.example.EcoSfera.modelos.Usuario;
import com.example.EcoSfera.servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.EcoSfera.config.LoginRequestDTO; // Asegúrate de importar el DTO
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {


    @Autowired
    private UsuarioService usuarioService;

    // --- Endpoint para REGISTRO de usuarios ---
    // Este endpoint ya lo tenías y sirve para crear (registrar) un nuevo usuario.
    // C:/Users/USUARIO/Documents/Sebastian/Trabajos/EcoSfera_Spring/src/main/java/com/example/EcoSfera/controladores/UsuarioController.java
// ...
    @PostMapping // Mapea a POST /api/usuarios
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) { // Cambiado a ResponseEntity<?> para mejor manejo de errores
        try {
            // Aquí podrías añadir validaciones a nivel de controlador si prefieres,
            // usando @Valid y anotaciones de Bean Validation en tu entidad Usuario.
            Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
            // Es buena práctica no devolver la contraseña en la respuesta
            nuevoUsuario.setContrasena(null);
            return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
        } catch (RuntimeException e) { // Captura "El email ya está registrado." u otras de validación del servicio
            // Devuelve el mensaje de la excepción como cuerpo de la respuesta 400
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
// ...

    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody LoginRequestDTO loginRequest) {
        Optional<Usuario> usuarioAutenticado = usuarioService.autenticarUsuario(
                loginRequest.getCredencial(), // Esto ya es genérico
                loginRequest.getContrasena()
        );

        if (usuarioAutenticado.isPresent()) {
            Usuario user = usuarioAutenticado.get();
            user.setContrasena(null);
            return ResponseEntity.ok(user);
        } else {
            // Para mejorar la seguridad, no reveles si el usuario o la contraseña fueron incorrectos.
            // Un mensaje genérico es mejor.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenciales inválidas"));
        }
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        // Es buena práctica no exponer las contraseñas, incluso hasheadas, en listados generales
        usuarios.forEach(u -> u.setContrasena(null));
        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
        return usuario.map(u -> {
                    u.setContrasena(null); // No exponer contraseña
                    return new ResponseEntity<>(u, HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    }
