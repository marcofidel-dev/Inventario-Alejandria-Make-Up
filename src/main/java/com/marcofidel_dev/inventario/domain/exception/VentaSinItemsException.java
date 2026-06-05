package com.marcofidel_dev.inventario.domain.exception;

public class VentaSinItemsException extends RuntimeException {
    public VentaSinItemsException() {
        super("La venta debe tener al menos un producto.");
    }
}
