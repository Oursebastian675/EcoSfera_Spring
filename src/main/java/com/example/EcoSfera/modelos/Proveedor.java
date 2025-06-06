package com.example.EcoSfera.modelos;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Importa esta anotación
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String direccion;
    private String telefono;

    @OneToMany(mappedBy = "proveedor", cascade = CascadeType.ALL, fetch = FetchType.LAZY) // fetch = FetchType.LAZY es buena práctica
    @JsonManagedReference // <-- AÑADE ESTO
    private List<Producto> productos;
}
    