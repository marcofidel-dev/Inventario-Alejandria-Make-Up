package com.marcofidel_dev.inventario.infrastructure.initializer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time idempotent runner that rounds all existing monetary values in the DB
 * to whole pesos (scale=0). Runs at startup and skips if already executed.
 *
 * IMPORTANT: Make a backup before the first run in production:
 *   cp inventario.db inventario.db.backup-money-YYYYMMDD
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MoneyNormalizationRunner {

    private static final String FLAG_KEY = "money_normalization_v1";
    private final JdbcTemplate jdbc;

    @PostConstruct
    public void normalizeExistingData() {
        if (alreadyRan()) {
            log.debug("MoneyNormalizationRunner: already executed, skipping.");
            return;
        }

        log.info("MoneyNormalizationRunner: rounding all monetary values to whole pesos...");

        jdbc.execute("UPDATE producto SET costo = ROUND(costo, 0), precio_venta = ROUND(precio_venta, 0)");
        jdbc.execute("UPDATE compra SET total_costo = ROUND(total_costo, 0)");
        jdbc.execute("UPDATE compra_item SET costo_unitario = ROUND(costo_unitario, 0)");
        jdbc.execute("UPDATE sale SET subtotal = ROUND(subtotal, 0), discount_amount = ROUND(discount_amount, 0), total = ROUND(total, 0)");
        jdbc.execute("UPDATE sale_item SET unit_price = ROUND(unit_price, 0), unit_cost = ROUND(unit_cost, 0), subtotal = ROUND(subtotal, 0)");
        jdbc.execute("""
            UPDATE cash_session SET
                initial_cash       = ROUND(initial_cash, 0),
                declared_cash      = CASE WHEN declared_cash IS NOT NULL THEN ROUND(declared_cash, 0) ELSE NULL END,
                expected_cash      = CASE WHEN expected_cash IS NOT NULL THEN ROUND(expected_cash, 0) ELSE NULL END,
                cash_difference    = CASE WHEN cash_difference IS NOT NULL THEN ROUND(cash_difference, 0) ELSE NULL END
            """);

        markAsRan();
        log.info("MoneyNormalizationRunner: done.");
    }

    private boolean alreadyRan() {
        try {
            jdbc.execute("""
                CREATE TABLE IF NOT EXISTS _migration_flags (
                    flag_key TEXT PRIMARY KEY,
                    ran_at   TEXT NOT NULL
                )
                """);
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM _migration_flags WHERE flag_key = ?",
                    Integer.class, FLAG_KEY);
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("MoneyNormalizationRunner: could not check flag, running normalization anyway.", e);
            return false;
        }
    }

    private void markAsRan() {
        jdbc.update(
                "INSERT OR IGNORE INTO _migration_flags(flag_key, ran_at) VALUES (?, datetime('now'))",
                FLAG_KEY);
    }
}
