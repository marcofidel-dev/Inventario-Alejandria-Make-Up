package com.marcofidel_dev.inventario.application.dto;

import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;
import com.marcofidel_dev.inventario.domain.entity.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Full sale detail for ADMIN: items with cost snapshots, profit totals, and seller info. */
public record SaleDetalleAdminDTO(
        Long id,
        LocalDateTime saleDate,
        BigDecimal subtotal,
        BigDecimal discountPercent,
        BigDecimal discountAmount,
        BigDecimal total,
        BigDecimal totalCost,
        BigDecimal totalProfit,
        PaymentMethod paymentMethod,
        SaleStatus status,
        String customerName,
        String sellerName,
        String voidReason,
        String notes,
        boolean virtualSale,
        List<SaleItemAdminViewDTO> items
) {}
