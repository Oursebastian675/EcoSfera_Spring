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
    public ResponseEntity<List<Proveedor>> getAllProveedores() {
        return new ResponseEntity<>(proveedorService.getAllProveedores(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proveedor> getProveedorById(@PathVariable Long id) {
        Optional<Proveedor> proveedor = proveedorService.getProveedorById(id);
        return proveedor.map(p -> new ResponseEntity<>(p, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Proveedor> createProveedor(@RequestBody Proveedor proveedor) {
        return new ResponseEntity<>(proveedorService.saveProveedor(proveedor), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proveedor> updateProveedor(@PathVariable Long id, @RequestBody Proveedor proveedor) {
        Optional<Proveedor> existingProveedor = proveedorService.getProveedorById(id);
        if (existingProveedor.isPresent()) {
            proveedor.setId(id);
            return new ResponseEntity<>(proveedorService.saveProveedor(proveedor), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProveedor(@PathVariable Long id) {
        proveedorService.deleteProveedor(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
