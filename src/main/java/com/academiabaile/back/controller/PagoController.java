package com.academiabaile.back.controller;

import com.academiabaile.back.entidades.Pago;
import com.academiabaile.back.service.PagoService;
import com.academiabaile.back.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
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

    @GetMapping(value = "/{id}/comprobante", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> generarComprobante(@PathVariable Long id) {
        Optional<Pago> pagoOpt = pagoService.obtenerPagoPorId(id);

        if (pagoOpt.isPresent()) {
            ByteArrayInputStream bis = pdfService.generarComprobante(pagoOpt.get());

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=comprobante-" + id + ".pdf");

            // La línea .contentType() se ha eliminado porque ya se especifica en @GetMapping
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(new InputStreamResource(bis));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
