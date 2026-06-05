package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;

public record MargenProductoDTO(
    Long productoId,
    String nombre,
    String codigoProducto,
    BigDecimal costo,
    BigDecimal precioVenta,
    BigDecimal margenPesos,
    BigDecimal margenPorcentaje
) {}
