package com.marcofidel_dev.inventario.application.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
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
import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;
import com.marcofidel_dev.inventario.domain.entity.Sale;
import com.marcofidel_dev.inventario.domain.entity.SaleItem;
import com.marcofidel_dev.inventario.infrastructure.repository.SaleRepository;
import com.marcofidel_dev.inventario.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComprobanteService {

    private final SaleRepository saleRepository;
    private final UserRepository userRepository;

    @Value("${pos.comprobante.empresa.nombre:Alejandría Make-Up}")
    private String empresaNombre;

    @Value("${pos.comprobante.empresa.direccion:Villavicencio, Meta}")
    private String empresaDireccion;

    @Value("${pos.comprobante.empresa.telefono:300 000 0000}")
    private String empresaTelefono;

    private static final float FONT_SIZE        = 7.5f;
    private static final float FONT_SIZE_TITLE  = 9.5f;
    private static final float FONT_SIZE_TOTAL  = 8.5f;
    private static final float WIDTH_MM         = 80f;
    private static final float HEIGHT_MM        = 220f;
    private static final String SEP = "--------------------------------";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy  hh:mm a");

    /**
     * Generates a thermal-80mm PDF receipt for the given sale.
     * @param ventaId the sale id
     * @param efectivoRecibido cash tendered (null for non-cash or unknown)
     */
    @Transactional(readOnly = true)
    public byte[] generarPDF(Long ventaId, BigDecimal efectivoRecibido) {
        Sale sale = saleRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada: " + ventaId));
        return buildPDF(sale, efectivoRecibido);
    }

    private byte[] buildPDF(Sale sale, BigDecimal efectivoRecibido) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        float pageW = mmToPoints(WIDTH_MM);
        float pageH = mmToPoints(HEIGHT_MM);
        float margin = 8f;
        float contentW = pageW - margin * 2;

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer)) {

            Document doc = new Document(pdfDoc, new PageSize(pageW, pageH));
            doc.setMargins(margin, margin, margin, margin);

            PdfFont mono     = PdfFontFactory.createFont(StandardFonts.COURIER);
            PdfFont monoBold = PdfFontFactory.createFont(StandardFonts.COURIER_BOLD);

            // ── Header ────────────────────────────────────────────────
            doc.add(line(empresaNombre,    monoBold, FONT_SIZE_TITLE, TextAlignment.CENTER));
            doc.add(line(empresaDireccion, mono,     FONT_SIZE,       TextAlignment.CENTER));
            doc.add(line("Tel: " + empresaTelefono, mono, FONT_SIZE,  TextAlignment.CENTER));
            doc.add(sep(mono));

            // ── Receipt metadata ──────────────────────────────────────
            doc.add(line("Comprobante de Venta",                mono, FONT_SIZE, TextAlignment.CENTER));
            doc.add(line("Nº " + String.format("%05d", sale.getId()), mono, FONT_SIZE, TextAlignment.CENTER));
            doc.add(line(sale.getSaleDate().format(DATE_FMT),   mono, FONT_SIZE, TextAlignment.CENTER));

            String sellerName = userRepository.findById(sale.getUserId())
                    .map(u -> u.getFullName() != null ? u.getFullName() : u.getUsername())
                    .orElse("Colaborador");
            doc.add(line("Atendiо: " + sellerName, mono, FONT_SIZE, TextAlignment.LEFT));

            String clientName = (sale.getCustomer() != null) ? sale.getCustomer().getName() : "Ocasional";
            doc.add(line("Cliente: " + clientName, mono, FONT_SIZE, TextAlignment.LEFT));
            doc.add(sep(mono));

            // ── Items ─────────────────────────────────────────────────
            float[] cols2 = {contentW * 0.60f, contentW * 0.40f};
            for (SaleItem item : sale.getItems()) {
                String desc = item.getQuantity() + " x " + truncate(item.getProducto().getNombre(), 21);
                String prices = formatMoney(item.getUnitPrice()) + "  " + formatMoney(item.getSubtotal());
                doc.add(twoCol(cols2,
                        cell(desc,   mono, FONT_SIZE, TextAlignment.LEFT),
                        cell(prices, mono, FONT_SIZE, TextAlignment.RIGHT)));
            }
            doc.add(sep(mono));

            // ── Totals ────────────────────────────────────────────────
            doc.add(twoCol(cols2,
                    cell("Subtotal:", mono, FONT_SIZE, TextAlignment.LEFT),
                    cell(formatMoney(sale.getSubtotal()), mono, FONT_SIZE, TextAlignment.RIGHT)));

            if (sale.getDiscountAmount() != null
                    && sale.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                doc.add(twoCol(cols2,
                        cell("Descuento (" + sale.getDiscountPercent().stripTrailingZeros().toPlainString() + "%):",
                                mono, FONT_SIZE, TextAlignment.LEFT),
                        cell("-" + formatMoney(sale.getDiscountAmount()), mono, FONT_SIZE, TextAlignment.RIGHT)));
            }

            doc.add(twoCol(cols2,
                    cell("TOTAL:", monoBold, FONT_SIZE_TOTAL, TextAlignment.LEFT),
                    cell(formatMoney(sale.getTotal()), monoBold, FONT_SIZE_TOTAL, TextAlignment.RIGHT)));

            // ── Payment ───────────────────────────────────────────────
            doc.add(spacer());
            doc.add(line("Pago: " + formatMetodoPago(sale.getPaymentMethod()), mono, FONT_SIZE, TextAlignment.LEFT));

            if (sale.getPaymentMethod() == PaymentMethod.EFECTIVO && efectivoRecibido != null) {
                BigDecimal cambio = efectivoRecibido.subtract(sale.getTotal()).setScale(2, RoundingMode.HALF_UP);
                doc.add(twoCol(cols2,
                        cell("Recibido:", mono, FONT_SIZE, TextAlignment.LEFT),
                        cell(formatMoney(efectivoRecibido), mono, FONT_SIZE, TextAlignment.RIGHT)));
                if (cambio.compareTo(BigDecimal.ZERO) >= 0) {
                    doc.add(twoCol(cols2,
                            cell("Cambio:", mono, FONT_SIZE, TextAlignment.LEFT),
                            cell(formatMoney(cambio), mono, FONT_SIZE, TextAlignment.RIGHT)));
                }
            }

            // ── Footer ────────────────────────────────────────────────
            doc.add(sep(mono));
            doc.add(line("¡Gracias por tu compra!", mono, FONT_SIZE, TextAlignment.CENTER));
            doc.add(line("Siguenos en @alejandria",  mono, FONT_SIZE, TextAlignment.CENTER));

        } catch (Exception e) {
            log.error("Error al generar PDF del comprobante para venta {}: {}", sale.getId(), e.getMessage(), e);
            throw new RuntimeException("No se pudo generar el comprobante PDF", e);
        }

        return baos.toByteArray();
    }

    // ── Layout helpers ───────────────────────────────────────────────

    private Paragraph line(String text, PdfFont font, float size, TextAlignment align) {
        return new Paragraph(text)
                .setFont(font).setFontSize(size)
                .setTextAlignment(align)
                .setMarginTop(1).setMarginBottom(0);
    }

    private Paragraph sep(PdfFont font) {
        return line(SEP, font, FONT_SIZE, TextAlignment.CENTER);
    }

    private Paragraph spacer() {
        return new Paragraph(" ").setFontSize(3).setMarginBottom(0).setMarginTop(0);
    }

    private Table twoCol(float[] widths, Cell left, Cell right) {
        return new Table(UnitValue.createPointArray(widths))
                .setWidth(widths[0] + widths[1])
                .setMargin(0).setPadding(0)
                .addCell(left).addCell(right);
    }

    private Cell cell(String text, PdfFont font, float size, TextAlignment align) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(size)
                        .setTextAlignment(align).setMarginBottom(0).setMarginTop(0))
                .setBorder(Border.NO_BORDER)
                .setPadding(0).setPaddingTop(1);
    }

    // ── Formatting helpers ───────────────────────────────────────────

    /** Colombian peso format: $75.000 (period as thousands separator, no decimals). */
    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "$0";
        long value = amount.setScale(0, RoundingMode.HALF_UP).longValue();
        if (value < 0) return "-" + formatMoney(amount.negate());
        String digits = String.valueOf(value);
        StringBuilder sb = new StringBuilder();
        int rem = digits.length() % 3;
        if (rem > 0) sb.append(digits, 0, rem);
        for (int i = rem; i < digits.length(); i += 3) {
            if (sb.length() > 0) sb.append('.');
            sb.append(digits, i, i + 3);
        }
        return "$" + sb;
    }

    private String formatMetodoPago(PaymentMethod method) {
        return switch (method) {
            case EFECTIVO      -> "Efectivo";
            case TARJETA       -> "Tarjeta";
            case TRANSFERENCIA -> "Transferencia";
            case NEQUI         -> "Nequi";
            case DAVIPLATA     -> "Daviplata";
            case MIXTO         -> "Pago Mixto";
        };
    }

    private String truncate(String s, int max) {
        return (s != null && s.length() > max) ? s.substring(0, max - 1) + "…" : s;
    }

    private float mmToPoints(float mm) {
        return mm * 72f / 25.4f;
    }
}
