package com.marcofidel_dev.inventario.infrastructure.config;

import org.hibernate.community.dialect.SQLiteDialect;

/**
 * Dialecto personalizado de SQLite para mejorar la compatibilidad con Hibernate
 * SQLiteDialect ya incluye soporte correcto para AUTOINCREMENT
 */
public class SQLiteDialectCustom extends SQLiteDialect {


    /**
     * SQLite no soporta directamente el comando 'drop table cascade'
     * Este método previene errores al intentar eliminar tablas
     */
    @Override
    public boolean dropConstraints() {
        return false;
    }
}


