package com.marcofidel_dev.inventario.ui.common;

import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Static formatting helpers shared across all POS controllers. */
public final class FormatUtils {

    private FormatUtils() {}

    /** Colombian peso format: $75.000 (period as thousands separator, no decimals). */
    public static String money(BigDecimal amount) {
        if (amount == null) return "$0";
        if (amount.compareTo(BigDecimal.ZERO) < 0) return "-" + money(amount.negate());
        long value = amount.setScale(0, RoundingMode.HALF_UP).longValue();
        String digits = String.valueOf(value);
        StringBuilder sb = new StringBuilder();
        int rem = digits.length() % 3;
        if (rem > 0) sb.append(digits, 0, rem);
        for (int i = rem; i < digits.length(); i += 3) {
            if (sb.length() > 0) sb.append('.');
            sb.append(digits, i, i + 3);
        }
        return "$" + sb;
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
