package com.academiabaile.back.service;

import com.academiabaile.back.entidades.Pago;
import com.academiabaile.back.entidades.Prestamo;
import com.academiabaile.back.repository.PrestamoRepository;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
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

            Table header = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
            header.setWidth(UnitValue.createPercentValue(100));

            Cell companyCell = new Cell();
            companyCell.add(new Paragraph("Prestamos Rodriguez").setBold().setFontSize(24));
            companyCell.add(new Paragraph("Av. Siempre Viva 123 - Springfield"));
            companyCell.setBorder(Border.NO_BORDER);
            header.addCell(companyCell);

            header.addCell(createInvoiceBox("FACTURA", pago.getId()));

            document.add(header);
            document.add(new Paragraph("\n"));

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

            Prestamo prestamo = prestamoRepository.findById(prestamoId).orElseThrow(() -> new IOException("Préstamo no encontrado"));
            Table header = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
            header.setWidth(UnitValue.createPercentValue(100));

            Cell clientCell = new Cell();
            clientCell.add(new Paragraph("Cliente:").setBold());
            clientCell.add(new Paragraph(prestamo.getCliente().getNombre()));
            // Eliminada la línea que intentaba acceder a un email inexistente.
            clientCell.setBorder(Border.NO_BORDER);
            header.addCell(clientCell);

            header.addCell(createInvoiceBox("ESTADO DE CUENTA", prestamoId));
            document.add(header);
            document.add(new Paragraph("\n"));

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

    private Cell createInvoiceBox(String docName, long number) throws IOException {
        Table box = new Table(UnitValue.createPercentArray(new float[]{1}));
        box.setWidth(UnitValue.createPercentValue(100));
        box.setBorder(new SolidBorder(ColorConstants.BLACK, 1));

        // CORRECTO: Usar la fuente correcta de iText 7
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        Paragraph rucParagraph = new Paragraph("RUC: " + RUC_EMPRESA)
                .setFont(font).setFontSize(18).setTextAlignment(TextAlignment.CENTER);
        Cell rucCell = new Cell().add(rucParagraph).setBorder(Border.NO_BORDER);
        box.addCell(rucCell);

        Paragraph docNameParagraph = new Paragraph(docName)
                .setFont(font).setFontSize(18).setTextAlignment(TextAlignment.CENTER);
        Cell docNameCell = new Cell().add(docNameParagraph).setBorder(Border.NO_BORDER);
        box.addCell(docNameCell);

        String formattedNumber = String.format("001-%07d", number);
        Paragraph numberParagraph = new Paragraph(formattedNumber)
                .setFont(font).setFontSize(14).setTextAlignment(TextAlignment.CENTER);
        Cell numberCell = new Cell().add(numberParagraph).setBorder(Border.NO_BORDER);
        box.addCell(numberCell);

        return new Cell().add(box).setBorder(Border.NO_BORDER).setPadding(0);
    }

    private Cell createCell(String content, TextAlignment alignment) {
        Cell cell = new Cell().add(new Paragraph(content));
        cell.setPadding(5);
        cell.setTextAlignment(alignment);
        cell.setBorder(Border.NO_BORDER);
        return cell;
    }

    private Cell createHeaderCell(String content) {
        return createCell(content, TextAlignment.CENTER).setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY);
    }
}
