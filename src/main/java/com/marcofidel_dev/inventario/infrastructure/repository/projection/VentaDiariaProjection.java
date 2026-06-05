package com.marcofidel_dev.inventario.infrastructure.repository.projection;

import java.math.BigDecimal;

public interface VentaDiariaProjection {
    String getFecha();
    BigDecimal getTotalVendido();
    BigDecimal getUtilidad();
    int getCantidadVentas();
}
