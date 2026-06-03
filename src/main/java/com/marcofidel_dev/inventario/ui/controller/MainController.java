package com.marcofidel_dev.inventario.ui.controller;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.marcofidel_dev.inventario.ui.config.SpringFXMLLoader;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final SpringFXMLLoader fxmlLoader;

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        log.info("MainController inicializado");
        mostrarDashboard();
    }

    @FXML
    private void mostrarDashboard() {
        cargarVista("/fxml/dashboard.fxml");
    }

    @FXML
    private void mostrarProductos() {
        cargarVista("/fxml/productos.fxml");
    }

    @FXML
    private void mostrarCompras() {
        cargarVista("/fxml/compras.fxml");
    }

    @FXML
    private void mostrarBackup() {
        cargarVista("/fxml/backup.fxml");
    }

    private void cargarVista(String fxmlPath) {
        try {
            log.debug("Cargando vista: {}", fxmlPath);
            Parent vista = fxmlLoader.load(fxmlPath);
            contentArea.getChildren().clear();
            contentArea.getChildren().add(vista);
        } catch (IOException e) {
            log.error("Error al cargar vista: " + fxmlPath, e);
        }
    }
}

