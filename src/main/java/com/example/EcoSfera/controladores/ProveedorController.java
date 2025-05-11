package com.example.EcoSfera.controladores;

import com.example.EcoSfera.modelos.Proveedor;
import com.example.EcoSfera.servicios.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {
    @Autowired
    private ProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<List<Proveedor>> obtenerTodosLosProveedores() {
        return ResponseEntity.ok(proveedorService.obtenerTodosLosProveedores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proveedor> obtenerProveedorPorId(@PathVariable Long id) {
        Optional<Proveedor> proveedor = proveedorService.obtenerProveedorPorId(id);
        return proveedor.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Proveedor> crearProveedor(@RequestBody Proveedor proveedor) {
        Proveedor nuevoProveedor = proveedorService.guardarProveedor(proveedor);
        return new ResponseEntity<>(nuevoProveedor, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proveedor> actualizarProveedor(@PathVariable Long id, @RequestBody Proveedor proveedorActualizado) {
        Optional<Proveedor> proveedorExistente = proveedorService.obtenerProveedorPorId(id);
        if (proveedorExistente.isPresent()) {
            proveedorActualizado.setId(id);
            Proveedor proveedorGuardado = proveedorService.guardarProveedor(proveedorActualizado);
            return ResponseEntity.ok(proveedorGuardado);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProveedor(@PathVariable Long id) {
        if (proveedorService.obtenerProveedorPorId(id).isPresent()) {
            proveedorService.eliminarProveedor(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
