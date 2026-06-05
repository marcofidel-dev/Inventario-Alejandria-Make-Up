package com.marcofidel_dev.inventario.domain.exception;

import java.math.BigDecimal;

public class DescuentoExcedidoException extends RuntimeException {
    public DescuentoExcedidoException(BigDecimal maxPercent) {
        super("El descuento supera el límite permitido para Colaborador (" + maxPercent.stripTrailingZeros().toPlainString() + "%).");
    }
}
