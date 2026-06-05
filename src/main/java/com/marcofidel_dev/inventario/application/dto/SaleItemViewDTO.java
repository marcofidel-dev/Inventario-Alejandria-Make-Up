package com.marcofidel_dev.inventario.application.dto;

import java.math.BigDecimal;

/** Item detail visible for COLABORADOR — no cost, no profit margin. */
public record SaleItemViewDTO(
        String productName,
        String productCode,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
