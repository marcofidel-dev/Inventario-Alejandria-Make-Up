package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;

public record StockPorCategoriaDTO(
    String categoria,
    int cantidadProductos,
    int cantidadUnidades,
    BigDecimal valorACosto
) {}
