package com.example.EcoSfera.controladores;

import com.example.EcoSfera.config.UsuarioUpdateDTO;
import com.example.EcoSfera.modelos.Usuario;
import com.example.EcoSfera.servicios.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.EcoSfera.config.LoginRequestDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
            nuevoUsuario.setContrasena(null); // Do not return password
            return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) { // Catch specific exception from service
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
        catch (RuntimeException e) { // Catch other runtime exceptions from service (e.g., email/username exists)
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody LoginRequestDTO loginRequest) {
        Optional<Usuario> usuarioAutenticado = usuarioService.autenticarUsuario(
                loginRequest.getCredencial(),
                loginRequest.getContrasena()
        );

        if (usuarioAutenticado.isPresent()) {
            Usuario user = usuarioAutenticado.get();
            user.setContrasena(null); // Do not return password
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenciales inválidas"));
        }
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        usuarios.forEach(u -> u.setContrasena(null)); // Do not return passwords
        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
        return usuario.map(u -> {
                    u.setContrasena(null); // Do not return password
                    return new ResponseEntity<>(u, HttpStatus.OK);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioUpdateDTO usuarioUpdateDTO) {
        try {
            Optional<Usuario> usuarioActualizadoOptional = usuarioService.actualizarUsuario(id, usuarioUpdateDTO);

            return usuarioActualizadoOptional.map(usuario -> {
                usuario.setContrasena(null); // Never return the password in the response
                return ResponseEntity.ok(usuario);
            }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body((Usuario) Map.of("message", "Usuario no encontrado con ID: " + id))); // Changed to Map for consistency

        } catch (RuntimeException e) { // Catches validation exceptions from the service (duplicate email/username, etc.)
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            boolean eliminado = usuarioService.deleteUsuario(id);
            if (eliminado) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // HTTP 204: Success, no content to return
            } else {

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Usuario no encontrado con ID: " + id));
            }
        } catch (DataIntegrityViolationException e) {
            logger.error("Error de integridad al intentar eliminar usuario con ID {}: {}", id, e.getMessage());
            // This typically means the user is referenced by other entities (e.g., Venta)
            // and cannot be deleted due to foreign key constraints.
            return ResponseEntity.status(HttpStatus.CONFLICT) // HTTP 409: Conflict
                    .body(Map.of("message", "No se puede eliminar el usuario. Puede tener datos asociados (ej. compras) que impiden su eliminación. ID: " + id));
        } catch (Exception e) {
            // Catch any other unexpected errors
            logger.error("Error inesperado al eliminar usuario con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ocurrió un error interno al intentar eliminar el usuario."));
        }
    }
}