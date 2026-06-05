package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;

public record ValoracionInventarioDTO(
    BigDecimal valorACosto,
    BigDecimal valorAPrecioVenta,
    BigDecimal utilidadPotencial,
    BigDecimal margenPromedio,
    int totalProductos,
    int totalUnidades
) {}
