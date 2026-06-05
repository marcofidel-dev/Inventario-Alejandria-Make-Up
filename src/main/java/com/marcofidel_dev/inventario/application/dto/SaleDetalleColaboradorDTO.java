package com.marcofidel_dev.inventario.application.dto;

import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;
import com.marcofidel_dev.inventario.domain.entity.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Full sale detail for COLABORADOR: items without cost or profit data. */
public record SaleDetalleColaboradorDTO(
        Long id,
        LocalDateTime saleDate,
        BigDecimal subtotal,
        BigDecimal discountPercent,
        BigDecimal discountAmount,
        BigDecimal total,
        PaymentMethod paymentMethod,
        SaleStatus status,
        String customerName,
        String notes,
        List<SaleItemViewDTO> items
) {}
