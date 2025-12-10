package com.academiabaile.back.controller;

import com.academiabaile.back.entidades.Prestamo;
import com.academiabaile.back.service.CalculoDeudaService;
import com.academiabaile.back.service.PdfService;
import com.academiabaile.back.service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private CalculoDeudaService calculoDeudaService;

    @Autowired
    private PdfService pdfService;

    @PostMapping
    public Prestamo crearPrestamo(@RequestBody Prestamo prestamo) {
        return prestamoService.guardarPrestamo(prestamo);
    }

    @GetMapping
    public List<Prestamo> obtenerPrestamos() {
        return prestamoService.obtenerPrestamos();
    }

    @GetMapping("/{id}/estado-de-cuenta")
    public ResponseEntity<byte[]> generarEstadoDeCuenta(@PathVariable Long id) {
        try {
            // 1. Calcular todos los datos de la deuda usando el nuevo servicio.
            Map<String, Object> datosDeuda = calculoDeudaService.calcularDeudaTotal(id);

            // 2. Generar el PDF pasando los datos calculados.
            byte[] pdfContents = pdfService.generarEstadoDeCuentaPdf(datosDeuda);

            // 3. Preparar y devolver la respuesta HTTP con el archivo PDF.
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "estado_de_cuenta_prestamo_" + id + ".pdf";
            headers.setContentDispositionFormData(filename, filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfContents, headers, HttpStatus.OK);
        } catch (Exception e) {
            // Imprimir el error en la consola del servidor para depuración
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
