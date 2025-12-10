package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Cuota;
import com.academiabaile.back.entidades.Pago;
import com.academiabaile.back.entidades.Prestamo;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
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
        document.add(new Paragraph("Monto del Préstamo: " + prestamo.getMonto()));
        document.add(new Paragraph("Interés: " + prestamo.getInteres()));
        document.add(new Paragraph("Meses: " + prestamo.getMeses()));
        document.add(new Paragraph("Deuda Total Pendiente: " + deudaTotal));

        document.add(new Paragraph("\n--- Detalle de Cuotas ---"));
        for (Cuota cuota : cuotas) {
            String estado = cuota.isPagada() ? "Pagada" : "Pendiente";
            document.add(new Paragraph(
                "Cuota #" + cuota.getNumeroCuota() +
                " | Vence: " + new SimpleDateFormat("dd-MM-yyyy").format(cuota.getFechaPago()) +
                " | Monto: " + cuota.getMonto() +
                " | Estado: " + estado
            ));
        }

        document.close();
        return baos.toByteArray();
    }

    public byte[] generarComprobante(Pago pago) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("Comprobante de Pago"));
        document.add(new Paragraph("ID de Pago: " + pago.getId()));
        document.add(new Paragraph("Fecha: " + new SimpleDateFormat("dd-MM-yyyy HH:mm").format(pago.getFecha())));
        document.add(new Paragraph("Monto Pagado: " + pago.getMonto()));

        if (pago.getPrestamo() != null && pago.getPrestamo().getCliente() != null) {
            document.add(new Paragraph("Cliente: " + pago.getPrestamo().getCliente().getNombre() + " " + pago.getPrestamo().getCliente().getApellido()));
            document.add(new Paragraph("Préstamo ID: " + pago.getPrestamo().getId()));
        }

        document.close();
        return baos.toByteArray();
    }
}
