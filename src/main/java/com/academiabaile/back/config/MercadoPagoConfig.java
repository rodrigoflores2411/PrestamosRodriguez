package com.academiabaile.back.config;

import com.mercadopago.MercadoPagoConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoConfig {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Bean
    public void mercadoPago() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }
}
