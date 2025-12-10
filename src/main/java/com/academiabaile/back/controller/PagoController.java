package com.academiabaile.back.controller;

import com.academiabaile.back.entidades.Pago;
import com.academiabaile.back.service.PagoService;
import com.academiabaile.back.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private PdfService pdfService;

    @PostMapping
    public Pago crearPago(@RequestBody Pago pago) {
        return pagoService.guardarPago(pago);
    }

    @GetMapping
    public List<Pago> obtenerPagos() {
        return pagoService.obtenerPagos();
    }

    @GetMapping(value = "/{id}/comprobante")
    public ResponseEntity<byte[]> generarComprobante(@PathVariable Long id) {
        Optional<Pago> pagoOpt = pagoService.obtenerPagoPorId(id);

        if (pagoOpt.isPresent()) {
            try {
                // 1. Generar el PDF, que ahora devuelve un byte[] directamente.
                byte[] pdfContents = pdfService.generarComprobante(pagoOpt.get());

                // 2. Preparar los headers para la respuesta PDF.
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                String filename = "comprobante_pago_" + id + ".pdf";
                headers.setContentDispositionFormData(filename, filename);
                headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

                // 3. Devolver la respuesta con el contenido del PDF.
                return new ResponseEntity<>(pdfContents, headers, HttpStatus.OK);
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
