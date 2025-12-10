package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Cuota;
import com.academiabaile.back.entidades.Prestamo;
import com.academiabaile.back.repository.CuotaRepository;
import com.academiabaile.back.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private CuotaRepository cuotaRepository;

    public Prestamo registrarPrestamo(Prestamo prestamo) {
        Prestamo nuevoPrestamo = prestamoRepository.save(prestamo);
        generarCuotas(nuevoPrestamo);
        return nuevoPrestamo;
    }

    private void generarCuotas(Prestamo prestamo) {
        double montoCuota = (prestamo.getMonto() * (1 + prestamo.getInteres())) / prestamo.getMeses();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prestamo.getFechaEmision());

        List<Cuota> cuotas = new ArrayList<>();
        for (int i = 1; i <= prestamo.getMeses(); i++) {
            calendar.add(Calendar.MONTH, 1);
            Cuota cuota = new Cuota();
            cuota.setPrestamo(prestamo);
            cuota.setNumeroCuota(i);
            cuota.setFechaPago(calendar.getTime());
            cuota.setMonto(montoCuota);
            cuota.setPagada(false);
            cuotas.add(cuota);
        }
        cuotaRepository.saveAll(cuotas);
    }

    public void actualizarIntereses() {
        List<Cuota> cuotas = cuotaRepository.findByPagadaIsFalseAndFechaPagoBefore(new Date());
        for (Cuota cuota : cuotas) {
            long diasDeAtraso = (new Date().getTime() - cuota.getFechaPago().getTime()) / (1000 * 60 * 60 * 24);
            int mesesDeAtraso = (int) (diasDeAtraso / 30);

            if (mesesDeAtraso > 0) {
                double interesAcumulativo = Math.pow(1.01, mesesDeAtraso);
                cuota.setMonto(cuota.getMonto() * interesAcumulativo);
                cuotaRepository.save(cuota);
            }
        }
    }

    public List<Prestamo> obtenerPrestamos() {
        return prestamoRepository.findAll();
    }
}
