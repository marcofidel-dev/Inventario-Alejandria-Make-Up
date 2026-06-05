package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;

public record KPIsRangoDTO(
    BigDecimal totalVendido,
    int cantidadVentas,
    BigDecimal utilidadBruta,
    BigDecimal ticketPromedio,
    BigDecimal margenPorcentaje,
    int cantidadProductosVendidos
) {}
