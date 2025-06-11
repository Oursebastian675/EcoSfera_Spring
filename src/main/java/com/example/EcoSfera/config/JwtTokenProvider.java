package com.example.EcoSfera.config; // Asegúrate de que el paquete sea el correcto
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // Específico para errores de firma
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component // Marca esta clase como un componente de Spring
public class JwtTokenProvider {

    private static final Logger tokenLogger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}") // Se inyecta desde application.properties
    private String jwtSecretString;

    @Value("${app.jwt.expiration-ms}") // Se inyecta desde application.properties
    private int jwtExpirationInMs;

    // Método para obtener la clave de firma.
    // Es buena práctica tenerla como un método privado o inicializarla una vez.
    private SecretKey getSigningKey() {
        // Convierte la cadena secreta (que debe ser suficientemente larga y segura)
        // en una SecretKey para el algoritmo HS512.
        // Asegúrate de que tu jwtSecretString sea una cadena codificada en Base64 si es compleja,
        // o una cadena simple lo suficientemente larga. Keys.hmacShaKeyFor espera bytes.
        return Keys.hmacShaKeyFor(jwtSecretString.getBytes());
    }

    /**
     * Genera un token JWT para un usuario autenticado.
     *
     * @param authentication El objeto Authentication que contiene los detalles del principal.
     * @return El token JWT generado.
     */
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateTokenFromUsername(userPrincipal.getUsername());
    }

    /**
     * Genera un token JWT directamente desde un nombre de usuario (o email).
     *
     * @param username El nombre de usuario (o email) para el cual generar el token.
     * @return El token JWT generado.
     */
    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        tokenLogger.debug("Generando token para {} con expiración en {}", username, expiryDate);

        return Jwts.builder()
                .setSubject(username) // El "subject" del token, usualmente el username o email
                .setIssuedAt(now) // Fecha de emisión
                .setExpiration(expiryDate) // Fecha de expiración
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // Firma el token con la clave y algoritmo
                .compact(); // Construye el token y lo serializa a una cadena compacta
    }

    /**
     * Extrae el nombre de usuario (o email) del token JWT.
     *
     * @param token El token JWT.
     * @return El nombre de usuario (o email) contenido en el token.
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
        
                .setSigningKey(getSigningKey()) // Establece la clave para verificar la firma
                .build()
                .parseClaimsJws(token) // Parsea y valida el token
                .getBody(); // Obtiene el cuerpo (payload) del token

        return claims.getSubject(); // Retorna el "subject"
    }

    /**
     * Valida un token JWT.
     * Verifica la firma, la expiración y si está bien formado.
     *
     * @param authToken El token JWT a validar.
     * @return true si el token es válido, false en caso contrario.
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) // Clave para verificar la firma
                    .build()
                    .parseClaimsJws(authToken); // Intenta parsear y validar. Si falla, lanza una excepción.
            tokenLogger.trace("Token JWT validado exitosamente: {}", authToken);
            return true;
            // En JwtTokenProvider.java
        } catch (SignatureException ex) {
            tokenLogger.error("Firma JWT inválida: {}", ex.getMessage()); // Revisa este log
        } catch (MalformedJwtException ex) {
            tokenLogger.error("Token JWT malformado: {}", ex.getMessage()); // Revisa este log
        } catch (ExpiredJwtException ex) {
            tokenLogger.error("Token JWT expirado: {}", ex.getMessage()); // Revisa este log
            // ...

        } catch (UnsupportedJwtException ex) {
            tokenLogger.error("Token JWT no soportado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // Esta excepción puede ocurrir si el token es null o vacío,
            // o si la clave de firma es inválida (aunque getSigningKey debería manejar esto).
            tokenLogger.error("Argumento JWT inválido o cadena de claims vacía: {}", ex.getMessage());
        }
        return false;
    }
}