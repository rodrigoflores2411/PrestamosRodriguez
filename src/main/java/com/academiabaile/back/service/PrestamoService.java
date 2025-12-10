package com.academiabaile.back.service;

import com.academiabaile.back.dto.PrestamoRequestDTO;
import com.academiabaile.back.entidades.Cliente;
import com.academiabaile.back.entidades.Cuota;
import com.academiabaile.back.entidades.Prestamo;
import com.academiabaile.back.exception.ResourceNotFoundException;
import com.academiabaile.back.repository.ClienteRepository;
import com.academiabaile.back.repository.CuotaRepository;
import com.academiabaile.back.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
public class PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CuotaRepository cuotaRepository;

    @Transactional
    public Prestamo registrarPrestamo(PrestamoRequestDTO prestamoRequest) {
        Cliente cliente = clienteRepository.findById(prestamoRequest.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + prestamoRequest.getClienteId()));

        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);
        prestamo.setMonto(prestamoRequest.getMonto());
        prestamo.setNumeroCuotas(prestamoRequest.getNumeroCuotas());
        prestamo.setFechaInicio(prestamoRequest.getFechaInicio());
        prestamo.setPagado(false);

        Prestamo nuevoPrestamo = prestamoRepository.save(prestamo);

        double montoCuota = prestamo.getMonto() / prestamo.getNumeroCuotas();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prestamo.getFechaInicio());

        for (int i = 1; i <= prestamo.getNumeroCuotas(); i++) {
            Cuota cuota = new Cuota();
            cuota.setPrestamo(nuevoPrestamo);
            cuota.setNumeroCuota(i);
            cuota.setMonto(montoCuota);
            cuota.setFechaPago(calendar.getTime());
            cuota.setPagada(false);
            cuotaRepository.save(cuota);

            calendar.add(Calendar.MONTH, 1);
        }

        return nuevoPrestamo;
    }

    public List<Prestamo> obtenerPrestamos() {
        return prestamoRepository.findAll();
    }

    public Optional<Prestamo> obtenerPrestamoPorId(Long id) {
        return prestamoRepository.findById(id);
    }
}
