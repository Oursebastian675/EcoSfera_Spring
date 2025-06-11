package com.example.EcoSfera.servicios;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}") // Para usar el remitente configurado
    private String fromEmailAddress;

    /**
     * Envía un correo electrónico con un archivo adjunto (factura PDF).
     *
     * @param toEmail La dirección de correo del destinatario.
     * @param subject El asunto del correo.
     * @param body El cuerpo del correo (puede ser HTML).
     * @param attachment El archivo MultipartFile que se adjuntará.
     * @param attachmentName El nombre que tendrá el archivo adjunto en el correo.
     */
    public void enviarFacturaPorCorreo(String toEmail, String subject, String body, MultipartFile attachment, String attachmentName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // El 'true' en MimeMessageHelper indica que es un mensaje multipart (necesario para adjuntos y HTML)
            // El "UTF-8" asegura la correcta codificación de caracteres.
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmailAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // El 'true' indica que el cuerpo es HTML

            if (attachment != null && !attachment.isEmpty()) {
                // Usamos ByteArrayResource para evitar problemas con archivos temporales
                helper.addAttachment(attachmentName, new ByteArrayResource(attachment.getBytes()));
                log.info("Archivo adjunto '{}' añadido al correo para {}.", attachmentName, toEmail);
            }

            mailSender.send(mimeMessage);
            log.info("Correo con factura enviado exitosamente a {}", toEmail);

        } catch (MessagingException e) {
            log.error("Error de mensajería al enviar correo a {}: {}", toEmail, e.getMessage());
            // Considera lanzar una excepción personalizada aquí para que el controlador la maneje
            throw new RuntimeException("Error al preparar el correo con factura: " + e.getMessage(), e);
        } catch (MailException e) {
            log.error("Error de MailException al enviar correo a {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error al enviar el correo con factura: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error de IOException al procesar el adjunto para {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error al leer el archivo adjunto: " + e.getMessage(), e);
        }
    }
}