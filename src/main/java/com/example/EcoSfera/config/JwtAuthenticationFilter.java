package com.example.EcoSfera.config; // Asegúrate de que el paquete sea el correcto

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Importa tu UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Marca esta clase como un componente de Spring para que pueda ser inyectada
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger filterLogger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService; // Este será tu UsuarioService

    @Autowired
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            filterLogger.debug("Procesando solicitud para la URI: {}", request.getRequestURI());

            if (StringUtils.hasText(jwt)) {
                filterLogger.debug("Token JWT extraído de la solicitud: '{}'", jwt);
                if (tokenProvider.validateToken(jwt)) {
                    filterLogger.debug("Token JWT validado exitosamente.");
                    String usernameOrEmail = tokenProvider.getUsernameFromJWT(jwt);
                    filterLogger.debug("Username/Email extraído del JWT: '{}'", usernameOrEmail);

                    // Carga los detalles del usuario desde tu UserDetailsService (UsuarioService)
                    UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOrEmail);
                    filterLogger.debug("UserDetails cargados para el usuario: '{}'", userDetails.getUsername());

                    // Crea el objeto de autenticación
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Las credenciales (contraseña) no son necesarias aquí ya que el token ya está validado
                            userDetails.getAuthorities()
                    );

                    // Establece detalles adicionales de la autenticación (como la IP y el agente de usuario)
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Establece la autenticación en el SecurityContextHolder
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    filterLogger.info("Usuario '{}' autenticado exitosamente y SecurityContext actualizado para la URI: {}",
                            usernameOrEmail, request.getRequestURI());
                } else {
                    filterLogger.warn("Validación del token JWT falló para el token: '{}'", jwt);
                    // Opcional: podrías querer limpiar el SecurityContext si el token es inválido
                    // SecurityContextHolder.clearContext();
                }
            } else {
                filterLogger.trace("No se encontró token JWT en la cabecera de autorización para la URI: {}", request.getRequestURI());
            }
        } catch (Exception ex) {
            filterLogger.error("No se pudo establecer la autenticación del usuario en el contexto de seguridad", ex);
            // Opcional: limpiar el SecurityContext en caso de error inesperado
            // SecurityContextHolder.clearContext();
        }

        // Continúa con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT de la cabecera "Authorization" de la solicitud.
     * El token debe estar prefijado con "Bearer ".
     *
     * @param request La solicitud HTTP.
     * @return El token JWT como String, o null si no se encuentra o no tiene el formato correcto.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remueve el prefijo "Bearer "
        }
        return null;
    }
}