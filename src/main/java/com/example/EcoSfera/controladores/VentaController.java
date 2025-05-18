package com.example.EcoSfera.controladores;


import com.example.EcoSfera.modelos.Venta;
import com.example.EcoSfera.servicios.NuevaVentaRequest;
import com.example.EcoSfera.servicios.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @PostMapping
    public Object crearVenta(@RequestBody NuevaVentaRequest ventaRequest) {
        try {
            Venta nuevaVenta = ventaService.crearVenta(ventaRequest);
            return new ResponseEntity<>(nuevaVenta, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}