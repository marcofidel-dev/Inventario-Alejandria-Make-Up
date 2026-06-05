package com.marcofidel_dev.inventario.application.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ClienteDetalleAnalyticsDTO(
    Long clienteId,
    String nombre,
    String phone,
    String email,
    int totalCompras,
    BigDecimal totalGastado,
    BigDecimal ticketPromedio,
    LocalDateTime ultimaCompra,
    long diasDesdeUltimaCompra,
    List<TopProductoDTO> productosFavoritos
) {}
