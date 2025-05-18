package com.example.EcoSfera.servicios;
import com.example.EcoSfera.modelos.Producto;
import com.example.EcoSfera.modelos.Usuario;
import com.example.EcoSfera.modelos.Venta;
import com.example.EcoSfera.repositorios.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private ProductoService productoService;

    @Transactional
    public Venta crearVenta(NuevaVentaRequest ventaRequest) {
        Long usuarioId = ventaRequest.getUsuarioId();
        Usuario usuario = usuarioService.getUsuarioById(usuarioId).orElse(null);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId);
        }

        Venta venta = new Venta();
        venta.setFechaVenta(LocalDateTime.now());
        venta.setUsuario(usuario);
        double totalVenta = 0.0;

        for (NuevaVentaRequest.ItemVenta item : ventaRequest.getItems()) {
            Producto producto = productoService.obtenerProductoPorId(item.getProductoId()).orElse(null);
            if (producto == null) {
                throw new IllegalArgumentException("Producto no encontrado con ID: " + item.getProductoId());
            }
            if (producto.getStock() < item.getCantidad()) {
                throw new IllegalArgumentException("No hay suficiente stock para el producto: " + producto.getNombre());
            }

            totalVenta += producto.getPrecio() * item.getCantidad();
            producto.setStock(producto.getStock() - item.getCantidad());
            productoService.guardarProducto(producto); // Asumo que tienes este método
        }

        venta.setTotalVenta(totalVenta);
        return ventaRepository.save(venta);
    }

    public Venta obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }
}