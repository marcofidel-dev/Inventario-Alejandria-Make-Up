package com.marcofidel_dev.inventario.infrastructure.repository.projection;

import java.math.BigDecimal;

public interface DesgloseUsuarioProjection {
    Long getUserId();
    String getNombreUsuario();
    int getCantidadVentas();
    BigDecimal getTotalVendido();
    BigDecimal getTicketPromedio();
}
