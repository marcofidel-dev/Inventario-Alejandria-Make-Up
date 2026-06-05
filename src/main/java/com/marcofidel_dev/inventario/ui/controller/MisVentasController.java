package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.service.SaleService;
import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;
import com.marcofidel_dev.inventario.domain.entity.Sale;
import com.marcofidel_dev.inventario.domain.entity.SaleItem;
import com.marcofidel_dev.inventario.domain.entity.SaleStatus;
import com.marcofidel_dev.inventario.ui.common.FormatUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MisVentasController {

    private final SaleService saleService;

    @FXML private Label lblFechaHoy;
    @FXML private Label lblTotalCobrado;
    @FXML private Label lblCantidadVentas;
    @FXML private Label lblTotalEfectivo;
    @FXML private Label lblTotalDigital;
    @FXML private TableView<Sale> tblVentas;
    @FXML private TableColumn<Sale, String> colHora;
    @FXML private TableColumn<Sale, String> colCliente;
    @FXML private TableColumn<Sale, String> colItems;
    @FXML private TableColumn<Sale, String> colTotal;
    @FXML private TableColumn<Sale, String> colMetodo;
    @FXML private TableColumn<Sale, String> colEstado;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        lblFechaHoy.setText(LocalDate.now().format(DATE_FMT));
        configurarColumnas();
        configurarDobleClick();
        cargar();
    }

    private void configurarColumnas() {
        colHora.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getSaleDate().format(TIME_FMT)));
        colCliente.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCustomer() != null
                        ? c.getValue().getCustomer().getName() : "Ocasional"));
        colItems.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getItems().size())));
        colTotal.setCellValueFactory(c ->
                new SimpleStringProperty(FormatUtils.money(c.getValue().getTotal())));
        colMetodo.setCellValueFactory(c ->
                new SimpleStringProperty(FormatUtils.metodoPago(c.getValue().getPaymentMethod())));
        colEstado.setCellValueFactory(c -> {
            boolean anulada = c.getValue().getStatus() == SaleStatus.ANULADA;
            return new SimpleStringProperty(anulada ? "❌ Anulada" : "✓ Completada");
        });
    }

    private void configurarDobleClick() {
        tblVentas.setRowFactory(tv -> {
            TableRow<Sale> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) verDetalle(row.getItem());
            });
            return row;
        });
    }

    @FXML
    private void cargar() {
        List<Sale> ventas = saleService.listarVentasMiSesion();
        tblVentas.setItems(FXCollections.observableArrayList(ventas));

        List<Sale> completadas = ventas.stream().filter(s -> s.getStatus() == SaleStatus.COMPLETADA).toList();
        BigDecimal totalCobrado = completadas.stream().map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalEfectivo = completadas.stream()
                .filter(s -> s.getPaymentMethod() == PaymentMethod.EFECTIVO)
                .map(Sale::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        lblTotalCobrado.setText(FormatUtils.money(totalCobrado));
        lblCantidadVentas.setText(String.valueOf(completadas.size()));
        lblTotalEfectivo.setText(FormatUtils.money(totalEfectivo));
        lblTotalDigital.setText(FormatUtils.money(totalCobrado.subtract(totalEfectivo)));
    }

    private void verDetalle(Sale sale) {
        StringBuilder sb = new StringBuilder();
        for (SaleItem item : sale.getItems()) {
            sb.append(item.getQuantity()).append(" x ").append(item.getProducto().getNombre())
              .append("  →  ").append(FormatUtils.money(item.getSubtotal())).append("\n");
        }
        sb.append("─────────────────────────────\n");
        sb.append("Subtotal:  ").append(FormatUtils.money(sale.getSubtotal())).append("\n");
        if (sale.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("Descuento: ").append(FormatUtils.money(sale.getDiscountAmount())).append("\n");
        }
        sb.append("TOTAL:     ").append(FormatUtils.money(sale.getTotal())).append("\n");
        sb.append("Método:    ").append(FormatUtils.metodoPago(sale.getPaymentMethod())).append("\n");
        if (sale.getStatus() == SaleStatus.ANULADA) {
            sb.append("\nANULADA: ").append(sale.getVoidReason());
        }

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Detalle de Venta");
        alert.setHeaderText("Venta N° " + String.format("%05d", sale.getId())
                + "  —  " + sale.getSaleDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")));
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }
}
