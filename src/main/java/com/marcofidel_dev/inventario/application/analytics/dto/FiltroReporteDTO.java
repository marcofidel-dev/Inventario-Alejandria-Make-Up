package com.marcofidel_dev.inventario.application.analytics.dto;

import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;

import java.time.LocalDate;

public record FiltroReporteDTO(
    LocalDate desde,
    LocalDate hasta,
    Long usuarioId,
    Long clienteId,
    Long productoId,
    PaymentMethod metodoPago,
    boolean incluirAnuladas
) {
    public static FiltroReporteDTO estesMes() {
        LocalDate hoy = LocalDate.now();
        return new FiltroReporteDTO(hoy.withDayOfMonth(1), hoy, null, null, null, null, false);
    }

    public static FiltroReporteDTO ultimos30Dias() {
        LocalDate hoy = LocalDate.now();
        return new FiltroReporteDTO(hoy.minusDays(29), hoy, null, null, null, null, false);
    }
}
