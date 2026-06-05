package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record DescuadresResumenDTO(
    int cantidadSesiones,
    int cantidadDescuadres,
    BigDecimal totalFaltante,
    BigDecimal totalSobrante,
    List<DesgloseUsuarioDescuadreDTO> porUsuario
) {}
