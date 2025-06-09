package com.example.EcoSfera.servicios;

import com.example.EcoSfera.config.InformacionEnvioDTO; // Importar DTO de config
import com.example.EcoSfera.config.ItemVentaDTO;       // Importar DTO de config
import com.example.EcoSfera.config.NuevaVentaRequestDTO; // Importar DTO de config
import com.example.EcoSfera.modelos.Producto;
import com.example.EcoSfera.modelos.Usuario;
import com.example.EcoSfera.modelos.Venta;
import com.example.EcoSfera.repositorios.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private UsuarioService usuarioService; // Asumo que tienes un método para obtener usuario por ID
    @Autowired
    private ProductoService productoService; // Asumo que tienes métodos para obtener y guardar producto

    @Transactional
    public Venta crearVenta(NuevaVentaRequestDTO ventaRequestDTO) { // Cambiado a NuevaVentaRequestDTO de config
        // Obtener el usuario
        Long usuarioId = ventaRequestDTO.getUserId();
        Usuario usuario = usuarioService.getUsuarioById(usuarioId) // Asumiendo que getUsuarioById devuelve Optional<Usuario>
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));

        Venta venta = new Venta();
        venta.setFechaVenta(LocalDateTime.now());
        venta.setUsuario(usuario);

        // Establecer información de envío y pago
        InformacionEnvioDTO infoEnvio = ventaRequestDTO.getInformacionEnvio();
        if (infoEnvio != null) {
            venta.setNombreCliente(infoEnvio.getNombre() + " " + infoEnvio.getApellido());
            venta.setDireccionEnvio(infoEnvio.getDireccion());
            venta.setTelefonoEnvio(infoEnvio.getTelefono());
            venta.setTipoDocumentoEnvio(infoEnvio.getTipoDocumento());
            venta.setNumeroDocumentoEnvio(infoEnvio.getNumeroDocumento());
        }
        venta.setMetodoPago(ventaRequestDTO.getMetodoPago());

        double totalVenta = 0.0;

        if (ventaRequestDTO.getItems() == null || ventaRequestDTO.getItems().isEmpty()) {
            throw new IllegalArgumentException("La lista de items no puede estar vacía.");
        }

        // Procesar items de la venta
        for (ItemVentaDTO itemDTO : ventaRequestDTO.getItems()) { // Cambiado a ItemVentaDTO de config
            Producto producto = productoService.obtenerProductoPorId(itemDTO.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + itemDTO.getProductoId()));

            if (producto.getStock() < itemDTO.getCantidad()) {
                throw new IllegalArgumentException("No hay suficiente stock para el producto: " + producto.getNombre());
            }

            totalVenta += producto.getPrecio() * itemDTO.getCantidad();
            producto.setStock(producto.getStock() - itemDTO.getCantidad());
            productoService.guardarProducto(producto); // Asumo que este método guarda/actualiza el producto
        }

        venta.setTotalVenta(totalVenta);
        return ventaRepository.save(venta);
    }

    public List<Venta> obtenerTodasLasVentas() {
        return ventaRepository.findAll();
    }

    public Venta obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id).orElse(null); // Considera lanzar una excepción si no se encuentra
    }

    @Transactional
    public boolean eliminarVenta(Long id) {
        if (ventaRepository.existsById(id)) {
            ventaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}