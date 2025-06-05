package com.example.EcoSfera.repositorios;

import com.example.EcoSfera.modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByUsuario(String usuario); // <-- DESCOMENTA O AÑADE ESTA LÍNEA
}