package com.marcofidel_dev.inventario.ui.common;

import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;
import com.marcofidel_dev.inventario.shared.money.MoneyCOP;

import java.math.BigDecimal;

/** Static formatting helpers shared across all POS controllers. */
public final class FormatUtils {

    private FormatUtils() {}

    /** Colombian peso format: $15.000 (period as thousands separator, no decimals). */
    public static String money(BigDecimal amount) {
        return MoneyCOP.format(amount);
    }

    public static String metodoPago(PaymentMethod method) {
        if (method == null) return "";
        return switch (method) {
            case EFECTIVO      -> "Efectivo";
            case TARJETA       -> "Tarjeta";
            case TRANSFERENCIA -> "Transferencia";
            case NEQUI         -> "Nequi";
            case DAVIPLATA     -> "Daviplata";
            case MIXTO         -> "Pago Mixto";
        };
    }
}
