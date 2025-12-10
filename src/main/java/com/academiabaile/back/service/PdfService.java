package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Cuota;
import com.academiabaile.back.entidades.Prestamo;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class PdfService {

    public byte[] generarEstadoDeCuentaPdf(Map<String, Object> datosDeuda) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        Prestamo prestamo = (Prestamo) datosDeuda.get("prestamo");
        double deudaTotal = (double) datosDeuda.get("deudaTotal");
        List<Cuota> cuotas = (List<Cuota>) datosDeuda.get("cuotas");

        document.add(new Paragraph("Estado de Cuenta"));
        document.add(new Paragraph("Cliente: " + prestamo.getCliente().getNombre()));
        document.add(new Paragraph("Descripción: " + prestamo.getDescripcion()));
        document.add(new Paragraph("Monto: " + prestamo.getMonto()));
        document.add(new Paragraph("Interés: " + prestamo.getInteres()));
        document.add(new Paragraph("Meses: " + prestamo.getMeses()));
        document.add(new Paragraph("Deuda Total: " + deudaTotal));

        document.add(new Paragraph("\nCuotas:"));
        for (Cuota cuota : cuotas) {
            document.add(new Paragraph("Número de Cuota: " + cuota.getNumeroCuota()));
            document.add(new Paragraph("Fecha de Pago: " + cuota.getFechaPago()));
            document.add(new Paragraph("Monto: " + cuota.getMonto()));
            document.add(new Paragraph("Pagada: " + (cuota.isPagada() ? "Sí" : "No")));
            document.add(new Paragraph("\n"));
        }

        document.close();
        return baos.toByteArray();
    }
}
