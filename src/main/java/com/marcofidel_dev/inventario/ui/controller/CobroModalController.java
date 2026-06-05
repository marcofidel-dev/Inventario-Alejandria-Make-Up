package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;
import com.marcofidel_dev.inventario.ui.common.FormatUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;

@Component
@Scope("prototype")
@Slf4j
public class CobroModalController {

    @FXML private Label lblTotal;
    @FXML private Label lblMetodo;
    @FXML private VBox pnlEfectivo;
    @FXML private TextField txtEfectivoRecibido;
    @FXML private Label lblCambio;
    @FXML private Button btnConfirmar;
    @FXML private Label lblError;

    private Stage stage;
    private BigDecimal totalAmount;
    private PaymentMethod metodoPago;
    private Consumer<BigDecimal> onConfirmado;

    public void setStage(Stage stage) { this.stage = stage; }
    public void setOnConfirmado(Consumer<BigDecimal> callback) { this.onConfirmado = callback; }

    @FXML
    public void initialize() {
        txtEfectivoRecibido.textProperty().addListener((obs, old, nv) -> {
            if (totalAmount != null) recalcularCambio();
        });
    }

    public void init(BigDecimal total, PaymentMethod metodo) {
        this.totalAmount = total;
        this.metodoPago = metodo;
        lblTotal.setText(FormatUtils.money(total));
        lblMetodo.setText(FormatUtils.metodoPago(metodo));

        boolean esEfectivo = metodo == PaymentMethod.EFECTIVO;
        pnlEfectivo.setVisible(esEfectivo);
        pnlEfectivo.setManaged(esEfectivo);

        if (esEfectivo) {
            txtEfectivoRecibido.setText(total.setScale(0, RoundingMode.CEILING).toPlainString());
            recalcularCambio();
            txtEfectivoRecibido.selectAll();
            txtEfectivoRecibido.requestFocus();
        } else {
            btnConfirmar.requestFocus();
        }
    }

    @FXML
    private void recalcularCambio() {
        try {
            BigDecimal recibido = new BigDecimal(txtEfectivoRecibido.getText().trim());
            BigDecimal cambio = recibido.subtract(totalAmount).setScale(2, RoundingMode.HALF_UP);
            boolean suficiente = recibido.compareTo(totalAmount) >= 0;

            lblCambio.setText(suficiente ? FormatUtils.money(cambio) : "Insuficiente");
            lblCambio.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: "
                    + (suficiente ? "#4CAF50" : "#D32F2F") + ";");
            btnConfirmar.setDisable(!suficiente);
            lblError.setText("");
        } catch (NumberFormatException e) {
            lblCambio.setText("—");
            btnConfirmar.setDisable(true);
        }
    }

    @FXML
    private void confirmar() {
        BigDecimal efectivoRecibido = null;
        if (metodoPago == PaymentMethod.EFECTIVO) {
            try {
                efectivoRecibido = new BigDecimal(txtEfectivoRecibido.getText().trim());
                if (efectivoRecibido.compareTo(totalAmount) < 0) {
                    lblError.setText("El efectivo recibido es insuficiente.");
                    return;
                }
            } catch (NumberFormatException e) {
                lblError.setText("Ingresa un monto válido.");
                return;
            }
        }
        if (stage != null) stage.close();
        if (onConfirmado != null) onConfirmado.accept(efectivoRecibido);
    }

    @FXML
    private void cancelar() {
        if (stage != null) stage.close();
    }
}
