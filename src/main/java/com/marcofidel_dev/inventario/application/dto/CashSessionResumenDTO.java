package com.marcofidel_dev.inventario.application.dto;

import com.marcofidel_dev.inventario.domain.entity.CashSessionStatus;
import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record CashSessionResumenDTO(
        Long sessionId,
        String userName,
        LocalDateTime openingDate,
        BigDecimal initialCash,
        BigDecimal totalSales,
        int saleCount,
        Map<PaymentMethod, BigDecimal> salesByMethod,
        BigDecimal expectedCash,
        CashSessionStatus status
) {}
