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

    /**
     * Procesa un pago manual subiendo un archivo de comprobante.
     */
    @Transactional
    public Cuota pagarCuotaConComprobante(Long cuotaId, MultipartFile comprobante) throws IOException {
        // Paso 1: Guardar el archivo del comprobante
        if (comprobante != null && !comprobante.isEmpty()) {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = "comprobante-" + cuotaId + "-" + comprobante.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(comprobante.getInputStream(), filePath);
        }

        // Paso 2: Marcar la cuota como pagada
        return marcarCuotaComoPagada(cuotaId);
    }

    /**
     * Procesa un pago online (como Mercado Pago) donde no hay archivo.
     */
    @Transactional
    public Cuota pagarCuotaOnline(Long cuotaId) {
        return marcarCuotaComoPagada(cuotaId);
    }

    /**
     * Lógica central para marcar una cuota como pagada y actualizar el estado del préstamo.
     */
    private Cuota marcarCuotaComoPagada(Long cuotaId) {
        // 1. Validar y encontrar la cuota
        Cuota cuota = cuotaRepository.findById(cuotaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuota no encontrada con id: " + cuotaId));

        // 2. Marcar la cuota como pagada
        cuota.setPagada(true);
        Cuota cuotaPagada = cuotaRepository.save(cuota);

        // 3. Verificar si el préstamo está completamente pagado
        verificarYActualizarEstadoPrestamo(cuota.getPrestamo());

        return cuotaPagada;
    }

    /**
     * Verifica todas las cuotas de un préstamo y lo marca como 'pagado' si corresponde.
     */
    private void verificarYActualizarEstadoPrestamo(Prestamo prestamo) {
        // Usamos un método de conteo del repositorio para más eficiencia
        long cuotasNoPagadas = cuotaRepository.countByPrestamoAndPagada(prestamo, false);

        if (cuotasNoPagadas == 0) {
            prestamo.setPagado(true);
            prestamoRepository.save(prestamo);
        }
    }
}
