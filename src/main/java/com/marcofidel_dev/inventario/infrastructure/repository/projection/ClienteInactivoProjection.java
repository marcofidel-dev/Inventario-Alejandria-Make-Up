package com.marcofidel_dev.inventario.infrastructure.repository.projection;

import java.math.BigDecimal;

public interface ClienteInactivoProjection {
    Long getClienteId();
    String getNombre();
    String getPhone();
    String getUltimaCompra();
    BigDecimal getTotalHistorico();
}
