package com.example.EcoSfera.repositorios;

import com.example.EcoSfera.modelos.Producto;
import com.example.EcoSfera.modelos.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
   // @Override
 //   Optional<Producto> findById(Long id);
}

