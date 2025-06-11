package com.example.EcoSfera.controladores;

import com.example.EcoSfera.config.LoginRequestDTO;
import com.example.EcoSfera.config.UsuarioUpdateDTO;
import com.example.EcoSfera.config.JwtTokenProvider; // Importar JwtTokenProvider
import com.example.EcoSfera.modelos.Usuario;
import com.example.EcoSfera.servicios.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.authentication.AuthenticationManager; // No lo estamos usando directamente aquí para generar token
import org.springframework.web.bind.annotation.*;
import java.util.HashMap; // Para la respuesta del login
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

    @Autowired
    private UsuarioService usuarioService;

    // @Autowired
    // private AuthenticationManager authenticationManager; // No es estrictamente necesario si UsuarioService maneja la lógica de contraseña

    @Autowired
    private JwtTokenProvider tokenProvider; // Inyectar para generar el token

    @PostMapping
    public ResponseEntity<?> registrarUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
            // Considera devolver un DTO en lugar de la entidad completa, y no la contraseña
            nuevoUsuario.setContrasena(null);
            return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
        } catch (RuntimeException e) { // Simplificado: RuntimeException cubre IllegalArgumentException
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
            // Generar el token JWT usando el email o nombre de usuario como subject
            String token = tokenProvider.generateTokenFromUsername(user.getEmail()); // O user.getUsuario()

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("nombre", user.getNombre());
            // Añade otros datos del usuario que el frontend pueda necesitar, pero NUNCA la contraseña.

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Credenciales inválidas"));
        }
    }

    // ... (resto de los métodos sin cambios relevantes para esta tarea)
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

            // Añadimos una pista de tipo <ResponseEntity<?>> a la operación map
            return usuarioActualizadoOptional.<ResponseEntity<?>>map(usuario -> {
                usuario.setContrasena(null); // Never return the password in the response
                return ResponseEntity.ok(usuario);
            }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Usuario no encontrado con ID: " + id)));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            boolean eliminado = usuarioService.deleteUsuario(id);
            if (eliminado) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Usuario no encontrado con ID: " + id));
            }
        } catch (DataIntegrityViolationException e) {
            logger.error("Error de integridad al intentar eliminar usuario con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "No se puede eliminar el usuario. Puede tener datos asociados (ej. compras) que impiden su eliminación. ID: " + id));
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar usuario con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ocurrió un error interno al intentar eliminar el usuario."));
        }
    }
}