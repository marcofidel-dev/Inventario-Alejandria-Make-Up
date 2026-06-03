package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.service.BackupService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackupController {

    private final BackupService backupService;

    @FXML
    private Label lblUltimoBackup;

    @FXML
    public void initialize() {
        log.info("BackupController inicializado");
        lblUltimoBackup.setText("Ninguno");
    }

    @FXML
    private void crearBackup() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta para guardar backup");
        
        File directorioSeleccionado = directoryChooser.showDialog(null);
        
        if (directorioSeleccionado != null) {
            try {
                String rutaBackup = backupService.crearBackup(directorioSeleccionado.getAbsolutePath());
                lblUltimoBackup.setText(rutaBackup);
                mostrarInfo("Backup creado exitosamente en:\n" + rutaBackup);
            } catch (Exception e) {
                log.error("Error al crear backup", e);
                mostrarError("Error al crear backup: " + e.getMessage());
            }
        }
    }

    @FXML
    private void restaurarBackup() {
        Alert confirmacion = new Alert(Alert.AlertType.WARNING);
        confirmacion.setTitle("Advertencia");
        confirmacion.setHeaderText("¿Restaurar backup?");
        confirmacion.setContentText(
            "ADVERTENCIA: Esta acción reemplazará la base de datos actual.\n" +
            "Se creará una copia de seguridad antes de restaurar.\n\n" +
            "Deberá REINICIAR LA APLICACIÓN después de restaurar.\n\n" +
            "¿Desea continuar?"
        );
        
        if (confirmacion.showAndWait().orElse(null) == javafx.scene.control.ButtonType.OK) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar archivo de backup");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Base de datos SQLite", "*.db")
            );
            
            File archivoSeleccionado = fileChooser.showOpenDialog(null);
            
            if (archivoSeleccionado != null) {
                try {
                    backupService.restaurarBackup(archivoSeleccionado.getAbsolutePath());
                    mostrarInfo(
                        "Backup restaurado exitosamente.\n\n" +
                        "IMPORTANTE: Debe cerrar y reiniciar la aplicación ahora."
                    );
                } catch (Exception e) {
                    log.error("Error al restaurar backup", e);
                    mostrarError("Error al restaurar backup: " + e.getMessage());
                }
            }
        }
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

