package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;

public record DesgloseUsuarioDTO(
    Long userId,
    String nombreUsuario,
    int cantidadVentas,
    BigDecimal totalVendido,
    BigDecimal ticketPromedio
) {}
