package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;

public record InventarioPorValorDTO(
    Long productoId,
    String nombre,
    String codigoProducto,
    int stockActual,
    BigDecimal costo,
    BigDecimal precioVenta,
    BigDecimal valorACosto,
    BigDecimal valorAPrecioVenta
) {}
