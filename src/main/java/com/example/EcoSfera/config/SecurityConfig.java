package com.example.EcoSfera.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
@Configuration
@EnableWebSecurity // Habilita la configuración de seguridad web de Spring
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF. Para APIs stateless es común deshabilitarlo.
                .csrf(csrf -> csrf.disable())

                // Configurar CORS a través del DSL de HttpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configurar las reglas de autorización para las peticiones HTTP
                .authorizeHttpRequests(authz -> authz
                        // Permitir acceso sin autenticación a los endpoints de usuarios y proveedores
                        .requestMatchers("/api/usuarios/**", "/api/usuarios/login").permitAll()
                        .requestMatchers("/api/proveedores/**").permitAll()
                        .requestMatchers("/api/productos/**").permitAll()
                        .requestMatchers("/api/ventas/**").permitAll()
                        // Cualquier otra solicitud debe estar autenticada
                        .anyRequest().authenticated()
                )

                // Configurar la gestión de sesiones para que sea STATELESS,
                // lo cual es común para APIs REST que usan tokens (como JWT).
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Especifica el origen de tu frontend React
        configuration.addAllowedOrigin("http://localhost:5173"); // O el puerto que use tu React
        configuration.addAllowedMethod("*"); // Permitir todos los métodos (GET, POST, PUT, etc.)
        configuration.addAllowedHeader("*"); // Permitir todas las cabeceras
        // configuration.setAllowCredentials(true); // Descomenta si necesitas enviar cookies/credenciales

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplicar esta configuración a todos los paths bajo /api/
        // Es importante que este path cubra todos tus endpoints de API
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}