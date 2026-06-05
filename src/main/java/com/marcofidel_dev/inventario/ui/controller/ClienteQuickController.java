package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.service.CustomerService;
import com.marcofidel_dev.inventario.domain.entity.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class ClienteQuickController {

    private final CustomerService customerService;

    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private Button btnCrear;
    @FXML private Label lblError;

    private Stage stage;
    private Consumer<Customer> onClienteCreado;

    public void setStage(Stage stage) { this.stage = stage; }
    public void setOnClienteCreado(Consumer<Customer> callback) { this.onClienteCreado = callback; }

    @FXML
    public void initialize() {
        txtNombre.requestFocus();
        txtNombre.setOnAction(e -> txtTelefono.requestFocus());
        txtTelefono.setOnAction(e -> crear());
    }

    @FXML
    private void crear() {
        lblError.setText("");
        String nombre = txtNombre.getText().trim();
        if (nombre.isBlank()) {
            lblError.setText("El nombre es obligatorio.");
            txtNombre.requestFocus();
            return;
        }
        try {
            btnCrear.setDisable(true);
            String tel = txtTelefono.getText().trim();
            Customer created = customerService.crearRapido(nombre, tel.isEmpty() ? null : tel);
            if (stage != null) stage.close();
            if (onClienteCreado != null) onClienteCreado.accept(created);
        } catch (Exception e) {
            lblError.setText(e.getMessage());
            btnCrear.setDisable(false);
        }
    }

    @FXML
    private void cancelar() {
        if (stage != null) stage.close();
    }
}
