package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Pago;
import com.academiabaile.back.repository.PagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    public Pago guardarPago(Pago pago) {
        return pagoRepository.save(pago);
    }

    public List<Pago> obtenerPagos() {
        return pagoRepository.findAll();
    }

    public Optional<Pago> obtenerPagoPorId(Long id) {
        return pagoRepository.findById(id);
    }
}
