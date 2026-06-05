package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.analytics.ReporteCajaService;
import com.marcofidel_dev.inventario.application.analytics.dto.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReporteCajaController {

    private final ReporteCajaService reporteCajaService;

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;

    @FXML private Label lblSesiones;
    @FXML private Label lblDescuadres;
    @FXML private Label lblFaltante;
    @FXML private Label lblSobrante;

    @FXML private TableView<SesionCajaResumenDTO>         tblSesiones;
    @FXML private TableColumn<SesionCajaResumenDTO, String>     colSFecha;
    @FXML private TableColumn<SesionCajaResumenDTO, String>     colSUsuario;
    @FXML private TableColumn<SesionCajaResumenDTO, BigDecimal> colSApertura;
    @FXML private TableColumn<SesionCajaResumenDTO, BigDecimal> colSEsperado;
    @FXML private TableColumn<SesionCajaResumenDTO, BigDecimal> colSDeclarado;
    @FXML private TableColumn<SesionCajaResumenDTO, BigDecimal> colSDiferencia;
    @FXML private TableColumn<SesionCajaResumenDTO, String>     colSEstado;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        LocalDate hoy = LocalDate.now();
        dpDesde.setValue(hoy.withDayOfMonth(1));
        dpHasta.setValue(hoy);
        configurarTabla();
        consultar();
    }

    private void configurarTabla() {
        colSFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().openingDate() != null ? c.getValue().openingDate().format(DT_FMT) : "—"));
        colSUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombreUsuario()));
        colSApertura.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().initialCash()));
        colSApertura.setCellFactory(col -> new MoneyCell<>());
        colSEsperado.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().expectedCash()));
        colSEsperado.setCellFactory(col -> new MoneyCell<>());
        colSDeclarado.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().declaredCash()));
        colSDeclarado.setCellFactory(col -> new MoneyCell<>());
        colSDiferencia.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().cashDifference()));
        colSDiferencia.setCellFactory(col -> new DiffCell<>());
        colSEstado.setCellValueFactory(c -> new SimpleStringProperty(estadoLabel(c.getValue())));

        // Doble clic abre detalle (simple alert por ahora)
        tblSesiones.setRowFactory(tv -> {
            TableRow<SesionCajaResumenDTO> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    mostrarDetalleSesion(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    public void consultar() {
        LocalDate desde = dpDesde.getValue() != null ? dpDesde.getValue() : LocalDate.now().withDayOfMonth(1);
        LocalDate hasta = dpHasta.getValue() != null ? dpHasta.getValue() : LocalDate.now();
        CompletableFuture.runAsync(() -> {
            try {
                List<SesionCajaResumenDTO> sesiones = reporteCajaService.listarSesiones(desde, hasta);
                DescuadresResumenDTO descuadres = reporteCajaService.getResumenDescuadres(desde, hasta);
                Platform.runLater(() -> {
                    tblSesiones.setItems(FXCollections.observableArrayList(sesiones));
                    lblSesiones.setText(String.valueOf(descuadres.cantidadSesiones()));
                    lblDescuadres.setText(String.valueOf(descuadres.cantidadDescuadres()));
                    lblFaltante.setText(cop(descuadres.totalFaltante()));
                    lblSobrante.setText(cop(descuadres.totalSobrante()));
                });
            } catch (Exception ex) {
                log.error("Error cargando reporte caja", ex);
            }
        });
    }

    private void mostrarDetalleSesion(SesionCajaResumenDTO sesion) {
        CompletableFuture.runAsync(() -> {
            try {
                SesionCajaDetalleDTO detalle = reporteCajaService.getDetalleSesion(sesion.id());
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Detalle de sesión #" + sesion.id());
                    alert.setHeaderText("Sesión de " + sesion.nombreUsuario());
                    alert.setContentText(
                            "Apertura: " + (sesion.openingDate() != null ? sesion.openingDate().format(DT_FMT) : "—") +
                            "\nCierre: "  + (sesion.closingDate()  != null ? sesion.closingDate().format(DT_FMT) : "Abierta") +
                            "\nInicial: " + cop(sesion.initialCash()) +
                            "\nEsperado: " + cop(sesion.expectedCash()) +
                            "\nDeclarado: " + cop(sesion.declaredCash()) +
                            "\nDiferencia: " + cop(sesion.cashDifference()) +
                            "\n\nVentas: " + detalle.cantidadVentas() +
                            "\nTotal ventas: " + cop(detalle.totalVentas())
                    );
                    alert.showAndWait();
                });
            } catch (Exception ex) {
                log.error("Error cargando detalle sesión {}", sesion.id(), ex);
            }
        });
    }

    private String estadoLabel(SesionCajaResumenDTO s) {
        if (s.cashDifference() == null) return s.status();
        BigDecimal dif = s.cashDifference();
        if (dif.compareTo(BigDecimal.ZERO) == 0) return "✅ Cuadrado";
        if (dif.compareTo(BigDecimal.ZERO) < 0)  return "🔴 Faltante";
        return "🟡 Sobrante";
    }

    private String cop(BigDecimal v) {
        if (v == null) return "$0";
        long val = v.setScale(0, RoundingMode.HALF_UP).longValue();
        if (val < 0) return "-" + cop(v.negate());
        String d = String.valueOf(val);
        StringBuilder sb = new StringBuilder();
        int rem = d.length() % 3;
        if (rem > 0) sb.append(d, 0, rem);
        for (int i = rem; i < d.length(); i += 3) {
            if (!sb.isEmpty()) sb.append('.');
            sb.append(d, i, i + 3);
        }
        return "$" + sb;
    }

    private static class MoneyCell<T> extends TableCell<T, BigDecimal> {
        @Override protected void updateItem(BigDecimal v, boolean empty) {
            super.updateItem(v, empty);
            if (empty || v == null) { setText(null); return; }
            long val = v.setScale(0, RoundingMode.HALF_UP).longValue();
            String d = String.valueOf(Math.abs(val));
            StringBuilder sb = new StringBuilder();
            int rem = d.length() % 3;
            if (rem > 0) sb.append(d, 0, rem);
            for (int i = rem; i < d.length(); i += 3) {
                if (!sb.isEmpty()) sb.append('.');
                sb.append(d, i, i + 3);
            }
            setText((val < 0 ? "-$" : "$") + sb);
        }
    }

    private static class DiffCell<T> extends TableCell<T, BigDecimal> {
        @Override protected void updateItem(BigDecimal v, boolean empty) {
            super.updateItem(v, empty);
            if (empty || v == null) { setText(null); setStyle(""); return; }
            long val = v.setScale(0, RoundingMode.HALF_UP).longValue();
            String d = String.valueOf(Math.abs(val));
            StringBuilder sb = new StringBuilder();
            int rem = d.length() % 3;
            if (rem > 0) sb.append(d, 0, rem);
            for (int i = rem; i < d.length(); i += 3) {
                if (!sb.isEmpty()) sb.append('.');
                sb.append(d, i, i + 3);
            }
            setText((val < 0 ? "-$" : "$") + sb);
            if (val < 0) setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
            else if (val > 0) setStyle("-fx-text-fill: #F57C00; -fx-font-weight: bold;");
            else setStyle("-fx-text-fill: #388E3C;");
        }
    }
}
