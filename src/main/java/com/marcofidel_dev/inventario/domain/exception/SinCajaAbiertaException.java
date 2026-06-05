package com.marcofidel_dev.inventario.domain.exception;

public class SinCajaAbiertaException extends RuntimeException {
    public SinCajaAbiertaException() {
        super("No hay una sesión de caja abierta. Debes abrir caja antes de registrar ventas.");
    }
}
