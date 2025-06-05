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
import org.springframework.web.filter.CorsFilter;

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
                // Deshabilitar CSRF porque probablemente estás construyendo una API stateless
                // y no usando formularios HTML tradicionales para la autenticación.
                // Si usas tokens (como JWT), CSRF no es tan relevante.
                .csrf(csrf -> csrf.disable())
                // Configurar CORS a través del filtro de Spring Security
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(authz -> authz
                        // Permitir acceso sin autenticación a los endpoints de registro y login
                        .requestMatchers("/api/usuarios", "/api/usuarios/login").permitAll()
                        // Cualquier otra solicitud debe estar autenticada
                        .anyRequest().authenticated()
                )
                // ...

                // Configurar la gestión de sesiones para que sea stateless si usas tokens
                // Si no usas tokens y dependes de sesiones, puedes omitir esto o ajustarlo.
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
        source.registerCorsConfiguration("/api/**", configuration); // Aplicar esta configuración a /api/**
        return source;
    }
}
    