package com.marcofidel_dev.inventario.application.analytics.dto;

public record AlertasResumenDTO(
    int stockCritico,
    int sinRotacion,
    int descuadresCaja
) {}
