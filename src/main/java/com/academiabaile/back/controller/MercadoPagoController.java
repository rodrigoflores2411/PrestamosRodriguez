package com.academiabaile.back.controller;

import com.academiabaile.back.service.MercadoPagoService;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.http.HttpStatus;
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

    /**
     * Endpoint alternativo para crear una preferencia de pago.
     * Recibe un JSON con "cuotaId" y devuelve la URL de pago.
     */
    @PostMapping("/preferencia")
    public ResponseEntity<?> crearPreferencia(@RequestBody Map<String, Long> request) {
        try {
            Long cuotaId = request.get("cuotaId");
            // SOLUCIÓN 1: Guardar el objeto Preference completo
            Preference preferencia = mercadoPagoService.crearPreferenciaDePago(cuotaId);
            
            // SOLUCIÓN 2: Extraer la URL (init_point) del objeto
            String urlPreferencia = preferencia.getInitPoint();
            
            return ResponseEntity.ok(Map.of("urlPreferencia", urlPreferencia));
        } catch (MPException | MPApiException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /*
     * SOLUCIÓN 3: La lógica de Webhook es más compleja.
     * El método 'procesarNotificacion' no existe porque la confirmación del pago
     * ya se maneja de forma más simple en PagoController con el endpoint '/success'.
     * Por eso, esta sección se deja comentada para evitar errores de compilación.
     */
    // @PostMapping("/webhook")
    // public ResponseEntity<Void> recibirNotificacion(@RequestParam("data.id") String paymentId) {
    //     // Para implementar esto, se necesitaría crear un método procesarNotificacion() en el servicio.
    //     // mercadoPagoService.procesarNotificacion(paymentId);
    //     return ResponseEntity.ok().build();
    // }
}
