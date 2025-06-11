package com.example.EcoSfera.controladores;

import com.example.EcoSfera.config.NuevaVentaRequestDTO;
import com.example.EcoSfera.modelos.Venta;
import com.example.EcoSfera.servicios.FacturaGenerationService; // Importar el servicio de facturas
import com.example.EcoSfera.servicios.VentaService;
// import com.example.EcoSfera.servicios.EmailService; // Si quieres enviar email también
// import org.springframework.core.io.ByteArrayResource; // Para EmailService
// import org.springframework.mock.web.MockMultipartFile; // Para EmailService
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private static final Logger log = LoggerFactory.getLogger(VentaController.class);

    @Autowired
    private VentaService ventaService;

    @Autowired
    private FacturaGenerationService facturaGenerationService; // Inyectar servicio de facturas

    // @Autowired
    // private EmailService emailService; // Descomentar si vas a enviar email

    @PostMapping
    public ResponseEntity<?> crearVenta(@RequestBody NuevaVentaRequestDTO ventaRequestDTO, Authentication authentication) {
        try {
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
                log.warn("Intento de crear venta sin autenticación válida.");
                return new ResponseEntity<>(Map.of("message", "Se requiere autenticación para crear una venta."), HttpStatus.UNAUTHORIZED);
            }
            // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            // String username = userDetails.getUsername(); // Este es el email o username del token
            // Aquí podrías validar que el ventaRequestDTO.getUserId() (si lo sigues usando)
            // coincida con el usuario autenticado, o mejor aún, ignorar el userId del DTO
            // y usar siempre el del usuario autenticado. Por ahora, asumimos que VentaService lo maneja
            // o que el frontend envía el userId correcto (aunque obtenerlo del token es más seguro).

            log.info("Procesando venta para el usuario autenticado: {}", authentication.getName());
            if (ventaRequestDTO.getUserId() == null) {
                // Si decides que el userId debe venir del token, y no del DTO, aquí lo establecerías.
                // Por ahora, si VentaService lo requiere, el DTO debe traerlo o VentaService debe obtenerlo.
                log.warn("userId es nulo en la solicitud de venta. Asegúrate de que VentaService pueda obtenerlo si es necesario.");
            }

            Venta nuevaVenta = ventaService.crearVenta(ventaRequestDTO);

            // --- INICIO INTEGRACIÓN FACTURACIÓN ---
            try {
                String emailUsuarioParaFactura = nuevaVenta.getUsuario().getEmail();
                String nombreArchivoFactura = "factura_ecosfera_" + nuevaVenta.getId() + ".pdf";

                // Generar el PDF de la factura
                byte[] pdfBytes = facturaGenerationService.generarFacturaPdf(nuevaVenta, ventaRequestDTO, nombreArchivoFactura);
                log.info("Factura generada en memoria ({} bytes) para la venta ID: {}", pdfBytes.length, nuevaVenta.getId());

                // Opcional: Enviar la factura por correo
                /*
                if (pdfBytes != null && pdfBytes.length > 0 && emailUsuarioParaFactura != null) {
                    MockMultipartFile multipartPdfFile = new MockMultipartFile(
                        nombreArchivoFactura, // nombre original del archivo
                        nombreArchivoFactura, // nombre del archivo
                        "application/pdf",    // content type
                        pdfBytes
                    );
                    String asuntoCorreo = "Tu factura de EcoSfera - Compra #" + nuevaVenta.getId();
                    String cuerpoCorreo = "<p>Hola " + nuevaVenta.getNombreCliente() + ",</p>" +
                                          "<p>Gracias por tu compra en EcoSfera. Adjuntamos la factura correspondiente (Nº " + nuevaVenta.getId() + ").</p>" +
                                          "<p>Saludos,<br/>El equipo de EcoSfera</p>";
                    emailService.enviarFacturaPorCorreo(emailUsuarioParaFactura, asuntoCorreo, cuerpoCorreo, multipartPdfFile, nombreArchivoFactura);
                    log.info("Factura para venta ID {} enviada por correo a {}", nuevaVenta.getId(), emailUsuarioParaFactura);
                }
                */

            } catch (Exception e) {
                log.error("Error durante la generación o envío de la factura para la venta ID {}: {}", nuevaVenta.getId(), e.getMessage(), e);
                // Importante: La venta ya se creó. Este error es solo de la facturación/email.
                // No se devuelve un error al cliente por esto, pero se registra.
                // Podrías añadir la factura a una cola de reintentos si el envío falla.
            }
            // --- FIN INTEGRACIÓN FACTURACIÓN ---

            return new ResponseEntity<>(nuevaVenta, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Intento de crear venta con datos inválidos: {}", e.getMessage());
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error inesperado al crear venta: ", e);
            return new ResponseEntity<>(Map.of("message", "Ocurrió un error procesando la venta."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ... (resto de los métodos GET, DELETE sin cambios)
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