package com.academiabaile.back.controller;

import com.academiabaile.back.service.MercadoPagoService;
import com.mercadopago.exceptions.MPException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/mercadopago")
public class MercadoPagoController {

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @PostMapping("/create-preference")
    public String createPaymentPreference(@RequestParam String title, @RequestParam int quantity, @RequestParam BigDecimal unitPrice) throws MPException {
        return mercadoPagoService.createPaymentPreference(title, quantity, unitPrice);
    }
}
