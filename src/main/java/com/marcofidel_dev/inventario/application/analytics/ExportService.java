package com.marcofidel_dev.inventario.application.analytics;

import com.marcofidel_dev.inventario.application.analytics.dto.FiltroReporteDTO;

import java.time.LocalDate;

public interface ExportService {

    byte[] exportarReporteVentasPDF(FiltroReporteDTO filtro);

    byte[] exportarReporteVentasExcel(FiltroReporteDTO filtro);

    byte[] exportarInventarioPDF(LocalDate desde, LocalDate hasta);

    byte[] exportarInventarioExcel(LocalDate desde, LocalDate hasta);

    byte[] exportarClientesExcel(LocalDate desde, LocalDate hasta);
}
