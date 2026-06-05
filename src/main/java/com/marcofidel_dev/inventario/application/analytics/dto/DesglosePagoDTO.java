package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;

public record DesglosePagoDTO(
    String metodoPago,
    BigDecimal total,
    int cantidadVentas,
    BigDecimal porcentaje
) {}
