package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReporteVentasPeriodoDTO(
    LocalDate desde,
    LocalDate hasta,
    BigDecimal totalVendido,
    BigDecimal utilidadTotal,
    BigDecimal margenPorcentaje,
    int cantidadVentas,
    BigDecimal ticketPromedio,
    List<VentaDiariaDTO> ventasDiarias,
    List<TopProductoDTO> topProductos,
    List<DesglosePagoDTO> desglosePorMetodo,
    List<DesgloseUsuarioDTO> desglosePorUsuario
) {}
