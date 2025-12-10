package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Prestamo;
import com.academiabaile.back.repository.PagoRepository;
import com.academiabaile.back.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class CalculoDeudaService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private PagoRepository pagoRepository; // Dependencia para consultar pagos

    public Map<String, Object> calcularDeudaTotal(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new RuntimeException("Préstamo con id " + prestamoId + " no encontrado."));

        // --- 1. Calcular Cuota Mensual ---
        double tasaInteresMensual = prestamo.getInteres() / 100.0 / 12.0;
        double montoPrincipal = prestamo.getMonto();
        int numeroCuotas = prestamo.getPlazo();

        double cuotaMensual = (montoPrincipal * tasaInteresMensual * Math.pow(1 + tasaInteresMensual, numeroCuotas))
                            / (Math.pow(1 + tasaInteresMensual, numeroCuotas) - 1);

        if (Double.isNaN(cuotaMensual) || Double.isInfinite(cuotaMensual) || cuotaMensual <= 0) {
            cuotaMensual = montoPrincipal / numeroCuotas;
        }

        // --- 2. Calcular Meses de Mora ---
        // CORRECTO: Convertir java.util.Date a LocalDate para poder comparar
        LocalDate fechaPrestamo = prestamo.getFecha().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        long mesesTranscurridos = ChronoUnit.MONTHS.between(fechaPrestamo, LocalDate.now());

        // CORRECTO: Contar los pagos desde el repositorio, no desde la entidad Préstamo
        long pagosRealizados = pagoRepository.countByPrestamoId(prestamoId);

        long mesesDeMora = Math.max(0, mesesTranscurridos - pagosRealizados);

        // --- 3. Calcular Interés Compuesto por Mora ---
        double interesPorMoraTotal = 0.0;
        double balanceAtrasado = 0.0;
        final double TASA_MORA = 0.01; // 1%

        for (int i = 0; i < mesesDeMora; i++) {
            balanceAtrasado += cuotaMensual;
            double penalidadDelMes = balanceAtrasado * TASA_MORA;
            interesPorMoraTotal += penalidadDelMes;
            balanceAtrasado += penalidadDelMes;
        }

        // --- 4. Consolidar Resultados ---
        double montoCuotasAtrasadas = cuotaMensual * mesesDeMora;
        double totalAPagar = montoCuotasAtrasadas + interesPorMoraTotal;

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("prestamoId", prestamoId);
        resultado.put("montoDelPrestamo", montoPrincipal);
        resultado.put("cuotaMensualEstimada", cuotaMensual);
        resultado.put("mesesTranscurridos", mesesTranscurridos);
        resultado.put("pagosRealizados", pagosRealizados);
        resultado.put("mesesDeMora", mesesDeMora);
        resultado.put("montoBaseDeuda", montoCuotasAtrasadas);
        resultado.put("interesPorMoraAcumulado", interesPorMoraTotal);
        resultado.put("totalDeudaCalculada", totalAPagar);
        resultado.put("fechaCalculo", LocalDate.now().toString());

        return resultado;
    }
}
