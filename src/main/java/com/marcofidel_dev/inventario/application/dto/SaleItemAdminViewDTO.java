package com.marcofidel_dev.inventario.application.dto;

import java.math.BigDecimal;

/** Item detail visible for ADMIN only — includes cost snapshot and profit calculation. */
public record SaleItemAdminViewDTO(
        String productName,
        String productCode,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal unitCost,
        BigDecimal subtotal,
        BigDecimal profit
) {}
