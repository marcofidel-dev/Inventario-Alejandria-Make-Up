package com.marcofidel_dev.inventario.infrastructure.repository.projection;

import java.math.BigDecimal;

public interface DescuadreProjection {
    Long getUserId();
    String getNombreUsuario();
    int getCantidadDescuadres();
    BigDecimal getTotalFaltante();
    BigDecimal getTotalSobrante();
}
