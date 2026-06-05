package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.service.AuthService;
import com.marcofidel_dev.inventario.domain.entity.User;
import com.marcofidel_dev.inventario.ui.config.SpringFXMLLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final AuthService authService;
    private final SpringFXMLLoader fxmlLoader;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private Stage loginStage;

    /** Called by JavaFXApplication before the login stage is shown. */
    public void setLoginStage(Stage stage) {
        this.loginStage = stage;
    }

    @FXML
    private void onLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isBlank() || password.isBlank()) {
            lblError.setText("Por favor ingrese usuario y contraseña.");
            return;
        }

        lblError.setText("");

        Optional<User> result = authService.login(username, password);

        if (result.isPresent()) {
            log.info("Login successful, opening main window");
            openMainWindow();
        } else {
            lblError.setText("Usuario o contraseña incorrectos, o cuenta inactiva.");
            txtPassword.clear();
            txtUsername.requestFocus();
        }
    }

    @FXML
    private void onPasswordKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            onLogin();
        }
    }

    private void openMainWindow() {
        try {
            Parent root = fxmlLoader.load("/fxml/main.fxml");

            Stage mainStage = new Stage();
            mainStage.setTitle("Sistema de Inventario - Alejandria Make-Up");

            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/logo-tienda.png"));
                mainStage.getIcons().add(icon);
            } catch (Exception e) {
                log.warn("Could not load app icon: {}", e.getMessage());
            }

            mainStage.setScene(new Scene(root, 1200, 700));
            mainStage.setMinWidth(1000);
            mainStage.setMinHeight(600);
            mainStage.setOnCloseRequest(event -> {
                authService.logout();
                Platform.exit();
                System.exit(0);
            });

            mainStage.show();
            loginStage.close();

        } catch (Exception e) {
            log.error("Failed to open main window", e);
            lblError.setText("Error al abrir la aplicación: " + e.getMessage());
        }
    }
}
