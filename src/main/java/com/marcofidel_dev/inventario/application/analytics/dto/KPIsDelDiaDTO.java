package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;

public record KPIsDelDiaDTO(
    BigDecimal totalVendido,
    int cantidadVentas,
    BigDecimal utilidadBruta,
    BigDecimal ticketPromedio,
    BigDecimal porcentajeVsAyer,
    BigDecimal porcentajeVsMismoDiaSemanaPasada,
    int cantidadProductosVendidos,
    String mejorVendedor
) {}
