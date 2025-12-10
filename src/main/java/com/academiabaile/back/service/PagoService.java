package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Cuota;
import com.academiabaile.back.entidades.Prestamo;
import com.academiabaile.back.exception.ResourceNotFoundException;
import com.academiabaile.back.repository.CuotaRepository;
import com.academiabaile.back.repository.PrestamoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class PagoService {

    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private CuotaRepository cuotaRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Transactional
    public Cuota pagarCuotaConComprobante(Long cuotaId, MultipartFile comprobante) throws IOException {
        // 1. Validar y encontrar la cuota
        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuota no encontrada con id: " + cuotaId));

        // 2. Guardar el archivo del comprobante
        if (comprobante != null && !comprobante.isEmpty()) {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = "comprobante-" + cuota.getId() + "-" + comprobante.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(comprobante.getInputStream(), filePath);

            // Aquí podrías guardar la ruta del archivo (filePath.toString()) en la entidad Cuota si quieres
        }

        // 3. Marcar la cuota como pagada
        cuota.setPagada(true);
        Cuota cuotaPagada = cuotaRepository.save(cuota);

        // 4. Verificar si el préstamo está completamente pagado
        verificarYActualizarEstadoPrestamo(cuota.getPrestamo().getId());

        return cuotaPagada;
    }

    private void verificarYActualizarEstadoPrestamo(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId).get(); // El préstamo debe existir
        List<Cuota> cuotasNoPagadas = cuotaRepository.findByPrestamoAndPagada(prestamo, false);

        if (cuotasNoPagadas.isEmpty()) {
            prestamo.setPagado(true);
            prestamoRepository.save(prestamo);
        }
    }
}
