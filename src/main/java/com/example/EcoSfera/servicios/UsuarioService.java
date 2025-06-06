package com.example.EcoSfera.servicios;

import com.example.EcoSfera.config.UsuarioUpdateDTO; // Importa el nuevo DTO
import com.example.EcoSfera.modelos.Usuario;
import com.example.EcoSfera.repositorios.UsuarioRepository;
// import com.example.EcoSfera.repositorios.VentaRepository; // Import if you add logic to check for sales
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.dao.DataIntegrityViolationException; // Import if you throw this from service
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Assuming VentaRepository would be needed if you check for sales before deletion
    // @Autowired
    // private VentaRepository ventaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> getUsuarioById(Long id) {
        return usuarioRepository.findById(id);
    }

    // This method saveUsuario might be redundant if crearUsuario and actualizarUsuario cover all cases.
    // Consider reviewing its necessity.
    public Usuario saveUsuario(Usuario usuario) {
        // For example, if this is only for internal use and doesn't need password encoding or validation,
        // it might be okay. Otherwise, it could bypass logic in crearUsuario.
        return usuarioRepository.save(usuario);
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param id El ID del usuario a eliminar.
     * @return true si el usuario fue encontrado y eliminado, false si el usuario no fue encontrado.
     * @throws org.springframework.dao.DataIntegrityViolationException si el usuario no puede ser eliminado debido a restricciones de integridad (ej. ventas asociadas).
     */
    @Transactional
    public boolean deleteUsuario(Long id) {
        if (usuarioRepository.existsById(id)) {
            // Optional: Add business logic here if a user cannot be deleted under certain conditions
            // e.g., check for associated sales if they shouldn't be orphaned or if cascading delete is not set up.
            // List<Venta> compras = ventaRepository.findByUsuarioId(id); // Assuming such a method exists
            // if (!compras.isEmpty()) {
            //     throw new DataIntegrityViolationException("El usuario con ID " + id + " tiene compras asociadas y no puede ser eliminado.");
            // }
            usuarioRepository.deleteById(id);
            logger.info("Usuario con ID {} eliminado exitosamente.", id);
            return true;
        }
        logger.warn("Intento de eliminar usuario con ID {} fallido. Usuario no encontrado.", id);
        return false;
    }

    @Transactional // Asegura que la operación sea atómica
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
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> autenticarUsuario(String credencial, String contrasenaPlana) {
        logger.info("--- INICIO AUTENTICACIÓN ---");
        logger.info("Credencial recibida: [{}]", credencial);

        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(credencial);

        if (!usuarioOptional.isPresent()) {
            logger.info("Usuario no encontrado por email [{}], intentando buscar por nombre de usuario.", credencial);
            usuarioOptional = usuarioRepository.findByUsuario(credencial);
        }

        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            logger.info("Usuario encontrado en BD. ID: {}, Email: {}, Username: {}", usuario.getId(), usuario.getEmail(), usuario.getUsuario());

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

    @Transactional // Asegura que la operación sea atómica
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
            usuarioExistente.setContrasena(passwordEncoder.encode(datosActualizacion.getContrasena()));
            logger.info("Contraseña actualizada para el usuario ID: {}", id);
        }

        Usuario usuarioActualizado = usuarioRepository.save(usuarioExistente);
        return Optional.of(usuarioActualizado);
    }
}