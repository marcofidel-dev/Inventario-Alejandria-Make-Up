package com.marcofidel_dev.inventario.domain.exception;

public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(String productName, int available) {
        super("Stock insuficiente para " + productName + " (disponible: " + available + ")");
    }
}
