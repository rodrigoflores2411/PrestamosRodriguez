package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Cuota;
import com.academiabaile.back.entidades.Prestamo;
import com.academiabaile.back.repository.CuotaRepository;
import com.academiabaile.back.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalculoDeudaService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private CuotaRepository cuotaRepository;

    public Map<String, Object> calcularDeudaTotal(Long id) {
        Prestamo prestamo = prestamoRepository.findById(id).orElse(null);
        if (prestamo == null) {
            return null;
        }

        List<Cuota> cuotas = prestamo.getCuotas();
        double deudaTotal = 0;
        for (Cuota cuota : cuotas) {
            if (!cuota.isPagada()) {
                deudaTotal += cuota.getMonto();
            }
        }

        Map<String, Object> datosDeuda = new HashMap<>();
        datosDeuda.put("prestamo", prestamo);
        datosDeuda.put("deudaTotal", deudaTotal);
        datosDeuda.put("cuotas", cuotas);

        return datosDeuda;
    }
}
