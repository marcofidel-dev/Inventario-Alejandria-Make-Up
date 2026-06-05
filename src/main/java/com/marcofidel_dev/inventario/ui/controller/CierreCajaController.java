package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.service.CashSessionService;
import com.marcofidel_dev.inventario.application.dto.CashSessionResumenDTO;
import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;
import com.marcofidel_dev.inventario.ui.common.FormatUtils;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class CierreCajaController {

    private final CashSessionService cashSessionService;

    @FXML private Label lblFechaApertura;
    @FXML private Label lblEfectivoInicial;
    @FXML private Label lblVentasEfectivo;
    @FXML private Label lblVentasTarjeta;
    @FXML private Label lblVentasTransferencia;
    @FXML private Label lblVentasOtros;
    @FXML private Label lblTotalVentas;
    @FXML private Label lblCantidadVentas;
    @FXML private Label lblEfectivoEsperado;
    @FXML private TextField txtEfectivoDeclarado;
    @FXML private Label lblDiferencia;
    @FXML private Label lblDiferenciaTag;
    @FXML private TextArea txtObservaciones;
    @FXML private Button btnCerrarCaja;
    @FXML private Label lblError;

    private Stage stage;
    private Long sesionId;
    private BigDecimal efectivoEsperado;
    private Runnable onCajaCerrada;

    public void setStage(Stage stage)               { this.stage = stage; }
    public void setSesionId(Long id)                { this.sesionId = id; }
    public void setOnCajaCerrada(Runnable callback) { this.onCajaCerrada = callback; }

    @FXML
    public void initialize() {
        txtEfectivoDeclarado.setOnAction(e -> cerrarCaja());
    }

    public void cargarResumen() {
        if (sesionId == null) return;
        try {
            CashSessionResumenDTO r = cashSessionService.getResumenSesion(sesionId);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy  hh:mm a");
            lblFechaApertura.setText(r.openingDate().format(fmt));
            lblEfectivoInicial.setText(FormatUtils.money(r.initialCash()));

            lblVentasEfectivo.setText(FormatUtils.money(
                    r.salesByMethod().getOrDefault(PaymentMethod.EFECTIVO, BigDecimal.ZERO)));
            lblVentasTarjeta.setText(FormatUtils.money(
                    r.salesByMethod().getOrDefault(PaymentMethod.TARJETA, BigDecimal.ZERO)));
            lblVentasTransferencia.setText(FormatUtils.money(
                    r.salesByMethod().getOrDefault(PaymentMethod.TRANSFERENCIA, BigDecimal.ZERO)));

            BigDecimal otros = r.salesByMethod().getOrDefault(PaymentMethod.NEQUI, BigDecimal.ZERO)
                    .add(r.salesByMethod().getOrDefault(PaymentMethod.DAVIPLATA, BigDecimal.ZERO))
                    .add(r.salesByMethod().getOrDefault(PaymentMethod.MIXTO, BigDecimal.ZERO));
            lblVentasOtros.setText(FormatUtils.money(otros));

            lblTotalVentas.setText(FormatUtils.money(r.totalSales()));
            lblCantidadVentas.setText(String.valueOf(r.saleCount()));

            efectivoEsperado = r.expectedCash();
            lblEfectivoEsperado.setText(FormatUtils.money(efectivoEsperado));

            txtEfectivoDeclarado.setText(efectivoEsperado.setScale(0, RoundingMode.HALF_UP).toPlainString());
            recalcularDiferencia();
            txtEfectivoDeclarado.requestFocus();
            txtEfectivoDeclarado.selectAll();
        } catch (Exception e) {
            lblError.setText("Error al cargar el resumen: " + e.getMessage());
            log.error("Error cargando resumen de sesión {}", sesionId, e);
        }
    }

    @FXML
    private void recalcularDiferencia() {
        if (efectivoEsperado == null) return;
        try {
            BigDecimal declarado = new BigDecimal(txtEfectivoDeclarado.getText().trim().replace(",", "."));
            BigDecimal diff = declarado.subtract(efectivoEsperado).setScale(2, RoundingMode.HALF_UP);
            lblDiferencia.setText(FormatUtils.money(diff));

            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                lblDiferencia.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");
                lblDiferenciaTag.setText("Sobrante");
                lblDiferenciaTag.setStyle("-fx-text-fill: #2E7D32; -fx-font-style: italic;");
            } else if (diff.compareTo(BigDecimal.ZERO) < 0) {
                lblDiferencia.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #D32F2F;");
                lblDiferenciaTag.setText("Faltante");
                lblDiferenciaTag.setStyle("-fx-text-fill: #D32F2F; -fx-font-style: italic;");
            } else {
                lblDiferencia.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #555;");
                lblDiferenciaTag.setText("Cuadre exacto");
                lblDiferenciaTag.setStyle("-fx-text-fill: #555; -fx-font-style: italic;");
            }
        } catch (NumberFormatException e) {
            lblDiferencia.setText("—");
            lblDiferenciaTag.setText("");
        }
    }

    @FXML
    private void cerrarCaja() {
        lblError.setText("");
        String texto = txtEfectivoDeclarado.getText().trim().replace(",", ".");
        if (texto.isBlank()) {
            lblError.setText("Ingresa el efectivo declarado.");
            return;
        }
        BigDecimal declarado;
        try {
            declarado = new BigDecimal(texto);
        } catch (NumberFormatException e) {
            lblError.setText("Monto inválido.");
            return;
        }
        try {
            btnCerrarCaja.setDisable(true);
            String obs = txtObservaciones.getText().trim();
            cashSessionService.cerrarSesion(sesionId, declarado, obs.isEmpty() ? null : obs);
            if (stage != null) stage.close();
            if (onCajaCerrada != null) onCajaCerrada.run();
        } catch (Exception e) {
            lblError.setText(e.getMessage());
            btnCerrarCaja.setDisable(false);
        }
    }

    @FXML
    private void cancelar() {
        if (stage != null) stage.close();
    }
}
