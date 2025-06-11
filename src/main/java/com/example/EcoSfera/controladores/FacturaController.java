package com.example.EcoSfera.controladores;

import com.example.EcoSfera.servicios.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Para proteger el endpoint
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api") // Ruta base para este controlador
public class FacturaController {

    private static final Logger log = LoggerFactory.getLogger(FacturaController.class);

    @Autowired
    private EmailService emailService;

    @PostMapping("/enviar-factura")
    @PreAuthorize("isAuthenticated()") // Ejemplo: Solo usuarios autenticados pueden llamar a esto
    // Podrías ser más específico con roles: @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> enviarFactura(
            @RequestParam("pdfFile") MultipartFile pdfFile,
            @RequestParam("emailUsuario") String emailUsuario,
            @RequestParam(value = "nombreArchivo", defaultValue = "factura.pdf") String nombreArchivo, // Nombre opcional para el adjunto
            @RequestParam(value = "asunto", defaultValue = "Tu Factura de EcoSfera") String asunto,
            @RequestParam(value = "cuerpoMensaje", defaultValue = "<p>Hola,</p><p>Adjuntamos tu factura correspondiente a tu reciente compra en EcoSfera.</p><p>¡Gracias por tu preferencia!</p><p>Saludos,<br/>El equipo de EcoSfera</p>") String cuerpoMensaje
    ) {

        if (pdfFile.isEmpty()) {
            log.warn("Intento de enviar factura sin archivo PDF para el usuario {}", emailUsuario);
            return ResponseEntity.badRequest().body(Map.of("message", "El archivo PDF es obligatorio."));
        }

        if (emailUsuario == null || emailUsuario.trim().isEmpty() || !emailUsuario.contains("@")) {
            log.warn("Intento de enviar factura con dirección de correo inválida: {}", emailUsuario);
            return ResponseEntity.badRequest().body(Map.of("message", "La dirección de correo del usuario es inválida."));
        }

        try {
            log.info("Solicitud para enviar factura a {} con archivo {}", emailUsuario, pdfFile.getOriginalFilename());
            emailService.enviarFacturaPorCorreo(emailUsuario, asunto, cuerpoMensaje, pdfFile, nombreArchivo);
            return ResponseEntity.ok(Map.of("message", "Factura enviada correctamente a " + emailUsuario));
        } catch (RuntimeException e) { // Captura las excepciones lanzadas por EmailService
            log.error("Error al procesar la solicitud de envío de factura para {}: {}", emailUsuario, e.getMessage());
            // Devuelve el mensaje de la excepción interna para más detalle, si es seguro hacerlo.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error al enviar la factura: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al enviar factura para {}: {}", emailUsuario, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ocurrió un error inesperado al enviar la factura."));
        }
    }
}