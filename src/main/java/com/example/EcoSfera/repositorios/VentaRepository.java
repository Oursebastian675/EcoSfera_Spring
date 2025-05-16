package com.example.EcoSfera.repositorios;
import com.example.EcoSfera.modelos.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
  //  @Override
  // Optional<Venta> findById(Long id);
}
