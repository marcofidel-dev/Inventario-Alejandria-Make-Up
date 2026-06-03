package com.marcofidel_dev.inventario.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Configuración de la base de datos SQLite
 * Optimiza el rendimiento y habilita características específicas de SQLite
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.marcofidel_dev.inventario.infrastructure.repository")
@EnableTransactionManagement
public class DatabaseConfig {

    private final DataSource dataSource;

    public DatabaseConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Configuración inicial de SQLite al arrancar la aplicación
     * Habilita claves foráneas y modo WAL para mejor concurrencia
     */
    @PostConstruct
    public void configureSQLite() {
        try (var connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // Habilitar claves foráneas (SQLite las tiene deshabilitadas por defecto)
            statement.execute("PRAGMA foreign_keys = ON");

            // Habilitar modo WAL (Write-Ahead Logging) para mejor concurrencia
            statement.execute("PRAGMA journal_mode = WAL");

            // Optimizaciones de rendimiento
            statement.execute("PRAGMA synchronous = NORMAL");
            statement.execute("PRAGMA temp_store = MEMORY");
            statement.execute("PRAGMA mmap_size = 30000000000");

            System.out.println("SQLite configurado correctamente con optimizaciones de rendimiento");

        } catch (SQLException e) {
            System.err.println("Error al configurar SQLite: " + e.getMessage());
            // No lanzamos excepción para no impedir el inicio de la aplicación
        }
    }
}

