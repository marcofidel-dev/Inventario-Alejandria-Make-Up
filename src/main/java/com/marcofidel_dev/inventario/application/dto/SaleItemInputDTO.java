package com.marcofidel_dev.inventario.application.dto;

import java.math.BigDecimal;

/**
 * @param unitPrice null = use product's current price; non-null = manual price override
 */
public record SaleItemInputDTO(
        Long productoId,
        int quantity,
        BigDecimal unitPrice
) {}
