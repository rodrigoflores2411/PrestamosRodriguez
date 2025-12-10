
package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Cuota;
import com.academiabaile.back.exception.ResourceNotFoundException;
import com.academiabaile.back.repository.CuotaRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${server.base-url}")
    private String baseUrl;

    @Autowired
    private CuotaRepository cuotaRepository;

    public Preference crearPreferenciaDePago(Long cuotaId) throws MPException, MPApiException {
        // 1. Encontrar la cuota
        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuota no encontrada con id: " + cuotaId));

        // 2. Inicializar Mercado Pago SDK
        MercadoPagoConfig.setAccessToken(accessToken);

        // 3. Crear item de la preferencia
        PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                .id(String.valueOf(cuota.getId()))
                .title("Pago de Cuota Nro: " + cuota.getNumeroCuota() + " - Préstamo " + cuota.getPrestamo().getDescripcion())
                .description("Pago de cuota del préstamo.")
                .quantity(1)
                .currencyId("ARS") // O la moneda que corresponda
                .unitPrice(new BigDecimal(cuota.getMonto()))
                .build();

        List<PreferenceItemRequest> items = new ArrayList<>();
        items.add(itemRequest);

        // 4. Configurar URLs de redirección (Callbacks)
        String successUrl = baseUrl + "/api/pagos/mercadopago/success?cuota_id=" + cuotaId;
        String failureUrl = baseUrl + "/api/pagos/mercadopago/failure?cuota_id=" + cuotaId;
        String pendingUrl = baseUrl + "/api/pagos/mercadopago/pending?cuota_id=" + cuotaId;

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(successUrl)
                .failure(failureUrl)
                .pending(pendingUrl)
                .build();
        
        // 5. Crear la preferencia de pago
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .autoReturn("approved") // Redirección automática solo en caso de pago aprobado
                .build();

        PreferenceClient client = new PreferenceClient();
        return client.create(preferenceRequest);
    }
}
