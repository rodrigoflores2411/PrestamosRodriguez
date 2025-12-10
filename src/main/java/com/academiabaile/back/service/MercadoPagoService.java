package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Cuota;
import com.academiabaile.back.entidades.Pago;
import com.academiabaile.back.repository.CuotaRepository;
import com.academiabaile.back.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MercadoPagoService {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    private final CuotaRepository cuotaRepository;
    private final PagoRepository pagoRepository;

    public MercadoPagoService(CuotaRepository cuotaRepository, PagoRepository pagoRepository) {
        this.cuotaRepository = cuotaRepository;
        this.pagoRepository = pagoRepository;
    }

    public String crearPreferencia(Long cuotaId) {
        RestTemplate restTemplate = new RestTemplate();

        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));

        // URL base de redirección (frontend)
        String baseUrl = "http://localhost:4200/prestamos"; // Cambia esto por la URL de tu frontend

        // Construir URLs con parámetros
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("prestamoId", cuota.getPrestamo().getId());

        String successUrl = builder.cloneBuilder().queryParam("payment_status", "success").build().toUriString();
        String failureUrl = builder.cloneBuilder().queryParam("payment_status", "failure").build().toUriString();
        String pendingUrl = builder.cloneBuilder().queryParam("payment_status", "pending").build().toUriString();

        // Headers con autorización
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Detalle del producto a pagar
        Map<String, Object> item = Map.of(
                "title", "Pago de cuota Nro " + cuota.getNumeroCuota(),
                "quantity", 1,
                "currency_id", "PEN",
                "unit_price", cuota.getMonto()
        );

        // Cuerpo de la preferencia
        Map<String, Object> body = new HashMap<>();
        body.put("items", List.of(item));
        body.put("metadata", Map.of("cuota_id", cuotaId));
        body.put("notification_url", "https://tu-backend.com/api/mercadopago/webhook"); // Cambia esto por la URL de tu backend
        body.put("back_urls", Map.of(
                "success", successUrl,
                "failure", failureUrl,
                "pending", pendingUrl
        ));
        body.put("auto_return", "approved");

        // Ejecutar la solicitud POST
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.mercadopago.com/checkout/preferences",
                entity,
                Map.class
        );

        // Retornar sandbox_init_point para pruebas
        return response.getBody().get("sandbox_init_point").toString();
    }

    public boolean procesarNotificacion(String paymentId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> resp = restTemplate.exchange(
                "https://api.mercadopago.com/v1/payments/" + paymentId,
                HttpMethod.GET,
                entity,
                Map.class
        );

        String status = String.valueOf(resp.getBody().get("status"));
        Map<?, ?> metadata = (Map<?, ?>) resp.getBody().get("metadata");
        Long cuotaId = ((Number) metadata.get("cuota_id")).longValue();

        if ("approved".equals(status)) {
            Cuota cuota = cuotaRepository.findById(cuotaId)
                    .orElseThrow(() -> new RuntimeException("Cuota no encontrada en pago exitoso"));

            if (!cuota.isPagada()) {
                cuota.setPagada(true);
                cuotaRepository.save(cuota);

                Pago pago = new Pago();
                pago.setPrestamo(cuota.getPrestamo());
                pago.setFecha(new Date());
                pago.setMonto(cuota.getMonto());
                pagoRepository.save(pago);
            }
            return true;
        }

        return false;
    }
}
