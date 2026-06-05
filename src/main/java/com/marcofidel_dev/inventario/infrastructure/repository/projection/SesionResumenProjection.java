package com.marcofidel_dev.inventario.infrastructure.repository.projection;

import java.math.BigDecimal;

public interface SesionResumenProjection {
    Long getId();
    Long getUserId();
    String getNombreUsuario();
    String getOpeningDate();
    String getClosingDate();
    BigDecimal getInitialCash();
    BigDecimal getExpectedCash();
    BigDecimal getDeclaredCash();
    BigDecimal getCashDifference();
    String getStatus();
}
