package com.marcofidel_dev.inventario.application.dto;

import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;
import com.marcofidel_dev.inventario.domain.entity.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Visible for both roles when the sale belongs to the current user. */
public record SaleResumenDTO(
        Long id,
        LocalDateTime saleDate,
        BigDecimal total,
        PaymentMethod paymentMethod,
        String customerName,
        int itemCount,
        SaleStatus status,
        boolean virtualSale
) {}
