package com.marcofidel_dev.inventario.infrastructure.repository.projection;

import java.math.BigDecimal;

public interface TopClienteProjection {
    Long getClienteId();
    String getNombre();
    String getPhone();
    int getCantidadCompras();
    BigDecimal getTotalComprado();
    String getUltimaCompra();
}
