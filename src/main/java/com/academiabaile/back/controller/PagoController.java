package com.academiabaile.back.controller;

import com.academiabaile.back.dto.MercadoPagoInitPointDTO;
import com.academiabaile.back.entidades.Cuota;
import com.academiabaile.back.service.MercadoPagoService;
import com.academiabaile.back.service.PagoService;
// Import corregido para la clase Preference
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private MercadoPagoService mercadoPagoService;

    // Endpoint para pago manual con comprobante
    @PostMapping("/comprobante/{cuotaId}")
    public ResponseEntity<Cuota> pagarConComprobante(@PathVariable Long cuotaId, @RequestParam("comprobante") MultipartFile comprobante) {
        try {
            Cuota cuotaPagada = pagoService.pagarCuotaConComprobante(cuotaId, comprobante);
            return ResponseEntity.ok(cuotaPagada);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoint para INICIAR el pago con Mercado Pago
    @PostMapping("/mercadopago/{cuotaId}")
    public ResponseEntity<MercadoPagoInitPointDTO> iniciarPagoConMercadoPago(@PathVariable Long cuotaId) {
        try {
            Preference preferencia = mercadoPagoService.crearPreferenciaDePago(cuotaId);
            return ResponseEntity.ok(new MercadoPagoInitPointDTO(preferencia.getInitPoint()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Callback de PAGO EXITOSO de Mercado Pago
    @GetMapping("/mercadopago/success")
    public RedirectView pagoExitoso(@RequestParam("cuota_id") Long cuotaId, @RequestParam("payment_id") String paymentId) {
        // Usa el método de servicio más apropiado
        pagoService.pagarCuotaOnline(cuotaId);
        // Redirige al frontend
        return new RedirectView("http://localhost:4200/pago-exitoso?payment_id=" + paymentId);
    }

    // Callback de PAGO FALLIDO de Mercado Pago
    @GetMapping("/mercadopago/failure")
    public RedirectView pagoFallido(@RequestParam("cuota_id") Long cuotaId) {
        return new RedirectView("http://localhost:4200/pago-fallido");
    }

    // Callback de PAGO PENDIENTE de Mercado Pago
    @GetMapping("mercadopago/pending")
    public RedirectView pagoPendiente(@RequestParam("cuota_id") Long cuotaId) {
        return new RedirectView("http://localhost:4200/pago-pendiente");
    }
}
