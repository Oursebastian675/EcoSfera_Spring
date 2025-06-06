package com.example.EcoSfera.controladores;

import com.example.EcoSfera.modelos.Venta;
import com.example.EcoSfera.servicios.NuevaVentaRequest;
import com.example.EcoSfera.servicios.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Covers GetMapping, PostMapping, DeleteMapping, PathVariable, etc.

import java.util.List; // Para devolver una lista de ventas

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @PostMapping
    public ResponseEntity<?> crearVenta(@RequestBody NuevaVentaRequest ventaRequest) { // Cambiado Object a ResponseEntity<?>
        try {
            Venta nuevaVenta = ventaService.crearVenta(ventaRequest);
            return new ResponseEntity<>(nuevaVenta, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) { // Captura general para otros errores inesperados
            // Consider logging the exception: log.error("Error al crear venta", e);
            return new ResponseEntity<>("Ocurrió un error procesando la venta: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- MÉTODOS GET ---

    @GetMapping
    public ResponseEntity<List<Venta>> obtenerTodasLasVentas() {
        List<Venta> ventas = ventaService.obtenerTodasLasVentas();
        if (ventas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(ventas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerVentaPorId(@PathVariable Long id) {
        Venta venta = ventaService.obtenerVentaPorId(id);
        if (venta == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(venta, HttpStatus.OK);
    }

    // --- NUEVO MÉTODO DELETE ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVenta(@PathVariable Long id) {
        try {
            boolean eliminada = ventaService.eliminarVenta(id);
            if (eliminada) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // HTTP 204: Éxito, sin contenido que devolver
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // HTTP 404: Venta no encontrada
            }
        } catch (Exception e) {
            // Es una buena práctica registrar el error en el servidor
            // log.error("Error al intentar eliminar la venta con ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // HTTP 500: Error interno del servidor
        }
    }
}