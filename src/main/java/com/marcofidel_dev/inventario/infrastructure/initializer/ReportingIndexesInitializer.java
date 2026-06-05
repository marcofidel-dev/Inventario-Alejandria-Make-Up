package com.marcofidel_dev.inventario.infrastructure.initializer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportingIndexesInitializer {

    private final DataSource dataSource;

    private static final String[] DDL = {
        "CREATE INDEX IF NOT EXISTS idx_sale_date ON sale(sale_date)",
        "CREATE INDEX IF NOT EXISTS idx_sale_status_date ON sale(status, sale_date)",
        "CREATE INDEX IF NOT EXISTS idx_sale_user_date ON sale(user_id, sale_date)",
        "CREATE INDEX IF NOT EXISTS idx_sale_customer ON sale(customer_id)",
        "CREATE INDEX IF NOT EXISTS idx_sale_item_producto ON sale_item(producto_id)",
        "CREATE INDEX IF NOT EXISTS idx_sale_item_sale ON sale_item(sale_id)",
        "CREATE INDEX IF NOT EXISTS idx_cash_session_user_status ON cash_session(user_id, status)"
    };

    @PostConstruct
    public void createIndexes() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : DDL) {
                stmt.execute(sql);
            }
            log.info("Reporting indexes ensured ({} indexes)", DDL.length);
        } catch (SQLException e) {
            log.warn("Could not create reporting indexes: {}", e.getMessage());
        }
    }
}
