package com.marcofidel_dev.inventario.application.analytics;

import com.marcofidel_dev.inventario.application.analytics.dto.FiltroReporteDTO;

import java.time.LocalDate;

public interface ExportService {

    byte[] exportarReporteVentasPDF(FiltroReporteDTO filtro);

    byte[] exportarReporteVentasExcel(FiltroReporteDTO filtro);

    byte[] exportarInventarioPDF();

    byte[] exportarInventarioExcel();

    byte[] exportarClientesExcel(LocalDate desde, LocalDate hasta);
}
