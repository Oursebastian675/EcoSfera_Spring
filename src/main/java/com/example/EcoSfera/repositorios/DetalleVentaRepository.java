package com.example.EcoSfera.repositorios;

import com.example.EcoSfera.modelos.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    // Métodos de consulta personalizados
}
