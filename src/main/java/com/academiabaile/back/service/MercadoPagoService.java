package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Cliente;
import com.academiabaile.back.entidades.Cuota;
import com.academiabaile.back.exception.ResourceNotFoundException;
import com.academiabaile.back.repository.CuotaRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MercadoPagoService {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${server.base-url}")
    private String baseUrl;

    @Autowired
    private CuotaRepository cuotaRepository;

    /**
     * CREA UNA PREFERENCIA DE PAGO PARA UNA CUOTA
     */
    public Preference crearPreferenciaDePago(Long cuotaId) throws MPException, MPApiException {

        // 1. Buscar cuota en base de datos
        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuota no encontrada con id: " + cuotaId));

        // 2. Inicializar el SDK con el token
        MercadoPagoConfig.setAccessToken(accessToken);

        // 3. Crear el item de la preferencia
        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .id(String.valueOf(cuota.getId()))
                .title("Pago de Cuota #" + cuota.getNumeroCuota() +
                        " - Préstamo: " + cuota.getPrestamo().getDescripcion())
                .description("Pago de cuota del préstamo")
                .quantity(1)
                .currencyId("PEN") // CAMBIADO A PERÚ
                .unitPrice(new BigDecimal(String.valueOf(cuota.getMonto())))
                .build();

        // 4. URLs de retorno
        String successUrl = baseUrl + "/api/pagos/mercadopago/success?cuota_id=" + cuotaId;
        String failureUrl = baseUrl + "/api/pagos/mercadopago/failure?cuota_id=" + cuotaId;
        String pendingUrl = baseUrl + "/api/pagos/mercadopago/pending?cuota_id=" + cuotaId;

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(successUrl)
                .failure(failureUrl)
                .pending(pendingUrl)
                .build();
                
        // 5. Información del pagador
        Cliente cliente = cuota.getPrestamo().getCliente();
        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .name(cliente.getNombre())
                .surname(cliente.getApellido())
                .email("test_user_12345678@testuser.com") // Email de prueba requerido por Mercado Pago
                .build();

        // 6. Construir la preferencia
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(List.of(item))
                .payer(payer)
                .backUrls(backUrls)
                .autoReturn("approved")
                .metadata(
                        java.util.Map.of("cuota_id", cuotaId)
                )
                .build();

        // 7. Crear preferencia con el nuevo client()
        PreferenceClient client = new PreferenceClient();
        return client.create(preferenceRequest);
    }


    /**
     * PROCESA WEBHOOK O NOTIFICACIÓN IPN
     */
    public void procesarNotificacion(String paymentId) {

        try {
            // Cargar token
            MercadoPagoConfig.setAccessToken(accessToken);

            // Consultar pago
            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(Long.valueOf(paymentId));

            System.out.println("Estado del pago: " + payment.getStatus());
            System.out.println("Monto pagado: " + payment.getTransactionAmount());
            System.out.println("Metadata: " + payment.getMetadata());

            // Solo procesamos pagos aprobados
            if ("approved".equalsIgnoreCase(payment.getStatus())) {

                // Obtener cuota_id desde metadata
                Long cuotaId = Long.valueOf(payment.getMetadata()
                        .get("cuota_id").toString());

                Cuota cuota = cuotaRepository.findById(cuotaId)
                        .orElseThrow(() -> new ResourceNotFoundException("Cuota no encontrada: " + cuotaId));

                cuota.setPagada(true);
                cuotaRepository.save(cuota);

                System.out.println("✔ Cuota " + cuotaId + " marcada como PAGADA.");
            }

        } catch (Exception e) {
            System.out.println("❌ Error procesando notificación de Mercado Pago:");
            e.printStackTrace();
        }
    }
}
