package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;

public record TopProductoDTO(
    Long productoId,
    String codigoProducto,
    String nombreProducto,
    int unidadesVendidas,
    BigDecimal ingresoTotal,
    BigDecimal utilidadTotal,
    BigDecimal margenPorcentaje
) {}
