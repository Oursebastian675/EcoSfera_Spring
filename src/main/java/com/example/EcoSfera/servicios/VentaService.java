package com.example.EcoSfera.servicios;

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


}
