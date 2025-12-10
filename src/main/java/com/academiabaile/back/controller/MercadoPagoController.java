package com.academiabaile.back.controller;

import com.academiabaile.back.service.MercadoPagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mercadopago")
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;

    public MercadoPagoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    @PostMapping("/preferencia")
    public ResponseEntity<Map<String, String>> crearPreferencia(@RequestBody Map<String, Long> request) {
        Long cuotaId = request.get("cuotaId");
        String urlPreferencia = mercadoPagoService.crearPreferencia(cuotaId);
        return ResponseEntity.ok(Map.of("urlPreferencia", urlPreferencia));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirNotificacion(@RequestParam("data.id") String paymentId) {
        mercadoPagoService.procesarNotificacion(paymentId);
        return ResponseEntity.ok().build();
    }
}
