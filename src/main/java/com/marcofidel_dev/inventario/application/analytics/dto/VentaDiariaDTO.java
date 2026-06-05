package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VentaDiariaDTO(
    LocalDate fecha,
    BigDecimal totalVendido,
    BigDecimal utilidad,
    int cantidadVentas
) {}
