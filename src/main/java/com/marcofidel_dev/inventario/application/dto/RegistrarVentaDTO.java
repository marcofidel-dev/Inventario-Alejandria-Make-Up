package com.marcofidel_dev.inventario.application.dto;

import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

public record RegistrarVentaDTO(
        List<SaleItemInputDTO> items,
        Long customerId,
        PaymentMethod paymentMethod,
        BigDecimal discountPercent,
        String notes
) {}
