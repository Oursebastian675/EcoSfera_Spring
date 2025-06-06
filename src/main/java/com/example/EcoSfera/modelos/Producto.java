// Asumiendo que tienes un archivo Producto.java similar a este:
package com.example.EcoSfera.modelos;

import com.fasterxml.jackson.annotation.JsonBackReference; // Importa esta anotación
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock; // O el tipo de dato que uses para stock

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id") // Asegúrate que el nombre de la columna sea correcto
    @JsonBackReference // <-- AÑADE ESTO
    private Proveedor proveedor;
}
    