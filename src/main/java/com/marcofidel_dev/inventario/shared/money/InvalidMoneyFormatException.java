package com.marcofidel_dev.inventario.shared.money;

public class InvalidMoneyFormatException extends RuntimeException {
    public InvalidMoneyFormatException(String message) {
        super(message);
    }
}
