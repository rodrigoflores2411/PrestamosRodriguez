package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Cuota;
import com.academiabaile.back.repository.CuotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class CalculoInteresService {

    @Autowired
    private CuotaRepository cuotaRepository;

    /**
     * Este método se ejecuta automáticamente todos los días a la 1:00 AM.
     * Busca todas las cuotas no pagadas y, si están vencidas, calcula y aplica
     * el interés por mora de forma acumulativa.
     */
    @Scheduled(cron = "0 0 1 * * ?") // Se ejecuta cada día a la 1 AM
    @Transactional
    public void actualizarInteresesPorMora() {
        System.out.println("Ejecutando tarea programada: Actualizando intereses por mora...");
        List<Cuota> cuotasNoPagadas = cuotaRepository.findByPagada(false);
        LocalDate fechaActual = LocalDate.now();

        for (Cuota cuota : cuotasNoPagadas) {
            LocalDate fechaPago = cuota.getFechaPago().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (fechaActual.isAfter(fechaPago)) {
                // La cuota está vencida. Calcular los meses de retraso.
                long mesesDeRetraso = ChronoUnit.MONTHS.between(fechaPago, fechaActual);

                if (mesesDeRetraso > 0) {
                    // Aplicar interés compuesto por cada mes completo de retraso
                    double nuevoMonto = cuota.getMonto();
                    for (int i = 0; i < mesesDeRetraso; i++) {
                        nuevoMonto *= 1.01; // Aplicar 1% de interés acumulativo
                    }

                    // Actualizar solo si el monto ha cambiado
                    if (nuevoMonto > cuota.getMonto()) {
                        cuota.setMonto(nuevoMonto);
                        cuotaRepository.save(cuota);
                        System.out.println("Interés por mora aplicado a la cuota #" + cuota.getNumeroCuota() + " del préstamo #" + cuota.getPrestamo().getId() + ". Nuevo monto: " + nuevoMonto);
                    }
                }
            }
        }
        System.out.println("Tarea de actualización de intereses finalizada.");
    }
}
