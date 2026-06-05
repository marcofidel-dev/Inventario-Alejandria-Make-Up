package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;

public record DesgloseUsuarioDescuadreDTO(
    Long userId,
    String nombreUsuario,
    int cantidadDescuadres,
    BigDecimal totalFaltante,
    BigDecimal totalSobrante
) {}
