package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.service.AuthService;
import com.marcofidel_dev.inventario.application.service.CashSessionService;
import com.marcofidel_dev.inventario.infrastructure.security.SessionContext;
import com.marcofidel_dev.inventario.ui.common.UIPermissionService;
import com.marcofidel_dev.inventario.ui.config.SpringFXMLLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final SpringFXMLLoader fxmlLoader;
    private final SessionContext sessionContext;
    private final UIPermissionService uiPermissionService;
    private final AuthService authService;
    private final CashSessionService cashSessionService;

    @FXML private StackPane contentArea;

    // ADMIN-only navigation items
    @FXML private Button btnCompras;
    @FXML private Button btnUsuarios;
    @FXML private Button btnDashboard;
    @FXML private Button btnReporteVentas;
    @FXML private Button btnReporteInventario;
    @FXML private Button btnReporteClientes;
    @FXML private Button btnReporteCaja;
    @FXML private Separator separadorAdmin;
    @FXML private Label lblAdminSection;

    // User info labels
    @FXML private Label lblUsuarioNombre;
    @FXML private Label lblUsuarioRol;

    @FXML
    public void initialize() {
        log.info("MainController inicializado");
        applyPermissions();
        // After the stage is shown, check for active cash session
        Platform.runLater(this::verificarAperturaCaja);
    }

    private void applyPermissions() {
        uiPermissionService.hideIfNotAdmin(btnCompras);
        uiPermissionService.hideIfNotAdmin(btnUsuarios);
        uiPermissionService.hideIfNotAdmin(btnDashboard);
        uiPermissionService.hideIfNotAdmin(btnReporteVentas);
        uiPermissionService.hideIfNotAdmin(btnReporteInventario);
        uiPermissionService.hideIfNotAdmin(btnReporteClientes);
        uiPermissionService.hideIfNotAdmin(btnReporteCaja);
        uiPermissionService.hideIfNotAdmin(separadorAdmin);
        uiPermissionService.hideIfNotAdmin(lblAdminSection);

        sessionContext.getCurrentUser().ifPresent(user -> {
            lblUsuarioNombre.setText(user.getFullName() != null ? user.getFullName() : user.getUsername());
            String rolLabel = "ADMIN".equals(user.getRole().name()) ? "Administrador" : "Colaborador";
            lblUsuarioRol.setText(rolLabel);
        });
    }

    private void verificarAperturaCaja() {
        if (cashSessionService.getSesionActiva().isEmpty()) {
            abrirAperturaCaja(() -> Platform.runLater(this::mostrarPos));
        } else {
            mostrarPos();
        }
    }

    // ─── POS navigation ─────────────────────────────────────────────────

    @FXML
    private void mostrarPos() {
        cargarVista("/fxml/pos.fxml");
    }

    @FXML
    private void mostrarCaja() {
        cashSessionService.getSesionActiva().ifPresentOrElse(
                session -> abrirCierreCaja(session.getId()),
                () -> abrirAperturaCaja(() -> Platform.runLater(this::mostrarPos))
        );
    }

    @FXML
    private void mostrarMisVentas() {
        cargarVista("/fxml/mis-ventas.fxml");
    }

    @FXML
    private void mostrarClientes() {
        // Clients screen — for now show a placeholder if not yet implemented
        try {
            cargarVista("/fxml/mis-ventas.fxml"); // reuse mis-ventas as placeholder
        } catch (Exception e) {
            log.warn("Vista de clientes no disponible");
        }
    }

    // ─── Shared navigation ──────────────────────────────────────────────

    @FXML
    private void mostrarProductos() {
        cargarVista("/fxml/productos.fxml");
    }

    @FXML
    private void mostrarCompras() {
        cargarVista("/fxml/compras.fxml");
    }

    @FXML
    private void mostrarUsuarios() {
        cargarVista("/fxml/users.fxml");
    }

    @FXML
    private void mostrarDashboard() {
        cargarVista("/fxml/dashboard.fxml");
    }

    @FXML
    private void mostrarReporteVentas() {
        cargarVista("/fxml/reporte-ventas.fxml");
    }

    @FXML
    private void mostrarReporteInventario() {
        cargarVista("/fxml/reporte-inventario.fxml");
    }

    @FXML
    private void mostrarReporteClientes() {
        cargarVista("/fxml/reporte-clientes.fxml");
    }

    @FXML
    private void mostrarReporteCaja() {
        cargarVista("/fxml/reporte-caja.fxml");
    }

    // ─── Session ────────────────────────────────────────────────────────

    @FXML
    private void cerrarSesion() {
        cashSessionService.getSesionActiva().ifPresentOrElse(
                session -> {
                    ButtonType btnCerrarCaja = new ButtonType("Cerrar Caja y Salir");
                    ButtonType btnSoloSalir = new ButtonType("Salir sin Cerrar Caja");
                    ButtonType btnCancelar = ButtonType.CANCEL;

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Cerrar Sesión");
                    confirm.setHeaderText("Tienes una sesión de caja abierta.");
                    confirm.setContentText("¿Qué deseas hacer?");
                    confirm.getButtonTypes().setAll(btnCerrarCaja, btnSoloSalir, btnCancelar);

                    confirm.showAndWait().ifPresent(result -> {
                        if (result == btnCerrarCaja) {
                            abrirCierreCaja(session.getId(),
                                    () -> Platform.runLater(this::doLogout));
                        } else if (result == btnSoloSalir) {
                            doLogout();
                        }
                    });
                },
                this::doLogout
        );
    }

    private void doLogout() {
        authService.logout();
        abrirLogin();
    }

    // ─── Modal helpers ───────────────────────────────────────────────────

    private void abrirAperturaCaja(Runnable onSuccess) {
        try {
            Map<String, Object> data = fxmlLoader.loadWithController("/fxml/apertura-caja.fxml");
            Parent root = (Parent) data.get("root");
            AperturaCajaController ctrl = (AperturaCajaController) data.get("controller");

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(contentArea.getScene() != null ? contentArea.getScene().getWindow() : null);
            stage.setTitle("Apertura de Caja");
            stage.setScene(new Scene(root, 480, 520));
            stage.setResizable(false);
            stage.setOnCloseRequest(e -> e.consume()); // block close without opening

            ctrl.setStage(stage);
            ctrl.setOnCajaAbierta(onSuccess);

            stage.show();
        } catch (IOException e) {
            log.error("Error al abrir apertura de caja", e);
        }
    }

    private void abrirCierreCaja(Long sesionId) {
        abrirCierreCaja(sesionId, () -> Platform.runLater(this::mostrarPos));
    }

    private void abrirCierreCaja(Long sesionId, Runnable onCajaCerrada) {
        try {
            Map<String, Object> data = fxmlLoader.loadWithController("/fxml/cierre-caja.fxml");
            Parent root = (Parent) data.get("root");
            CierreCajaController ctrl = (CierreCajaController) data.get("controller");

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(contentArea.getScene().getWindow());
            stage.setTitle("Cierre de Caja");
            stage.setScene(new Scene(root, 520, 700));
            stage.setResizable(false);

            ctrl.setStage(stage);
            ctrl.setSesionId(sesionId);
            ctrl.setOnCajaCerrada(onCajaCerrada);

            stage.show();
            ctrl.cargarResumen();
        } catch (IOException e) {
            log.error("Error al abrir cierre de caja", e);
        }
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

    private void abrirLogin() {
        try {
            Map<String, Object> fxmlData = fxmlLoader.loadWithController("/fxml/login.fxml");
            Parent loginRoot = (Parent) fxmlData.get("root");
            LoginController loginCtrl = (LoginController) fxmlData.get("controller");

            Stage loginStage = new Stage();
            loginCtrl.setLoginStage(loginStage);
            loginStage.setTitle("Sistema de Inventario - Alejandria Make-Up");
            loginStage.setScene(new Scene(loginRoot, 420, 480));
            loginStage.setResizable(false);
            loginStage.setOnCloseRequest(e -> {
                Platform.exit();
                System.exit(0);
            });
            loginStage.show();

            Stage mainStage = (Stage) contentArea.getScene().getWindow();
            mainStage.close();

        } catch (IOException e) {
            log.error("Error al abrir login", e);
        }
    }
}
