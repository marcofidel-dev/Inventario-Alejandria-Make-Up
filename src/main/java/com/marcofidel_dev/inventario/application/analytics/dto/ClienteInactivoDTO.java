package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ClienteInactivoDTO(
    Long clienteId,
    String nombre,
    String phone,
    LocalDate ultimaCompra,
    BigDecimal totalHistorico,
    long diasSinComprar
) {}
