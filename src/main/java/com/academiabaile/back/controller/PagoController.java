package com.academiabaile.back.controller;

import com.academiabaile.back.entidades.Cuota;
import com.academiabaile.back.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @PostMapping("/comprobante/{cuotaId}")
    public ResponseEntity<Cuota> pagarConComprobante(@PathVariable Long cuotaId, @RequestParam("comprobante") MultipartFile comprobante) {
        try {
            Cuota cuotaPagada = pagoService.pagarCuotaConComprobante(cuotaId, comprobante);
            return ResponseEntity.ok(cuotaPagada);
        } catch (IOException e) {
            // Considera un manejo de excepciones más robusto aquí
            return ResponseEntity.status(500).build();
        }
    }
}
