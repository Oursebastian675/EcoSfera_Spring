package com.example.EcoSfera.servicios;

import com.example.EcoSfera.modelos.Producto;
import com.example.EcoSfera.repositorios.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ProductoServicio {
    @Autowired
    private ProductoRepositorio productoRepositorio;

    public List<Producto> listarProductos() {
        return productoRepositorio.findAll();
    }

    public Producto obtenerProductoPorId(Long id) {
        return productoRepositorio.findById(id).orElse(null);
    }

    public Producto guardarProducto(Producto producto) {
        return productoRepositorio.save(producto);
    }

    public void eliminarProducto(Long id) {
        productoRepositorio.deleteById(id);
    }

}
