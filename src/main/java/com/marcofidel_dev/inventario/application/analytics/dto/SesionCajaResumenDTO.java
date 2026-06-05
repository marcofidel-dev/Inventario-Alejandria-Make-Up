package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SesionCajaResumenDTO(
    Long id,
    Long userId,
    String nombreUsuario,
    LocalDateTime openingDate,
    LocalDateTime closingDate,
    BigDecimal initialCash,
    BigDecimal expectedCash,
    BigDecimal declaredCash,
    BigDecimal cashDifference,
    String status
) {}
