package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaResumenDTO(
    Long id,
    LocalDateTime saleDate,
    String clienteNombre,
    BigDecimal total,
    String paymentMethod,
    String status,
    String usuarioNombre,
    int cantidadItems
) {}
