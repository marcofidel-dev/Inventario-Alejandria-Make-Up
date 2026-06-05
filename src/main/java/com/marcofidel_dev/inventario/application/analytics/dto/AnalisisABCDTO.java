package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnalisisABCDTO(
    List<ProductoABCDTO> categoriaA,
    List<ProductoABCDTO> categoriaB,
    List<ProductoABCDTO> categoriaC,
    BigDecimal totalIngresos
) {}
