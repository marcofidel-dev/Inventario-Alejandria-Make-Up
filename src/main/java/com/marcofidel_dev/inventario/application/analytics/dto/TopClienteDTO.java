package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TopClienteDTO(
    Long clienteId,
    String nombre,
    String phone,
    int cantidadCompras,
    BigDecimal totalComprado,
    LocalDateTime ultimaCompra
) {}
