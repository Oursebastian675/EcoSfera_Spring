package com.example.EcoSfera.repositorios;
import com.example.EcoSfera.modelos.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    // Métodos de consulta personalizados
}
