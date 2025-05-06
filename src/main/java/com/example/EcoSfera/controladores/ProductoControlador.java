package com.example.EcoSfera.controladores;
import com.example.EcoSfera.modelos.Producto;
import com.example.EcoSfera.servicios.ProductoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productos")

public class ProductoControlador {
    @Autowired
    private ProductoServicio productoServicio;

    @GetMapping
    public List<Producto> listarProductos() {
        return productoServicio.listarProductos();
    }

    @GetMapping("/{id}")
    public Producto obtenerProductoPorId(@PathVariable Long id) {
        return productoServicio.obtenerProductoPorId(id);
    }

    @PostMapping
    public Producto guardarProducto(@RequestBody Producto producto) {
        return productoServicio.guardarProducto(producto);
    }

    @DeleteMapping("/{id}")
    public void eliminarProducto(@PathVariable Long id) {
        productoServicio.eliminarProducto(id);
    }

}
