package com.marcofidel_dev.inventario.application.analytics.dto;

public record ProductoStockCriticoDTO(
    Long productoId,
    String nombre,
    String codigoProducto,
    int stockActual,
    int stockMinimo,
    int diferencia
) {}
