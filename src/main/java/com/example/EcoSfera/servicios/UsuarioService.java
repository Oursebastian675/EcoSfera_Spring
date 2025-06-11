package com.example.EcoSfera.servicios;

import com.example.EcoSfera.config.UsuarioUpdateDTO;
import com.example.EcoSfera.modelos.Usuario;
import com.example.EcoSfera.repositorios.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy; // Importa la anotación @Lazy
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Usuario> buscarPorEmailOCredencial(String credencial) {
        // Intenta buscar por email primero
        Optional<Usuario> usuarioPorEmail = usuarioRepository.findByEmail(credencial);
        if (usuarioPorEmail.isPresent()) {
            return usuarioPorEmail;
        }
        // Si no se encuentra por email, intenta buscar por nombre de usuario
        return usuarioRepository.findByUsuario(credencial);
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Usuario usuario = buscarPorEmailOCredencial(usernameOrEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado con email o nombre de usuario: " + usernameOrEmail));

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(), // O el identificador principal que uses (e.g., usuario.getUsuario())
                usuario.getContrasena(), // La contraseña ya debe estar codificada en la BD
                new ArrayList<>() // Aquí deberías mapear tus roles/autoridades si los tienes
        );
    }

    // Este método ya estaba en tu código y usa passwordEncoder correctamente
    public void registrarUsuario(String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        // ...lógica para guardar el usuario con la contraseña codificada
        // Por ejemplo:
        // Usuario nuevoUsuario = new Usuario();
        // nuevoUsuario.setContrasena(encodedPassword);
        // ...otros campos...
        // usuarioRepository.save(nuevoUsuario);
        logger.info("Contraseña codificada para registro: {}", encodedPassword); // Ejemplo de log
    }

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    // Este método ya estaba y es correcto, no necesita cambios aquí
    // public Usuario saveUsuario(Usuario usuario) {
    // return usuarioRepository.save(usuario);
    // }


    @Transactional
    public boolean deleteUsuario(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            logger.info("Usuario con ID {} eliminado exitosamente.", id);
            return true;
        }
        logger.warn("Intento de eliminar usuario con ID {} fallido. Usuario no encontrado.", id);
        return false;
    }

    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El email '" + usuario.getEmail() + "' ya está registrado.");
        }

        if (usuario.getUsuario() != null && !usuario.getUsuario().isEmpty()) {
            if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent()) {
                throw new RuntimeException("El nombre de usuario '" + usuario.getUsuario() + "' ya está en uso.");
            }
        }

        if (usuario.getContrasena() == null || usuario.getContrasena().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        // La codificación de la contraseña se hace aquí, usando el passwordEncoder inyectado
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        return usuarioRepository.save(usuario);
    }


    public Optional<Usuario> autenticarUsuario(String credencial, String contrasenaPlana) {
        logger.info("--- INICIO AUTENTICACIÓN ---");
        logger.info("Credencial recibida: [{}]", credencial);

        Optional<Usuario> usuarioOptional = buscarPorEmailOCredencial(credencial); // Usamos el método refactorizado

        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            logger.info("Usuario encontrado en BD. ID: {}, Email: {}, Username: {}", usuario.getId(), usuario.getEmail(), usuario.getUsuario());

            // La comparación de contraseñas se hace aquí, usando el passwordEncoder inyectado
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

    @Transactional
    public Optional<Usuario> actualizarUsuario(Long id, UsuarioUpdateDTO datosActualizacion) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(id);
        if (!usuarioOptional.isPresent()) {
            return Optional.empty();
        }

        Usuario usuarioExistente = usuarioOptional.get();

        // Actualizar nombre
        if (datosActualizacion.getNombre() != null) {
            usuarioExistente.setNombre(datosActualizacion.getNombre());
        }
        // Actualizar apellido
        if (datosActualizacion.getApellido() != null) {
            usuarioExistente.setApellido(datosActualizacion.getApellido());
        }
        // Actualizar email (con validación de unicidad si cambia)
        if (datosActualizacion.getEmail() != null && !datosActualizacion.getEmail().equalsIgnoreCase(usuarioExistente.getEmail())) {
            if (usuarioRepository.findByEmail(datosActualizacion.getEmail()).isPresent()) {
                throw new RuntimeException("El email '" + datosActualizacion.getEmail() + "' ya está registrado por otro usuario.");
            }
            usuarioExistente.setEmail(datosActualizacion.getEmail());
        }
        // Actualizar telefono
        if (datosActualizacion.getTelefono() != null) {
            usuarioExistente.setTelefono(datosActualizacion.getTelefono());
        }
        // Actualizar edad
        if (datosActualizacion.getEdad() != null) {
            usuarioExistente.setEdad(datosActualizacion.getEdad());
        }
        // Actualizar nombre de usuario (con validación de unicidad si cambia)
        if (datosActualizacion.getUsuario() != null && !datosActualizacion.getUsuario().isEmpty() &&
                (usuarioExistente.getUsuario() == null || !datosActualizacion.getUsuario().equalsIgnoreCase(usuarioExistente.getUsuario()))) {
            if (usuarioRepository.findByUsuario(datosActualizacion.getUsuario()).isPresent()) {
                throw new RuntimeException("El nombre de usuario '" + datosActualizacion.getUsuario() + "' ya está en uso por otro usuario.");
            }
            usuarioExistente.setUsuario(datosActualizacion.getUsuario());
        }
        // Actualizar contraseña si se proporciona una nueva
        if (datosActualizacion.getContrasena() != null && !datosActualizacion.getContrasena().isEmpty()) {
            // La codificación de la nueva contraseña se hace aquí
            usuarioExistente.setContrasena(passwordEncoder.encode(datosActualizacion.getContrasena()));
            logger.info("Contraseña actualizada para el usuario ID: {}", id);
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        return Optional.of(usuarioActualizado);
    }
}