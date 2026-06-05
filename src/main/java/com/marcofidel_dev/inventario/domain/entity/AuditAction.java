package com.marcofidel_dev.inventario.domain.entity;

public enum AuditAction {
    LOGIN,
    LOGOUT,
    CREATE,
    UPDATE,
    DELETE,
    PASSWORD_CHANGE,
    ROLE_CHANGE,
    USER_DEACTIVATED,
    SALE,
    VOID_SALE,
    OPEN_CASH_SESSION,
    CLOSE_CASH_SESSION
}
