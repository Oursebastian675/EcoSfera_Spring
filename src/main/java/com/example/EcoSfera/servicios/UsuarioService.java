package com.example.EcoSfera.servicios;

import com.example.EcoSfera.modelos.Usuario;
import com.example.EcoSfera.repositorios.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    // Añade un logger para un mejor seguimiento en producción
    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }


    public Usuario saveUsuario(Usuario usuario) {
        // Si se actualiza la contraseña, también debería hashearse aquí.
        // Considera un método específico para cambio de contraseña.
        if (usuario.getContrasena() != null && !usuario.getContrasena().isEmpty()) {
            // Asumimos que si la contraseña no es nula/vacía y no está ya hasheada (difícil de saber sin un flag),
            // se quiere actualizar y hashear. Esto es simplista; un DTO para actualización sería mejor.
            // Para el flujo de `crearUsuario`, la contraseña ya vendrá para ser hasheada.
        }
        return usuarioRepository.save(usuario);
    }

    public void deleteUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    public Usuario crearUsuario(Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El email '" + usuario.getEmail() + "' ya está registrado.");
        }

        // Validación de nombre de usuario existente (si 'usuario' debe ser único)
        if (usuario.getUsuario() != null && !usuario.getUsuario().isEmpty()) {
            if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent()) {
                throw new RuntimeException("El nombre de usuario '" + usuario.getUsuario() + "' ya está en uso.");
            }
        }

        if (usuario.getContrasena() == null || usuario.getContrasena().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> autenticarUsuario(String credencial, String contrasenaPlana) {
        logger.info("--- INICIO AUTENTICACIÓN ---");
        logger.info("Credencial recibida: [{}]", credencial);
        // No loguear contraseñas planas en producción, o hacerlo con mucho cuidado y niveles de log adecuados.
        // logger.debug("Contraseña plana recibida: [{}]", contrasenaPlana);


        // Intenta buscar por email primero
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(credencial);

        // Si no se encontró por email, intenta buscar por nombre de usuario
        if (!usuarioOptional.isPresent()) {
            logger.info("Usuario no encontrado por email [{}], intentando buscar por nombre de usuario.", credencial);
            usuarioOptional = usuarioRepository.findByUsuario(credencial);
        }

        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            logger.info("Usuario encontrado en BD. ID: {}, Email: {}, Username: {}", usuario.getId(), usuario.getEmail(), usuario.getUsuario());
            // logger.debug("Hash almacenado en BD para {}: [{}]", credencial, usuario.getContrasena());


            if (passwordEncoder.matches(contrasenaPlana, usuario.getContrasena())) {
                logger.info("¡Las contraseñas coinciden para la credencial [{}]!", credencial);
                logger.info("--- FIN AUTENTICACIÓN (ÉXITO) ---");
                return Optional.of(usuario);
            } else {
                logger.warn("Las contraseñas NO coinciden para la credencial [{}].", credencial);
            }
        } else {
            logger.warn("No se encontró ningún usuario con la credencial: [{}]", credencial);
        }

        logger.info("--- FIN AUTENTICACIÓN (FALLO) ---");
        return Optional.empty();
    }
}