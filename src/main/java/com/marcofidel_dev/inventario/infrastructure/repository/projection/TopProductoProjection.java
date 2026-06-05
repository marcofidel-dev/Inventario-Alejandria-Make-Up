package com.marcofidel_dev.inventario.infrastructure.repository.projection;

import java.math.BigDecimal;

public interface TopProductoProjection {
    Long getProductoId();
    String getCodigoProducto();
    String getNombreProducto();
    int getUnidadesVendidas();
    BigDecimal getIngresoTotal();
    BigDecimal getUtilidadTotal();
}
