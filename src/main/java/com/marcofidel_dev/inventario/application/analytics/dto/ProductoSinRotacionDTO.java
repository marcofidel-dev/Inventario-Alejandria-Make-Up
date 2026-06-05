package com.marcofidel_dev.inventario.application.analytics.dto;

import java.time.LocalDate;

public record ProductoSinRotacionDTO(
    Long productoId,
    String nombre,
    int stockActual,
    LocalDate ultimaVenta
) {}
