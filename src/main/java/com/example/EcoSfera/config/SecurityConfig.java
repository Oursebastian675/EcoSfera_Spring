package com.example.EcoSfera.config;

import com.fasterxml.jackson.databind.ObjectMapper; // Para convertir el Map a JSON String
import jakarta.servlet.http.HttpServletResponse; // Necesario para los handlers
import org.springframework.beans.factory.annotation.Autowired; // Para inyectar el filtro JWT
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Descomentar si usas @PreAuthorize, etc. en los métodos
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Para añadir el filtro JWT antes
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime; // Para el timestamp en las respuestas de error
import java.util.HashMap; // Para construir el cuerpo del JSON de error
import java.util.Map; // Para construir el cuerpo del JSON de error

@Configuration
@EnableWebSecurity
// @EnableMethodSecurity // Descomentar si usas anotaciones como @PreAuthorize en tus controladores
public class SecurityConfig {

    // 1. Declara el campo para tu filtro
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 2. Inyéctalo a través del constructor
    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll() // Registro
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/login").permitAll() // Login
                        .requestMatchers(HttpMethod.GET, "/api/productos/**").permitAll() // Ver productos

                        // Endpoints que requieren autenticación
                        .requestMatchers(HttpMethod.POST, "/api/ventas").authenticated() // Endpoint para crear una venta/compra
                        .requestMatchers(HttpMethod.POST, "/api/factura/generar-y-descargar").authenticated() // Generar y descargar factura
                        .requestMatchers(HttpMethod.POST, "/api/enviar-factura").authenticated() // Enviar factura (si aún lo usas)

                        // Endpoints que antes eran de administración, ahora solo requieren autenticación
                        // .requestMatchers("/api/proveedores/**").authenticated() // Si tuvieras este endpoint y quisieras protegerlo
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").authenticated() // Ver todos los usuarios (ahora solo autenticado)
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/{id}").authenticated() // Eliminar usuario por ID (ahora solo autenticado)
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/{id}").authenticated() // Actualizar usuario (ya estaba como autenticado)

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                            Map<String, Object> body = new HashMap<>();
                            body.put("timestamp", LocalDateTime.now().toString());
                            body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
                            body.put("error", "Unauthorized");
                            body.put("message", "Acceso no autorizado. Se requiere autenticación.");
                            body.put("detail", authException.getMessage());
                            body.put("path", request.getRequestURI());

                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(body));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                            Map<String, Object> body = new HashMap<>();
                            body.put("timestamp", LocalDateTime.now().toString());
                            body.put("status", HttpServletResponse.SC_FORBIDDEN);
                            body.put("error", "Forbidden");
                            body.put("message", "Acceso denegado. No tiene los permisos necesarios."); // Este mensaje sigue siendo relevante si alguna vez reintroduces roles o permisos más granulares
                            body.put("detail", accessDeniedException.getMessage());
                            body.put("path", request.getRequestURI());

                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(body));
                        })
                )
                // Añadimos el filtro JWT antes del filtro de autenticación por usuario/contraseña
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:5173"); // O el origen de tu frontend
        configuration.addAllowedMethod("*"); // GET, POST, PUT, DELETE, OPTIONS, etc.
        configuration.addAllowedHeader("*"); // Content-Type, Authorization, etc.
        // configuration.setAllowCredentials(true); // Descomentar si necesitas enviar cookies/sesiones con CORS

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica esta configuración a TODAS las rutas
        return source;
    }
}