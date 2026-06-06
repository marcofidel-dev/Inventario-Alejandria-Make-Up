package com.marcofidel_dev.inventario.shared.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Central utility for Colombian Peso (COP) amounts.
 * All monetary values in the app are stored internally as BigDecimal with scale=0.
 * Every money input/output must pass through this class.
 *
 * Format: $15.000 (period as thousands separator, no cents, $ prefix).
 * Percentages are NOT monetary amounts and are handled separately (BigDecimal with decimals).
 */
public final class MoneyCOP {

    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(0);

    private MoneyCOP() {}

    // ─── NORMALIZATION ────────────────────────────────────────────────────────

    /** Rounds any BigDecimal to scale=0 with HALF_UP. Returns ZERO for null. */
    public static BigDecimal normalize(BigDecimal value) {
        if (value == null) return ZERO;
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    public static BigDecimal of(long pesos) {
        return BigDecimal.valueOf(pesos).setScale(0);
    }

    public static BigDecimal of(int pesos) {
        return BigDecimal.valueOf(pesos).setScale(0);
    }

    // ─── OUTPUT FORMATTING ────────────────────────────────────────────────────

    /** "$15.000" — for UI labels, PDF, and display text. */
    public static String format(BigDecimal value) {
        BigDecimal norm = normalize(value);
        if (norm.signum() < 0) return "-" + format(norm.negate());
        String digits = String.valueOf(norm.longValue());
        StringBuilder sb = new StringBuilder();
        int rem = digits.length() % 3;
        if (rem > 0) sb.append(digits, 0, rem);
        for (int i = rem; i < digits.length(); i += 3) {
            if (!sb.isEmpty()) sb.append('.');
            sb.append(digits, i, i + 3);
        }
        return "$" + sb;
    }

    /** "15.000" — no $ prefix, for editable text fields after losing focus. */
    public static String formatPlain(BigDecimal value) {
        String formatted = format(value);
        return formatted.startsWith("-") ? "-" + formatted.substring(2) : formatted.substring(1);
    }

    // ─── INPUT PARSING ────────────────────────────────────────────────────────

    /**
     * Parses user-typed COP text into BigDecimal scale=0.
     * Accepts: "15000", "15.000", "$15.000", " $15.000 ", "15,000".
     * Returns ZERO for blank input.
     * Throws InvalidMoneyFormatException for letters, negative values, or invalid formats.
     */
    public static BigDecimal parse(String text) {
        if (text == null || text.isBlank()) return ZERO;

        String cleaned = text.trim()
                .replace("$", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", "");

        if (cleaned.isEmpty()) return ZERO;

        try {
            long pesos = Long.parseLong(cleaned);
            if (pesos < 0) {
                throw new InvalidMoneyFormatException(
                        "El monto no puede ser negativo: " + text);
            }
            return BigDecimal.valueOf(pesos).setScale(0);
        } catch (NumberFormatException e) {
            throw new InvalidMoneyFormatException(
                    "Formato de monto inválido: '" + text + "'. Use solo números.");
        }
    }

    // ─── SAFE ARITHMETIC ─────────────────────────────────────────────────────

    public static BigDecimal add(BigDecimal... values) {
        BigDecimal sum = ZERO;
        for (BigDecimal v : values) sum = sum.add(normalize(v));
        return sum;
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return normalize(a).subtract(normalize(b));
    }

    public static BigDecimal multiply(BigDecimal value, int quantity) {
        return normalize(normalize(value).multiply(BigDecimal.valueOf(quantity)));
    }

    /**
     * Applies a percentage to a base amount and rounds to whole pesos.
     * Example: applyPercentage(15000, 10) → 1500.
     * Note: percentage is NOT a monetary value — it keeps its own scale.
     */
    public static BigDecimal applyPercentage(BigDecimal base, BigDecimal percentage) {
        if (percentage == null) return normalize(base);
        BigDecimal factor = percentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return normalize(normalize(base).multiply(factor));
    }

    /**
     * Calculates margin percentage: ((price - cost) / price) * 100.
     * Returns result with 1 decimal (e.g., 46.7).
     * Returns ZERO if price is zero.
     */
    public static BigDecimal marginPercentage(BigDecimal price, BigDecimal cost) {
        BigDecimal p = normalize(price);
        if (p.signum() == 0) return ZERO;
        BigDecimal margin = p.subtract(normalize(cost));
        return margin.multiply(BigDecimal.valueOf(100))
                .divide(p, 1, RoundingMode.HALF_UP);
    }
}
