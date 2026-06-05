package com.marcofidel_dev.inventario.infrastructure.security;

/**
 * Thrown when the current session lacks the required role to perform an operation.
 * Intentionally NOT related to Spring Security — this is a desktop-app-only guard.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
