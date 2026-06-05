package com.marcofidel_dev.inventario.infrastructure.repository.projection;

import java.math.BigDecimal;

public interface DesglosePagoProjection {
    String getMetodoPago();
    BigDecimal getTotal();
    int getCantidadVentas();
}
