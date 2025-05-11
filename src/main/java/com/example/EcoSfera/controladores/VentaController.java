package com.example.EcoSfera.controladores;

import com.example.EcoSfera.modelos.Venta;
import com.example.EcoSfera.servicios.DetalleVentaDTO;
import com.example.EcoSfera.servicios.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    @Autowired
    private VentaService ventaService;

    @PostMapping("/{usuarioId}")
    public ResponseEntity<Venta> realizarVenta(@PathVariable Long usuarioId, @RequestBody List<DetalleVentaDTO> detalles) {
        try {
            Venta nuevaVenta = ventaService.realizarVenta(usuarioId, detalles);
            return new ResponseEntity<>(nuevaVenta, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // O manejar el error de forma más específica
        }
    }

    @GetMapping
    public ResponseEntity<List<Venta>> obtenerTodasLasVentas() {
        return ResponseEntity.ok(ventaService.obtenerTodasLasVentas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerVentaPorId(@PathVariable Long id) {
        Optional<Venta> venta = ventaService.obtenerVentaPorId(id);
        return venta.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Otros endpoints para gestionar ventas
}