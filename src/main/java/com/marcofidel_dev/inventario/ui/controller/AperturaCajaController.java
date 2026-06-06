package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.service.CashSessionService;
import com.marcofidel_dev.inventario.infrastructure.security.SessionContext;
import com.marcofidel_dev.inventario.shared.money.InvalidMoneyFormatException;
import com.marcofidel_dev.inventario.shared.money.MoneyCOP;
import java.math.BigDecimal;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class AperturaCajaController {

    private final CashSessionService cashSessionService;
    private final SessionContext sessionContext;

    @FXML private Label lblUsuario;
    @FXML private Label lblFecha;
    @FXML private TextField txtEfectivoInicial;
    @FXML private TextArea txtObservaciones;
    @FXML private Button btnAbrirCaja;
    @FXML private Label lblError;

    private Stage stage;
    private Runnable onCajaAbierta;

    public void setStage(Stage stage) { this.stage = stage; }
    public void setOnCajaAbierta(Runnable callback) { this.onCajaAbierta = callback; }

    @FXML
    public void initialize() {
        sessionContext.getCurrentUser().ifPresent(user ->
            lblUsuario.setText(user.getFullName() != null ? user.getFullName() : user.getUsername()));
        lblFecha.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy  hh:mm a")));
        txtEfectivoInicial.requestFocus();
        txtEfectivoInicial.setOnAction(e -> abrirCaja());
    }

    @FXML
    private void abrirCaja() {
        lblError.setText("");
        String texto = txtEfectivoInicial.getText().trim();
        if (texto.isBlank()) {
            lblError.setText("Ingresa el efectivo inicial (puede ser 0).");
            return;
        }
        BigDecimal efectivo;
        try {
            efectivo = MoneyCOP.parse(texto);
        } catch (InvalidMoneyFormatException e) {
            lblError.setText("Monto inválido. Usa solo números (ej: 100000).");
            return;
        }
        try {
            btnAbrirCaja.setDisable(true);
            String obs = txtObservaciones.getText().trim();
            cashSessionService.abrirSesion(efectivo, obs.isEmpty() ? null : obs);
            if (stage != null) stage.close();
            if (onCajaAbierta != null) onCajaAbierta.run();
        } catch (Exception e) {
            lblError.setText(e.getMessage());
            btnAbrirCaja.setDisable(false);
        }
    }
}
