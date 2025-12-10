package com.academiabaile.back.controller;

import com.academiabaile.back.entidades.Prestamo;
import com.academiabaile.back.service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @PostMapping
    public Prestamo crearPrestamo(@RequestBody Prestamo prestamo) {
        return prestamoService.registrarPrestamo(prestamo);
    }

    @GetMapping
    public List<Prestamo> obtenerPrestamos() {
        return prestamoService.obtenerPrestamos();
    }
}
