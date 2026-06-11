package com.marcofidel_dev.inventario.application.analytics;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.marcofidel_dev.inventario.application.analytics.dto.*;
import com.marcofidel_dev.inventario.application.service.DashboardService;
import com.marcofidel_dev.inventario.domain.entity.Role;
import com.marcofidel_dev.inventario.infrastructure.security.RequiresRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcofidel_dev.inventario.shared.money.MoneyCOP;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * iText Cell and POI Cell share the same simple name — all iText Cell usages
 * use the fully-qualified name; POI Cell is covered by the wildcard import.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExportServiceImpl implements ExportService {

    private final ReporteVentasService reporteVentasService;
    private final ReporteInventarioService reporteInventarioService;
    private final ReporteClientesService reporteClientesService;
    private final DashboardService dashboardService;

    @Value("${pos.comprobante.empresa.nombre:Alejandría Make-Up}")
    private String empresaNombre;

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_DT    = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─── PDF — Ventas ─────────────────────────────────────────────────────────

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public byte[] exportarReporteVentasPDF(FiltroReporteDTO filtro) {
        ReporteVentasPeriodoDTO reporte = reporteVentasService.generarReporte(filtro);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer)) {

            Document doc = new Document(pdfDoc, PageSize.A4);
            doc.setMargins(36, 36, 36, 36);
            PdfFont bold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            doc.add(new Paragraph(empresaNombre)
                    .setFont(bold).setFontSize(18).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Reporte de Ventas")
                    .setFont(normal).setFontSize(13).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Período: " + filtro.desde().format(FMT_FECHA) +
                    " al " + filtro.hasta().format(FMT_FECHA))
                    .setFont(normal).setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph(" "));

            Table kpiTable = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                    .setWidth(UnitValue.createPercentValue(100));
            addKpiCell(kpiTable, "Total Vendido",  cop(reporte.totalVendido()),    bold, normal);
            addKpiCell(kpiTable, "Ventas",         String.valueOf(reporte.cantidadVentas()), bold, normal);
            addKpiCell(kpiTable, "Utilidad",       cop(reporte.utilidadTotal()),   bold, normal);
            addKpiCell(kpiTable, "Margen",         pct(reporte.margenPorcentaje()), bold, normal);
            doc.add(kpiTable);
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Top Productos").setFont(bold).setFontSize(12));
            Table prodTable = new Table(UnitValue.createPercentArray(new float[]{40, 15, 22, 23}))
                    .setWidth(UnitValue.createPercentValue(100));
            addHeaderRow(prodTable, bold, "Producto", "Unidades", "Ingreso", "Utilidad");
            for (TopProductoDTO p : reporte.topProductos()) {
                prodTable.addCell(iCell(p.nombreProducto(), normal, 9));
                prodTable.addCell(iCell(String.valueOf(p.unidadesVendidas()), normal, 9));
                prodTable.addCell(iCell(cop(p.ingresoTotal()), normal, 9));
                prodTable.addCell(iCell(cop(p.utilidadTotal()), normal, 9));
            }
            doc.add(prodTable);
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Desglose por Método de Pago").setFont(bold).setFontSize(12));
            Table pagoTable = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20}))
                    .setWidth(UnitValue.createPercentValue(100));
            addHeaderRow(pagoTable, bold, "Método", "Ventas", "Total", "% del Total");
            for (DesglosePagoDTO d : reporte.desglosePorMetodo()) {
                pagoTable.addCell(iCell(d.metodoPago(), normal, 9));
                pagoTable.addCell(iCell(String.valueOf(d.cantidadVentas()), normal, 9));
                pagoTable.addCell(iCell(cop(d.total()), normal, 9));
                pagoTable.addCell(iCell(pct(d.porcentaje()), normal, 9));
            }
            doc.add(pagoTable);
            doc.close();
        } catch (Exception e) {
            log.error("Error generando PDF de ventas", e);
            throw new RuntimeException("No se pudo generar el PDF", e);
        }
        return baos.toByteArray();
    }

    // ─── Excel — Ventas ───────────────────────────────────────────────────────

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public byte[] exportarReporteVentasExcel(FiltroReporteDTO filtro) {
        ReporteVentasPeriodoDTO reporte = reporteVentasService.generarReporte(filtro);
        List<VentaResumenDTO> ventas    = reporteVentasService.listarVentasFiltradas(filtro);

        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle hdr   = buildHeaderStyle(wb);
            CellStyle money = buildMoneyStyle(wb);
            CellStyle pctSt = buildPctStyle(wb);

            Sheet resumen = wb.createSheet("Resumen");
            Row r0 = resumen.createRow(0);
            excelCell(r0, 0, "Métrica", hdr); excelCell(r0, 1, "Valor", hdr);
            excelMoneyRow(resumen, 1, "Total Vendido",     reporte.totalVendido(), money);
            excelStrRow(resumen,   2, "Cantidad Ventas",   String.valueOf(reporte.cantidadVentas()));
            excelMoneyRow(resumen, 3, "Utilidad Total",    reporte.utilidadTotal(), money);
            excelPctRow(resumen,   4, "Margen %",          reporte.margenPorcentaje(), pctSt);
            resumen.autoSizeColumn(0); resumen.autoSizeColumn(1);

            Sheet ventasSheet = wb.createSheet("Ventas");
            String[] vc = {"ID","Fecha","Cliente","Total","Método","Estado","Usuario","Items"};
            writeHeader(ventasSheet, vc, hdr);
            int vr = 1;
            for (VentaResumenDTO v : ventas) {
                Row row = ventasSheet.createRow(vr++);
                excelCell(row, 0, String.valueOf(v.id()));
                excelCell(row, 1, v.saleDate().format(FMT_DT));
                excelCell(row, 2, v.clienteNombre());
                excelCell(row, 3, v.total(), money);
                excelCell(row, 4, v.paymentMethod());
                excelCell(row, 5, v.status());
                excelCell(row, 6, v.usuarioNombre());
                excelCell(row, 7, String.valueOf(v.cantidadItems()));
            }
            for (int i = 0; i < 8; i++) ventasSheet.autoSizeColumn(i);

            Sheet prodSheet = wb.createSheet("Top Productos");
            String[] pc = {"ID","Código","Producto","Unidades","Ingreso","Utilidad","Margen %"};
            writeHeader(prodSheet, pc, hdr);
            int pr = 1;
            for (TopProductoDTO p : reporte.topProductos()) {
                Row row = prodSheet.createRow(pr++);
                excelCell(row, 0, String.valueOf(p.productoId()));
                excelCell(row, 1, p.codigoProducto() != null ? p.codigoProducto() : "");
                excelCell(row, 2, p.nombreProducto());
                excelCell(row, 3, String.valueOf(p.unidadesVendidas()));
                excelCell(row, 4, p.ingresoTotal(), money);
                excelCell(row, 5, p.utilidadTotal(), money);
                excelCell(row, 6, p.margenPorcentaje(), pctSt);
            }
            for (int i = 0; i < 7; i++) prodSheet.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generando Excel de ventas", e);
            throw new RuntimeException("No se pudo generar el Excel", e);
        }
    }

    // ─── PDF — Inventario ─────────────────────────────────────────────────────

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public byte[] exportarInventarioPDF(LocalDate desde, LocalDate hasta) {
        ValoracionInventarioDTO val        = reporteInventarioService.getValoracionActual();
        List<InventarioPorValorDTO> items  = reporteInventarioService.getInventarioPorValor();
        List<ProductoStockCriticoDTO> critico = dashboardService.getProductosStockCritico();
        List<ProductoSinRotacionDTO> sinRot   = dashboardService.getProductosSinRotacion(30);
        AnalisisABCDTO abc                    = reporteVentasService.getAnalisisABC(desde, hasta);
        List<MargenProductoDTO> margenes      = reporteInventarioService.getMargenes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer)) {

            Document doc = new Document(pdfDoc, PageSize.A4);
            doc.setMargins(36, 36, 36, 36);
            PdfFont bold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            doc.add(new Paragraph(empresaNombre).setFont(bold).setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Reporte de Inventario").setFont(normal).setFontSize(13)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph(" "));

            Table kpi = new Table(UnitValue.createPercentArray(new float[]{25,25,25,25}))
                    .setWidth(UnitValue.createPercentValue(100));
            addKpiCell(kpi, "Valor a Costo",     cop(val.valorACosto()),       bold, normal);
            addKpiCell(kpi, "Valor a Precio",    cop(val.valorAPrecioVenta()), bold, normal);
            addKpiCell(kpi, "Utilidad Potencial", cop(val.utilidadPotencial()), bold, normal);
            addKpiCell(kpi, "Margen Prom.",      pct(val.margenPromedio()),    bold, normal);
            doc.add(kpi);
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Inventario por Valor").setFont(bold).setFontSize(12));
            Table t = new Table(UnitValue.createPercentArray(new float[]{35,10,15,20,20}))
                    .setWidth(UnitValue.createPercentValue(100));
            addHeaderRow(t, bold, "Producto","Stock","Costo","Valor Costo","Valor Venta");
            for (InventarioPorValorDTO i : items) {
                t.addCell(iCell(i.nombre(), normal, 8));
                t.addCell(iCell(String.valueOf(i.stockActual()), normal, 8));
                t.addCell(iCell(cop(i.costo()), normal, 8));
                t.addCell(iCell(cop(i.valorACosto()), normal, 8));
                t.addCell(iCell(cop(i.valorAPrecioVenta()), normal, 8));
            }
            doc.add(t);

            // ── Stock Crítico ───────────────────────────────────────────────
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Stock Crítico").setFont(bold).setFontSize(12));
            if (critico.isEmpty()) {
                doc.add(new Paragraph("Todos los productos tienen stock suficiente.")
                        .setFont(normal).setFontSize(9));
            } else {
                Table tc = new Table(UnitValue.createPercentArray(new float[]{38, 14, 12, 12, 12}))
                        .setWidth(UnitValue.createPercentValue(100));
                addHeaderRow(tc, bold, "Producto", "Código", "Stock", "Mínimo", "Faltan");
                for (ProductoStockCriticoDTO p : critico) {
                    tc.addCell(iCell(p.nombre(), normal, 8));
                    tc.addCell(iCell(p.codigoProducto() != null ? p.codigoProducto() : "", normal, 8));
                    tc.addCell(iCell(String.valueOf(p.stockActual()), normal, 8));
                    tc.addCell(iCell(String.valueOf(p.stockMinimo()), normal, 8));
                    tc.addCell(iCell(String.valueOf(Math.abs(p.diferencia())), normal, 8));
                }
                doc.add(tc);
            }

            // ── Sin Rotación ────────────────────────────────────────────────
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Sin Rotación (30+ días sin ventas)").setFont(bold).setFontSize(12));
            if (sinRot.isEmpty()) {
                doc.add(new Paragraph("Todos los productos han tenido movimiento en 30 días.")
                        .setFont(normal).setFontSize(9));
            } else {
                Table ts = new Table(UnitValue.createPercentArray(new float[]{50, 20, 30}))
                        .setWidth(UnitValue.createPercentValue(100));
                addHeaderRow(ts, bold, "Producto", "Stock", "Última Venta");
                for (ProductoSinRotacionDTO p : sinRot) {
                    ts.addCell(iCell(p.nombre(), normal, 8));
                    ts.addCell(iCell(String.valueOf(p.stockActual()), normal, 8));
                    ts.addCell(iCell(p.ultimaVenta() != null
                            ? p.ultimaVenta().format(FMT_FECHA) : "Nunca", normal, 8));
                }
                doc.add(ts);
            }

            // ── Análisis ABC ────────────────────────────────────────────────
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Análisis ABC — " + desde.format(FMT_FECHA)
                    + " al " + hasta.format(FMT_FECHA)).setFont(bold).setFontSize(12));
            if (abc.categoriaA().isEmpty() && abc.categoriaB().isEmpty() && abc.categoriaC().isEmpty()) {
                doc.add(new Paragraph("Sin datos de ventas en el período seleccionado.")
                        .setFont(normal).setFontSize(9));
            } else {
                if (!abc.categoriaA().isEmpty()) {
                    doc.add(new Paragraph("Categoría A — 80% de ingresos").setFont(bold).setFontSize(10));
                    doc.add(buildAbcTable(abc.categoriaA(), bold, normal));
                }
                if (!abc.categoriaB().isEmpty()) {
                    doc.add(new Paragraph("Categoría B — 15% de ingresos").setFont(bold).setFontSize(10));
                    doc.add(buildAbcTable(abc.categoriaB(), bold, normal));
                }
                if (!abc.categoriaC().isEmpty()) {
                    doc.add(new Paragraph("Categoría C — 5% de ingresos").setFont(bold).setFontSize(10));
                    doc.add(buildAbcTable(abc.categoriaC(), bold, normal));
                }
            }

            // ── Márgenes ────────────────────────────────────────────────────
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Análisis de Márgenes").setFont(bold).setFontSize(12));
            Table tm = new Table(UnitValue.createPercentArray(new float[]{35, 12, 18, 18, 17}))
                    .setWidth(UnitValue.createPercentValue(100));
            addHeaderRow(tm, bold, "Producto", "Código", "Costo", "Precio", "Margen %");
            for (MargenProductoDTO m : margenes) {
                tm.addCell(iCell(m.nombre(), normal, 8));
                tm.addCell(iCell(m.codigoProducto() != null ? m.codigoProducto() : "", normal, 8));
                tm.addCell(iCell(cop(m.costo()), normal, 8));
                tm.addCell(iCell(cop(m.precioVenta()), normal, 8));
                tm.addCell(iCell(pct(m.margenPorcentaje()), normal, 8));
            }
            doc.add(tm);

            doc.close();
        } catch (Exception e) {
            log.error("Error generando PDF de inventario", e);
            throw new RuntimeException("No se pudo generar el PDF", e);
        }
        return baos.toByteArray();
    }

    // ─── Excel — Inventario ───────────────────────────────────────────────────

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public byte[] exportarInventarioExcel(LocalDate desde, LocalDate hasta) {
        List<InventarioPorValorDTO> items    = reporteInventarioService.getInventarioPorValor();
        List<MargenProductoDTO> margenes     = reporteInventarioService.getMargenes();
        List<ProductoStockCriticoDTO> critico = dashboardService.getProductosStockCritico();
        List<ProductoSinRotacionDTO> sinRot   = dashboardService.getProductosSinRotacion(30);
        AnalisisABCDTO abc                    = reporteVentasService.getAnalisisABC(desde, hasta);

        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle hdr   = buildHeaderStyle(wb);
            CellStyle money = buildMoneyStyle(wb);
            CellStyle pctSt = buildPctStyle(wb);

            Sheet inv = wb.createSheet("Inventario");
            writeHeader(inv, new String[]{"ID","Código","Producto","Stock","Costo","Precio","Valor Costo","Valor Venta"}, hdr);
            int r = 1;
            for (InventarioPorValorDTO i : items) {
                Row row = inv.createRow(r++);
                excelCell(row, 0, String.valueOf(i.productoId()));
                excelCell(row, 1, i.codigoProducto() != null ? i.codigoProducto() : "");
                excelCell(row, 2, i.nombre());
                excelCell(row, 3, String.valueOf(i.stockActual()));
                excelCell(row, 4, i.costo(), money);
                excelCell(row, 5, i.precioVenta(), money);
                excelCell(row, 6, i.valorACosto(), money);
                excelCell(row, 7, i.valorAPrecioVenta(), money);
            }
            for (int i = 0; i < 8; i++) inv.autoSizeColumn(i);

            Sheet mg = wb.createSheet("Márgenes");
            writeHeader(mg, new String[]{"ID","Código","Producto","Costo","Precio","Margen $","Margen %"}, hdr);
            int mr = 1;
            for (MargenProductoDTO m : margenes) {
                Row row = mg.createRow(mr++);
                excelCell(row, 0, String.valueOf(m.productoId()));
                excelCell(row, 1, m.codigoProducto() != null ? m.codigoProducto() : "");
                excelCell(row, 2, m.nombre());
                excelCell(row, 3, m.costo(), money);
                excelCell(row, 4, m.precioVenta(), money);
                excelCell(row, 5, m.margenPesos(), money);
                excelCell(row, 6, m.margenPorcentaje(), pctSt);
            }
            for (int i = 0; i < 7; i++) mg.autoSizeColumn(i);

            // Stock Crítico
            Sheet criticoSh = wb.createSheet("Stock Crítico");
            writeHeader(criticoSh, new String[]{"Producto", "Código", "Stock", "Mínimo", "Faltan"}, hdr);
            int cr = 1;
            for (ProductoStockCriticoDTO p : critico) {
                Row rowC = criticoSh.createRow(cr++);
                excelCell(rowC, 0, p.nombre());
                excelCell(rowC, 1, p.codigoProducto() != null ? p.codigoProducto() : "");
                excelCell(rowC, 2, String.valueOf(p.stockActual()));
                excelCell(rowC, 3, String.valueOf(p.stockMinimo()));
                excelCell(rowC, 4, String.valueOf(Math.abs(p.diferencia())));
            }
            for (int i = 0; i < 5; i++) criticoSh.autoSizeColumn(i);

            // Sin Rotación
            Sheet sinRotSh = wb.createSheet("Sin Rotación");
            writeHeader(sinRotSh, new String[]{"Producto", "Stock", "Última Venta"}, hdr);
            int sr = 1;
            for (ProductoSinRotacionDTO p : sinRot) {
                Row rowS = sinRotSh.createRow(sr++);
                excelCell(rowS, 0, p.nombre());
                excelCell(rowS, 1, String.valueOf(p.stockActual()));
                excelCell(rowS, 2, p.ultimaVenta() != null
                        ? p.ultimaVenta().format(FMT_FECHA) : "Nunca");
            }
            for (int i = 0; i < 3; i++) sinRotSh.autoSizeColumn(i);

            // Análisis ABC
            Sheet abcSh = wb.createSheet("Análisis ABC");
            writeHeader(abcSh, new String[]{"Categoría", "Producto", "Código", "Unidades", "Ingreso", "% Acum."}, hdr);
            int ar = 1;
            for (ProductoABCDTO p : abc.categoriaA()) {
                Row row = abcSh.createRow(ar++);
                excelCell(row, 0, "A"); excelCell(row, 1, p.nombre());
                excelCell(row, 2, p.codigoProducto() != null ? p.codigoProducto() : "");
                excelCell(row, 3, String.valueOf(p.unidadesVendidas()));
                excelCell(row, 4, p.ingreso(), money);
                excelCell(row, 5, p.porcentajeAcumulado(), pctSt);
            }
            for (ProductoABCDTO p : abc.categoriaB()) {
                Row row = abcSh.createRow(ar++);
                excelCell(row, 0, "B"); excelCell(row, 1, p.nombre());
                excelCell(row, 2, p.codigoProducto() != null ? p.codigoProducto() : "");
                excelCell(row, 3, String.valueOf(p.unidadesVendidas()));
                excelCell(row, 4, p.ingreso(), money);
                excelCell(row, 5, p.porcentajeAcumulado(), pctSt);
            }
            for (ProductoABCDTO p : abc.categoriaC()) {
                Row row = abcSh.createRow(ar++);
                excelCell(row, 0, "C"); excelCell(row, 1, p.nombre());
                excelCell(row, 2, p.codigoProducto() != null ? p.codigoProducto() : "");
                excelCell(row, 3, String.valueOf(p.unidadesVendidas()));
                excelCell(row, 4, p.ingreso(), money);
                excelCell(row, 5, p.porcentajeAcumulado(), pctSt);
            }
            for (int i = 0; i < 6; i++) abcSh.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generando Excel de inventario", e);
            throw new RuntimeException("No se pudo generar el Excel", e);
        }
    }

    // ─── Excel — Clientes ─────────────────────────────────────────────────────

    @Override
    @RequiresRole(Role.ADMIN)
    @Transactional(readOnly = true)
    public byte[] exportarClientesExcel(LocalDate desde, LocalDate hasta) {
        List<TopClienteDTO> top       = reporteClientesService.getTopClientes(desde, hasta, 100);
        List<ClienteInactivoDTO> inac = reporteClientesService.getClientesInactivos(90);

        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle hdr   = buildHeaderStyle(wb);
            CellStyle money = buildMoneyStyle(wb);

            Sheet topSh = wb.createSheet("Top Clientes");
            writeHeader(topSh, new String[]{"ID","Nombre","Teléfono","Compras","Total","Última Compra"}, hdr);
            int tr = 1;
            for (TopClienteDTO c : top) {
                Row row = topSh.createRow(tr++);
                excelCell(row, 0, String.valueOf(c.clienteId()));
                excelCell(row, 1, c.nombre());
                excelCell(row, 2, c.phone() != null ? c.phone() : "");
                excelCell(row, 3, String.valueOf(c.cantidadCompras()));
                excelCell(row, 4, c.totalComprado(), money);
                excelCell(row, 5, c.ultimaCompra() != null ? c.ultimaCompra().format(FMT_DT) : "");
            }
            for (int i = 0; i < 6; i++) topSh.autoSizeColumn(i);

            Sheet inacSh = wb.createSheet("Clientes Inactivos");
            writeHeader(inacSh, new String[]{"ID","Nombre","Teléfono","Última Compra","Días","Total Histórico"}, hdr);
            int ir = 1;
            for (ClienteInactivoDTO c : inac) {
                Row row = inacSh.createRow(ir++);
                excelCell(row, 0, String.valueOf(c.clienteId()));
                excelCell(row, 1, c.nombre());
                excelCell(row, 2, c.phone() != null ? c.phone() : "");
                excelCell(row, 3, c.ultimaCompra() != null ? c.ultimaCompra().format(FMT_FECHA) : "Nunca");
                excelCell(row, 4, c.diasSinComprar() == Long.MAX_VALUE ? "N/A" : String.valueOf(c.diasSinComprar()));
                excelCell(row, 5, c.totalHistorico(), money);
            }
            for (int i = 0; i < 6; i++) inacSh.autoSizeColumn(i);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generando Excel de clientes", e);
            throw new RuntimeException("No se pudo generar el Excel", e);
        }
    }

    // ─── POI helpers ─────────────────────────────────────────────────────────

    private CellStyle buildHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }

    private CellStyle buildMoneyStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
        return s;
    }

    private CellStyle buildPctStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setDataFormat(wb.createDataFormat().getFormat("0.00\"%\""));
        return s;
    }

    private void writeHeader(Sheet sheet, String[] cols, CellStyle style) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < cols.length; i++) excelCell(row, i, cols[i], style);
    }

    private void excelCell(Row row, int col, String value) {
        row.createCell(col).setCellValue(value != null ? value : "");
    }

    private void excelCell(Row row, int col, String value, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell c = row.createCell(col);
        c.setCellValue(value != null ? value : "");
        c.setCellStyle(style);
    }

    private void excelCell(Row row, int col, BigDecimal value, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell c = row.createCell(col, CellType.NUMERIC);
        c.setCellValue(value != null ? value.doubleValue() : 0.0);
        c.setCellStyle(style);
    }

    private void excelMoneyRow(Sheet sheet, int rowNum, String label, BigDecimal value, CellStyle money) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        excelCell(row, 1, value, money);
    }

    private void excelStrRow(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private void excelPctRow(Sheet sheet, int rowNum, String label, BigDecimal value, CellStyle pct) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        excelCell(row, 1, value, pct);
    }

    // ─── iText helpers — fully qualified Cell to avoid name clash with POI ────

    private void addKpiCell(Table table, String label, String value, PdfFont bold, PdfFont normal) {
        com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(label).setFont(normal).setFontSize(9)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph(value).setFont(bold).setFontSize(14)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                .setPadding(8);
        table.addCell(cell);
    }

    private void addHeaderRow(Table table, PdfFont bold, String... headers) {
        for (String h : headers) {
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(h).setFont(bold).setFontSize(9))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY));
        }
    }

    private com.itextpdf.layout.element.Cell iCell(String text, PdfFont font, float size) {
        return new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(text != null ? text : "").setFont(font).setFontSize(size));
    }

    private String cop(BigDecimal v) {
        return MoneyCOP.format(v);
    }

    private String pct(BigDecimal v) {
        return (v != null ? v.setScale(1, RoundingMode.HALF_UP).toPlainString() : "0.0") + "%";
    }

    private Table buildAbcTable(List<ProductoABCDTO> items, PdfFont bold, PdfFont normal) {
        Table t = new Table(UnitValue.createPercentArray(new float[]{42, 14, 25, 19}))
                .setWidth(UnitValue.createPercentValue(100));
        addHeaderRow(t, bold, "Producto", "Unidades", "Ingreso", "% Acum.");
        for (ProductoABCDTO p : items) {
            t.addCell(iCell(p.nombre(), normal, 8));
            t.addCell(iCell(String.valueOf(p.unidadesVendidas()), normal, 8));
            t.addCell(iCell(cop(p.ingreso()), normal, 8));
            t.addCell(iCell(pct(p.porcentajeAcumulado()), normal, 8));
        }
        return t;
    }
}
