package com.academiabaile.back.controller;

import com.academiabaile.back.dto.PrestamoRequestDTO;
import com.academiabaile.back.entidades.Prestamo;
import com.academiabaile.back.service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @PostMapping
    public ResponseEntity<Prestamo> crearPrestamo(@RequestBody PrestamoRequestDTO prestamoRequest) {
        Prestamo nuevoPrestamo = prestamoService.registrarPrestamo(prestamoRequest);
        return ResponseEntity.ok(nuevoPrestamo);
    }

    @GetMapping
    public List<Prestamo> obtenerPrestamos() {
        return prestamoService.obtenerPrestamos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prestamo> obtenerPrestamoPorId(@PathVariable Long id) {
        return prestamoService.obtenerPrestamoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
