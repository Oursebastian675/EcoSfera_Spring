package com.example.EcoSfera.repositorios;

import com.example.EcoSfera.modelos.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Métodos de consulta personalizados, por ejemplo, buscar por nombre de usuario
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
}