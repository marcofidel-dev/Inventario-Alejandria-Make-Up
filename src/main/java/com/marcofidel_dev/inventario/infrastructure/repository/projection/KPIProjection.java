package com.marcofidel_dev.inventario.infrastructure.repository.projection;

import java.math.BigDecimal;

public interface KPIProjection {
    BigDecimal getTotalVendido();
    int getCantidadVentas();
    BigDecimal getUtilidadBruta();
    BigDecimal getTicketPromedio();
    int getCantidadProductosVendidos();
}
