package com.marcofidel_dev.inventario.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class BackupService {

    private static final String DB_FILE = "inventario.db";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

    public String crearBackup(String directorioDestino) throws IOException {
        File dbFile = new File(DB_FILE);

        if (!dbFile.exists()) {
            throw new IOException("El archivo de base de datos no existe: " + DB_FILE);
        }

        // Crear nombre del backup con timestamp
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String nombreBackup = "inventario_" + timestamp + ".db";

        Path destino = Path.of(directorioDestino, nombreBackup);

        // Copiar archivo
        Files.copy(dbFile.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

        log.info("Backup creado exitosamente: {}", destino.toAbsolutePath());
        return destino.toAbsolutePath().toString();
    }

    public void restaurarBackup(String archivoBackup) throws IOException {
        File backupFile = new File(archivoBackup);

        if (!backupFile.exists()) {
            throw new IOException("El archivo de backup no existe: " + archivoBackup);
        }

        File dbFile = new File(DB_FILE);

        // Crear backup de seguridad del archivo actual antes de restaurar
        if (dbFile.exists()) {
            String backupSeguridad = DB_FILE + ".before-restore";
            Files.copy(dbFile.toPath(), Path.of(backupSeguridad), StandardCopyOption.REPLACE_EXISTING);
            log.info("Backup de seguridad creado: {}", backupSeguridad);
        }

        // Restaurar el backup
        Files.copy(backupFile.toPath(), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        log.info("Backup restaurado exitosamente desde: {}", archivoBackup);
        log.warn("IMPORTANTE: La aplicación debe reiniciarse para que los cambios tengan efecto");
    }
}

