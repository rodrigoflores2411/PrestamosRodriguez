package com.academiabaile.back.service;

import com.academiabaile.back.model.Pago;
import com.academiabaile.back.model.Prestamo;
import com.academiabaile.back.repository.PrestamoRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.font.StandardFonts;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

@Service
public class PdfService {

    private static final String RUC_EMPRESA = "20123456789";

    @Autowired
    private PrestamoRepository prestamoRepository;

    public byte[] generarComprobante(Pago pago) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(out))) {
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            // --- CABECERA ---
            Table header = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
            header.setWidth(UnitValue.createPercentValue(100));

            // Datos de la empresa
            Cell companyCell = new Cell();
            companyCell.add(new Paragraph("Prestamos Rodriguez").setBold().setFontSize(24));
            companyCell.add(new Paragraph("Av. Siempre Viva 123 - Springfield"));
            companyCell.setBorder(Border.NO_BORDER);
            header.addCell(companyCell);

            // Recuadro normativo
            header.addCell(createInvoiceBox("FACTURA", pago.getId()));

            document.add(header);
            document.add(new Paragraph("\n"));

            // --- DATOS DEL CLIENTE ---
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Table clientDetails = new Table(UnitValue.createPercentArray(new float[]{25, 75}));
            clientDetails.setWidth(UnitValue.createPercentValue(100));
            clientDetails.addCell(createCell("Señor(es):", TextAlignment.LEFT).setBold());
            String clientName = (pago.getPrestamo() != null && pago.getPrestamo().getCliente() != null) ? pago.getPrestamo().getCliente().getNombre() : "Cliente no especificado";
            clientDetails.addCell(createCell(clientName, TextAlignment.LEFT));
            clientDetails.addCell(createCell("Fecha de emisión:", TextAlignment.LEFT).setBold());
            clientDetails.addCell(createCell(sdf.format(pago.getFecha()), TextAlignment.LEFT));
            document.add(clientDetails);
            document.add(new Paragraph("\n"));

            // --- TABLA DE ITEMS ---
            Table items = new Table(UnitValue.createPercentArray(new float[]{15, 60, 25}));
            items.setWidth(UnitValue.createPercentValue(100));
            items.addHeaderCell(createHeaderCell("CANTIDAD"));
            items.addHeaderCell(createHeaderCell("DESCRIPCIÓN"));
            items.addHeaderCell(createHeaderCell("VALOR"));

            DecimalFormat df = new DecimalFormat("#,##0.00");
            items.addCell(createCell("1", TextAlignment.CENTER));
            items.addCell(createCell("Pago de cuota de préstamo", TextAlignment.LEFT));
            items.addCell(createCell("USD " + df.format(pago.getMonto()), TextAlignment.RIGHT));
            document.add(items);
            document.add(new Paragraph("\n"));

            // --- TOTALES ---
            Table totals = new Table(UnitValue.createPercentArray(new float[]{80, 20}));
            totals.setWidth(UnitValue.createPercentValue(100));
            totals.addCell(createCell("TOTAL:", TextAlignment.RIGHT).setBold().setFontSize(14));
            totals.addCell(createCell("USD " + df.format(pago.getMonto()), TextAlignment.RIGHT).setBold().setFontSize(14));
            document.add(totals);

            document.add(new Paragraph("\n\n\n").setMarginBottom(20));
            document.add(new Paragraph("CANCELADO").setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER).setFontColor(ColorConstants.GREEN));

            document.close();
        }
        return out.toByteArray();
    }

    public byte[] generarEstadoDeCuentaPdf(Map<String, Object> datosDeuda) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DecimalFormat df = new DecimalFormat("#,##0.00");
        Long prestamoId = (Long) datosDeuda.get("prestamoId");

        try (PdfDocument pdf = new PdfDocument(new PdfWriter(out))) {
            Document document = new Document(pdf, PageSize.A4);
            document.setMargins(30, 30, 30, 30);

            // --- CABECERA ---
            Prestamo prestamo = prestamoRepository.findById(prestamoId).orElseThrow(() -> new IOException("Préstamo no encontrado"));
            Table header = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
            header.setWidth(UnitValue.createPercentValue(100));

            // Datos del cliente
            Cell clientCell = new Cell();
            clientCell.add(new Paragraph("Cliente:").setBold());
            clientCell.add(new Paragraph(prestamo.getCliente().getNombre()));
            clientCell.add(new Paragraph(prestamo.getCliente().getEmail()));
            clientCell.setBorder(Border.NO_BORDER);
            header.addCell(clientCell);

            // Recuadro normativo
            header.addCell(createInvoiceBox("ESTADO DE CUENTA", prestamoId));
            document.add(header);
            document.add(new Paragraph("\n"));

            // --- RESUMEN DEL PRÉSTAMO ---
            document.add(new Paragraph("Resumen del Préstamo").setBold().setFontSize(14));
            Table details = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
            details.setWidth(UnitValue.createPercentValue(100));
            details.addCell(createCell("Monto Original del Préstamo:", TextAlignment.LEFT));
            details.addCell(createCell("USD " + df.format(prestamo.getMonto()), TextAlignment.RIGHT));
            details.addCell(createCell("Fecha de Otorgamiento:", TextAlignment.LEFT));
            details.addCell(createCell(prestamo.getFecha().toString(), TextAlignment.RIGHT));
            details.addCell(createCell("Plazo Original (Meses):", TextAlignment.LEFT));
            details.addCell(createCell(String.valueOf(prestamo.getPlazo()), TextAlignment.RIGHT));
            details.addCell(createCell("Pagos Realizados:", TextAlignment.LEFT));
            details.addCell(createCell(String.valueOf(datosDeuda.get("pagosRealizados")), TextAlignment.RIGHT));
            document.add(details);
            document.add(new Paragraph("\n"));

            // --- DETALLE DE LA DEUDA ---
            document.add(new Paragraph("Detalle de Deuda al " + datosDeuda.get("fechaCalculo")).setBold().setFontSize(14));
            Table debtTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
            debtTable.setWidth(UnitValue.createPercentValue(100));
            debtTable.addCell(createCell("Cuota mensual estimada:", TextAlignment.LEFT));
            debtTable.addCell(createCell("USD " + df.format(datosDeuda.get("cuotaMensualEstimada")), TextAlignment.RIGHT));
            debtTable.addCell(createCell("Meses de mora reportados:", TextAlignment.LEFT));
            debtTable.addCell(createCell(String.valueOf(datosDeuda.get("mesesDeMora")), TextAlignment.RIGHT));
            debtTable.addCell(createCell("Monto de cuotas atrasadas:", TextAlignment.LEFT));
            debtTable.addCell(createCell("USD " + df.format(datosDeuda.get("montoBaseDeuda")), TextAlignment.RIGHT));
            debtTable.addCell(createCell("Interés por mora acumulado (1% compuesto):", TextAlignment.LEFT).setFontColor(ColorConstants.RED));
            debtTable.addCell(createCell("USD " + df.format(datosDeuda.get("interesPorMoraAcumulado")), TextAlignment.RIGHT).setFontColor(ColorConstants.RED));
            document.add(debtTable);

            // --- TOTAL A PAGAR ---
            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
            totalTable.setWidth(UnitValue.createPercentValue(100));
            totalTable.addCell(createCell("TOTAL DEUDA CALCULADA:", TextAlignment.RIGHT).setBold().setFontSize(16));
            Cell totalCell = createCell("USD " + df.format(datosDeuda.get("totalDeudaCalculada")), TextAlignment.RIGHT).setBold().setFontSize(16).setBackgroundColor(ColorConstants.YELLOW);
            totalTable.addCell(totalCell);
            document.add(totalTable);

            document.close();
        }
        return out.toByteArray();
    }

    /**
     * Crea el recuadro superior derecho que cumple con la normativa.
     */
    private Cell createInvoiceBox(String docName, long number) throws IOException {
        Table box = new Table(UnitValue.createPercentArray(new float[]{1}));
        box.setWidth(UnitValue.createPercentValue(100));
        box.setBorder(new SolidBorder(ColorConstants.BLACK, 1));
        
        box.addCell(createCell("RUC: " + RUC_EMPRESA, TextAlignment.CENTER).setBold().setFontSize(18).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)));
        box.addCell(createCell(docName, TextAlignment.CENTER).setBold().setFontSize(18).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)));
        
        String formattedNumber = String.format("001-%07d", number);
        box.addCell(createCell(formattedNumber, TextAlignment.CENTER).setBold().setFontSize(14).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)));
        
        return new Cell().add(box).setBorder(Border.NO_BORDER).setPadding(0);
    }

    /**
     * Helper para crear celdas de tabla simples y sin bordes.
     */
    private Cell createCell(String content, TextAlignment alignment) {
        Cell cell = new Cell().add(new Paragraph(content));
        cell.setPadding(5);
        cell.setTextAlignment(alignment);
        cell.setBorder(Border.NO_BORDER);
        return cell;
    }

    /**
     * Helper para crear celdas de cabecera para tablas.
     */
    private Cell createHeaderCell(String content) {
        return createCell(content, TextAlignment.CENTER).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY);
    }
}
