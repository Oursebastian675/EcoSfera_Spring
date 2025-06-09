package com.example.EcoSfera.controladores;

import com.example.EcoSfera.config.NuevaVentaRequestDTO; // Importar el DTO correcto
import com.example.EcoSfera.modelos.Venta;
import com.example.EcoSfera.servicios.VentaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Importar para obtener usuario autenticado
import org.springframework.security.core.userdetails.UserDetails; // Para obtener detalles del usuario
// import com.example.EcoSfera.servicios.UsuarioService; // Si necesitas buscar usuario por email/username
import org.springframework.web.bind.annotation.*;
// import jakarta.validation.Valid; // Si decides usar validaciones en el DTO

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private static final Logger log = LoggerFactory.getLogger(VentaController.class);

    @Autowired
    private VentaService ventaService;

    // @Autowired // Descomentar si lo usas para obtener el ID del usuario
    // private UsuarioService usuarioService;


    @PostMapping
    // Considera añadir @Valid si usas anotaciones de validación en NuevaVentaRequestDTO
    public ResponseEntity<?> crearVenta(@RequestBody NuevaVentaRequestDTO ventaRequestDTO, Authentication authentication) {
        try {
            // **Recomendación de Seguridad y Buenas Prácticas:**
            // Es mejor obtener el ID del usuario desde el objeto `Authentication`
            // en lugar de confiar en el `userId` enviado en el cuerpo de la solicitud.
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                // Aquí necesitarías una forma de obtener el ID del Usuario a partir de su username (email)
                // Por ejemplo, si tu UserDetails implementa una interfaz con getId() o si buscas en UsuarioService
                // String username = userDetails.getUsername();
                // Usuario usuarioAutenticado = usuarioService.findByUsernameOrEmail(username); // Necesitarías este método
                // if (usuarioAutenticado != null) {
                //     ventaRequestDTO.setUserId(usuarioAutenticado.getId());
                // } else {
                //     log.warn("Usuario autenticado no encontrado en la base de datos: {}", username);
                //     return new ResponseEntity<>(Map.of("message", "Error de autenticación: Usuario no encontrado."), HttpStatus.UNAUTHORIZED);
                // }
                log.info("Procesando venta para el usuario autenticado: {}", authentication.getName());
                // Si decides seguir usando el userId del DTO por ahora, asegúrate que el frontend lo envíe correctamente.
                // Si el userId del DTO es null y la autenticación está presente, podrías lanzar un error o intentar obtenerlo.
                if (ventaRequestDTO.getUserId() == null) {
                    // Aquí decides cómo manejarlo: o bien es un error si esperas que el DTO lo traiga,
                    // o intentas obtenerlo del 'authentication' como se comentó arriba.
                    // Por ahora, si se usa el userId del DTO, este debe venir.
                    log.warn("userId es nulo en la solicitud de venta y no se está obteniendo de la autenticación.");
                }

            } else if (ventaRequestDTO.getUserId() == null) {
                // Si no hay autenticación y el userId no viene en el DTO, es un error.
                log.warn("Intento de crear venta sin userId y sin autenticación.");
                return new ResponseEntity<>(Map.of("message", "Se requiere userId o estar autenticado."), HttpStatus.BAD_REQUEST);
            }


            Venta nuevaVenta = ventaService.crearVenta(ventaRequestDTO);
            return new ResponseEntity<>(nuevaVenta, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Intento de crear venta con datos inválidos: {}", e.getMessage());
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error inesperado al crear venta: ", e); // Incluir la traza de la excepción en el log
            return new ResponseEntity<>(Map.of("message", "Ocurrió un error procesando la venta."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ... resto de los métodos del controlador (obtenerTodasLasVentas, obtenerVentaPorId, eliminarVenta) ...
    // Estos no necesitan cambios directos por la refactorización del DTO de creación.
    @GetMapping
    public ResponseEntity<List<Venta>> obtenerTodasLasVentas() {
        List<Venta> ventas = ventaService.obtenerTodasLasVentas();
        if (ventas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(ventas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerVentaPorId(@PathVariable Long id) {
        Venta venta = ventaService.obtenerVentaPorId(id);
        if (venta == null) {
            log.warn("Intento de obtener venta con ID no encontrado: {}", id);
            return new ResponseEntity<>(Map.of("message", "Venta no encontrada con ID: " + id), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(venta, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVenta(@PathVariable Long id) {
        try {
            boolean eliminada = ventaService.eliminarVenta(id);
            if (eliminada) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                log.warn("Intento de eliminar venta con ID no encontrado: {}", id);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error inesperado al eliminar venta con ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}