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

import java.util.ArrayList;
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
        // 1. Buscar Cliente
        Cliente cliente = clienteRepository.findById(prestamoRequest.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + prestamoRequest.getClienteId()));

        // 2. Crear y Guardar Préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);
        prestamo.setDescripcion(prestamoRequest.getDescripcion());
        prestamo.setFechaEmision(prestamoRequest.getFechaEmision());
        prestamo.setMonto(prestamoRequest.getMonto());
        prestamo.setInteres(prestamoRequest.getInteres());
        prestamo.setMeses(prestamoRequest.getMeses());
        prestamo.setPagado(false);

        Prestamo nuevoPrestamo = prestamoRepository.save(prestamo);

        // 3. Calcular Monto Total y 4. Monto de Cuota
        double montoTotalConInteres = prestamo.getMonto() * (1 + prestamo.getInteres() / 100);
        double montoCuota = montoTotalConInteres / prestamo.getMeses();

        // 5. Generar y Guardar Cuotas
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prestamo.getFechaEmision());
        List<Cuota> cuotas = new ArrayList<>();
        for (int i = 1; i <= prestamo.getMeses(); i++) {
            calendar.add(Calendar.MONTH, 1);
            Cuota cuota = new Cuota();
            cuota.setPrestamo(nuevoPrestamo);
            cuota.setNumeroCuota(i);
            cuota.setMonto(montoCuota);
            cuota.setFechaPago(calendar.getTime());
            cuota.setPagada(false);
            cuotas.add(cuotaRepository.save(cuota));
        }
        
        nuevoPrestamo.setCuotas(cuotas);

        // 6. Devolver Préstamo (con cuotas asociadas por la transacción)
        return nuevoPrestamo;
    }

    public List<Prestamo> obtenerPrestamos() {
        return prestamoRepository.findAll();
    }

    public Optional<Prestamo> obtenerPrestamoPorId(Long id) {
        return prestamoRepository.findById(id);
    }
}
