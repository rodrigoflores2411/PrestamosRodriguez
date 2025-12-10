package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Prestamo;
import com.academiabaile.back.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class CalculoDeudaService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    /**
     * Calcula la deuda total de un préstamo, incluyendo cuotas atrasadas e interés por mora.
     * @param prestamoId El ID del préstamo a calcular.
     * @return Un mapa con el desglose de la deuda.
     */
    public Map<String, Object> calcularDeudaTotal(Long prestamoId) {
        // Busca el préstamo o lanza una excepción si no se encuentra.
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new RuntimeException("Préstamo con id " + prestamoId + " no encontrado."));

        // --- 1. Calcular la Cuota Mensual (Fórmula de Amortización Francesa) ---
        double tasaInteresMensual = prestamo.getInteres() / 100 / 12;
        double montoPrincipal = prestamo.getMonto();
        int numeroCuotas = prestamo.getPlazo();
        
        double cuotaMensual = (montoPrincipal * tasaInteresMensual * Math.pow(1 + tasaInteresMensual, numeroCuotas))
                            / (Math.pow(1 + tasaInteresMensual, numeroCuotas) - 1);
        
        // Si el interés es 0, la fórmula da NaN. Usamos una simple división como alternativa.
        if (Double.isNaN(cuotaMensual) || Double.isInfinite(cuotaMensual)) {
            cuotaMensual = montoPrincipal / numeroCuotas;
        }

        // --- 2. Calcular los Meses de Mora ---
        // Calcula cuántos meses han pasado desde que se otorgó el préstamo.
        long mesesTranscurridos = ChronoUnit.MONTHS.between(prestamo.getFecha(), LocalDate.now());
        
        // Cuenta cuántos pagos se han realizado.
        int pagosRealizados = prestamo.getPagos() != null ? prestamo.getPagos().size() : 0;
        
        // Los meses de mora son la diferencia entre los meses que han pasado y los pagos que se han hecho.
        long mesesDeMora = Math.max(0, mesesTranscurridos - pagosRealizados);

        // --- 3. Calcular Interés Compuesto por Mora (1% mensual sobre el balance atrasado) ---
        double interesPorMoraTotal = 0.0;
        double balanceAtrasado = 0.0;
        final double TASA_MORA = 0.01; // 1%

        // Se simula mes a mes el cálculo de la mora.
        for (int i = 0; i < mesesDeMora; i++) {
            // Se añade la cuota del mes que no se pagó al balance atrasado.
            balanceAtrasado += cuotaMensual; 
            
            // Se calcula la penalidad de este mes sobre el total del balance atrasado.
            double penalidadDelMes = balanceAtrasado * TASA_MORA;
            interesPorMoraTotal += penalidadDelMes;
            
            // La penalidad se suma al balance para el cálculo del siguiente mes (interés compuesto).
            balanceAtrasado += penalidadDelMes; 
        }

        // --- 4. Calcular el Monto Total a Pagar ---
        double montoCuotasAtrasadas = cuotaMensual * mesesDeMora;
        double totalAPagar = montoCuotasAtrasadas + interesPorMoraTotal;
        
        // --- 5. Preparar el resultado en un mapa ---
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
