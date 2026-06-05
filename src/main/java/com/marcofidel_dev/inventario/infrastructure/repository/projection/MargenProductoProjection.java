package com.marcofidel_dev.inventario.infrastructure.repository.projection;

import java.math.BigDecimal;

public interface MargenProductoProjection {
    Long getProductoId();
    String getNombre();
    String getCodigoProducto();
    BigDecimal getCosto();
    BigDecimal getPrecioVenta();
}
