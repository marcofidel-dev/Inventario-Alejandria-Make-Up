package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;

public record VentaPorHoraDTO(
    int hora,
    int cantidadVentas,
    BigDecimal totalVendido
) {}
