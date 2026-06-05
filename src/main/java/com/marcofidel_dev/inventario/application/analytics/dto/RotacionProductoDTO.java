package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;

public record RotacionProductoDTO(
    Long productoId,
    String nombre,
    int stockActual,
    int unidadesVendidas,
    BigDecimal rotacion
) {}
