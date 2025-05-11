package com.example.EcoSfera.servicios;

import com.example.EcoSfera.modelos.DetalleVenta;
import com.example.EcoSfera.modelos.Producto;
import com.example.EcoSfera.modelos.Venta;
import com.example.EcoSfera.repositorios.ProductoRepository;
import com.example.EcoSfera.repositorios.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VentaService {
    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Transactional
    public Venta realizarVenta(Long usuarioId, List<DetalleVentaDTO> detalles) {
        // 1. Obtener el usuario
        // 2. Crear la entidad Venta
        // 3. Para cada detalle de venta:
        //    - Obtener el producto
        //    - Crear la entidad DetalleVenta
        //    - Actualizar el stock del producto
        // 4. Guardar la venta y los detalles de venta

        // Implementación (ejemplo simplificado):
        // ... (obtener usuario)
        Venta venta = new Venta();
        // venta.setUsuario(usuario); // Asigna el usuario a la venta

        BigDecimal totalVenta = BigDecimal.ZERO;
        List<DetalleVenta> detallesVentaList = new ArrayList<>();

        for (DetalleVentaDTO detalleDTO : detalles) {
            Optional<Producto> productoOptional = productoRepository.findById(detalleDTO.getProductoId());
            if (productoOptional.isPresent()) {
                Producto producto = productoOptional.get();
                if (producto.getStock() >= detalleDTO.getCantidad()) {
                    DetalleVenta detalle = new DetalleVenta();
                    detalle.setVenta(venta);
                    detalle.setProducto(producto);
                    detalle.setCantidad(detalleDTO.getCantidad());
                    detalle.setPrecioUnitario(producto.getPrecio()); // Puedes usar un precio diferente si es necesario
                    detallesVentaList.add(detalle);
                    totalVenta = totalVenta.add(producto.getPrecio().multiply(new BigDecimal(detalleDTO.getCantidad())));
                    producto.setStock(producto.getStock() - detalleDTO.getCantidad());
                    productoRepository.save(producto); // Actualizar stock
                } else {
                    throw new RuntimeException("No hay suficiente stock para el producto con ID: " + producto.getId());
                }
            } else {
                throw new RuntimeException("Producto no encontrado con ID: " + detalleDTO.getProductoId());
            }
        }

        venta.setDetallesVenta(detallesVentaList);
        venta.setTotalVenta(totalVenta);
        return ventaRepository.save(venta);
    }

    public List<Venta> obtenerTodasLasVentas() {
        return ventaRepository.findAll();
    }

    public Optional<Venta> obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id);
    }

    // Otros métodos para gestionar ventas
}
