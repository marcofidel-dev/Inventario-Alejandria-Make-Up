package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;

public record ProductoABCDTO(
    Long productoId,
    String nombre,
    String codigoProducto,
    int unidadesVendidas,
    BigDecimal ingreso,
    BigDecimal porcentajeAcumulado
) {}
