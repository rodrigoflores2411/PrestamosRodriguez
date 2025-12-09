package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Pago;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

@Service
public class PdfService {

    public ByteArrayInputStream generarComprobante(Pago pago) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(20, 20, 20, 20);

            // --- CABECERA ---
            Table header = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
            header.setWidth(UnitValue.createPercentValue(100));

            // Datos de la empresa
            Cell companyCell = new Cell();
            companyCell.add(new Paragraph("Prestamos Rodriguez").setBold().setFontSize(20));
            companyCell.add(new Paragraph("Av. Siempre Viva 123 - Springfield"));
            companyCell.add(new Paragraph("RUC: 20123456789"));
            companyCell.setBorder(Border.NO_BORDER);
            header.addCell(companyCell);

            // Factura Box
            Table invoiceBox = new Table(UnitValue.createPercentArray(new float[]{1}));
            invoiceBox.addCell(createCell("FACTURA", TextAlignment.CENTER).setFontSize(14).setBold());
            invoiceBox.addCell(createCell("001 - N° " + pago.getId(), TextAlignment.CENTER));
            Cell invoiceCell = new Cell().add(invoiceBox);
            invoiceCell.setBorder(Border.NO_BORDER);
            header.addCell(invoiceCell);

            document.add(header);
            document.add(new Paragraph("\n")); // Espacio

            // --- DATOS DEL CLIENTE ---
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Table clientDetails = new Table(UnitValue.createPercentArray(new float[]{25, 75}));
            clientDetails.setWidth(UnitValue.createPercentValue(100));
            clientDetails.addCell(createCell("Señor(es):", TextAlignment.LEFT).setBold());
            // Asumiendo que podemos acceder al nombre del cliente
            String clientName = (pago.getPrestamo() != null && pago.getPrestamo().getCliente() != null) ? pago.getPrestamo().getCliente().getNombre() : "Cliente no especificado";
            clientDetails.addCell(createCell(clientName, TextAlignment.LEFT));
            clientDetails.addCell(createCell("Fecha de emisión:", TextAlignment.LEFT).setBold());
            clientDetails.addCell(createCell(sdf.format(pago.getFecha()), TextAlignment.LEFT));
            document.add(clientDetails.setBorder(Border.NO_BORDER)); // Se aplica el borde a la tabla entera
            document.add(new Paragraph("\n"));

            // --- TABLA DE ITEMS ---
            Table items = new Table(UnitValue.createPercentArray(new float[]{15, 60, 25}));
            items.setWidth(UnitValue.createPercentValue(100));
            items.addHeaderCell(createCell("CANTIDAD", TextAlignment.CENTER).setBold());
            items.addHeaderCell(createCell("DESCRIPCIÓN", TextAlignment.CENTER).setBold());
            items.addHeaderCell(createCell("VALOR", TextAlignment.CENTER).setBold());

            // Item
            DecimalFormat df = new DecimalFormat("#.00");
            double total = pago.getMonto();
            double igv = total / 1.18 * 0.18;
            double subtotal = total - igv;

            items.addCell(createCell("1", TextAlignment.CENTER));
            items.addCell(createCell("Pago de cuota de préstamo", TextAlignment.LEFT));
            items.addCell(createCell("S/ " + df.format(total), TextAlignment.RIGHT));
            document.add(items);
            document.add(new Paragraph("\n"));

            // --- TOTALES ---
            Table totals = new Table(UnitValue.createPercentArray(new float[]{80, 20}));
            totals.setWidth(UnitValue.createPercentValue(100));
            totals.addCell(createCell("Subtotal:", TextAlignment.RIGHT).setBold());
            totals.addCell(createCell("S/ " + df.format(subtotal), TextAlignment.RIGHT));
            totals.addCell(createCell("I.G.V. (18%):", TextAlignment.RIGHT).setBold());
            totals.addCell(createCell("S/ " + df.format(igv), TextAlignment.RIGHT));
            totals.addCell(createCell("TOTAL:", TextAlignment.RIGHT).setBold().setFontSize(14));
            totals.addCell(createCell("S/ " + df.format(total), TextAlignment.RIGHT).setBold().setFontSize(14));
            document.add(totals.setBorder(Border.NO_BORDER));
            document.add(new Paragraph("\n"));

            // --- PIE DE PÁGINA ---
            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph("CANCELADO").setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER).setFontColor(ColorConstants.GREEN);
            document.add(footer);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // Helper para crear celdas sin borde y con padding
    private Cell createCell(String content, TextAlignment alignment) {
        Cell cell = new Cell().add(new Paragraph(content));
        cell.setPadding(5);
        cell.setTextAlignment(alignment);
        cell.setBorder(Border.NO_BORDER);
        return cell;
    }
}
