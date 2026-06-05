package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record SesionCajaDetalleDTO(
    SesionCajaResumenDTO sesion,
    List<VentaResumenDTO> ventas,
    BigDecimal totalVentas,
    int cantidadVentas
) {}
