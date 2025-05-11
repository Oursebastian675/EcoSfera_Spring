package com.example.EcoSfera.repositorios;

import com.example.EcoSfera.modelos.Producto;
import com.example.EcoSfera.modelos.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Puedes agregar métodos de consulta personalizados si los necesitas
}

