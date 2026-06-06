package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.analytics.ExportService;
import com.marcofidel_dev.inventario.application.analytics.ReporteVentasService;
import com.marcofidel_dev.inventario.shared.money.MoneyCOP;
import com.marcofidel_dev.inventario.ui.common.MoneyTableCell;
import com.marcofidel_dev.inventario.application.analytics.dto.*;
import com.marcofidel_dev.inventario.application.dto.UserDTO;
import com.marcofidel_dev.inventario.application.service.UserService;
import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReporteVentasController {

    private final ReporteVentasService reporteVentasService;
    private final ExportService exportService;
    private final UserService userService;

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private ComboBox<UserDTO> cbUsuario;
    @FXML private ComboBox<PaymentMethod> cbMetodo;
    @FXML private CheckBox chkAnuladas;

    @FXML private Label lblTotal;
    @FXML private Label lblCantVentas;
    @FXML private Label lblUtilidad;
    @FXML private Label lblMargen;

    @FXML private TableView<VentaResumenDTO>         tblVentas;
    @FXML private TableColumn<VentaResumenDTO, Long>       colId;
    @FXML private TableColumn<VentaResumenDTO, String>     colFecha;
    @FXML private TableColumn<VentaResumenDTO, String>     colCliente;
    @FXML private TableColumn<VentaResumenDTO, BigDecimal> colTotal;
    @FXML private TableColumn<VentaResumenDTO, String>     colMetodo;
    @FXML private TableColumn<VentaResumenDTO, String>     colEstado;
    @FXML private TableColumn<VentaResumenDTO, String>     colUsuario;
    @FXML private TableColumn<VentaResumenDTO, Integer>    colItems;

    @FXML private TableView<DesglosePagoDTO>           tblDesglosePago;
    @FXML private TableColumn<DesglosePagoDTO, String>     colDPMetodo;
    @FXML private TableColumn<DesglosePagoDTO, BigDecimal> colDPTotal;
    @FXML private TableColumn<DesglosePagoDTO, Integer>    colDPVentas;
    @FXML private TableColumn<DesglosePagoDTO, BigDecimal> colDPPct;

    @FXML private ProgressIndicator progressExport;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        LocalDate hoy = LocalDate.now();
        dpDesde.setValue(hoy.withDayOfMonth(1));
        dpHasta.setValue(hoy);

        cbUsuario.setConverter(new StringConverter<>() {
            @Override public String toString(UserDTO u) { return u == null ? "Todos" : u.getFullName(); }
            @Override public UserDTO fromString(String s) { return null; }
        });
        cbUsuario.getItems().add(null);
        try { cbUsuario.getItems().addAll(userService.listUsers()); }
        catch (Exception e) { log.warn("No se pudieron cargar usuarios", e); }

        cbMetodo.setConverter(new StringConverter<>() {
            @Override public String toString(PaymentMethod m) { return m == null ? "Todos" : m.name(); }
            @Override public PaymentMethod fromString(String s) { return null; }
        });
        cbMetodo.getItems().add(null);
        cbMetodo.getItems().addAll(PaymentMethod.values());

        configurarTablas();
        aplicarFiltros();
    }

    private void configurarTablas() {
        colId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().id()));
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().saleDate() != null ? c.getValue().saleDate().format(DT_FMT) : ""));
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().clienteNombre()));
        colTotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().total()));
        colTotal.setCellFactory(MoneyTableCell.factory());
        colMetodo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().paymentMethod()));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status()));
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().usuarioNombre()));
        colItems.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().cantidadItems()));

        colDPMetodo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().metodoPago()));
        colDPTotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().total()));
        colDPTotal.setCellFactory(MoneyTableCell.factory());
        colDPVentas.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().cantidadVentas()));
        colDPPct.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().porcentaje()));
        colDPPct.setCellFactory(col -> new PctCell<>());
    }

    @FXML
    public void aplicarFiltros() {
        FiltroReporteDTO filtro = buildFiltro();
        CompletableFuture.runAsync(() -> {
            try {
                ReporteVentasPeriodoDTO r = reporteVentasService.generarReporte(filtro);
                List<VentaResumenDTO> ventas = reporteVentasService.listarVentasFiltradas(filtro);
                Platform.runLater(() -> {
                    lblTotal.setText(cop(r.totalVendido()));
                    lblCantVentas.setText(String.valueOf(r.cantidadVentas()));
                    lblUtilidad.setText(cop(r.utilidadTotal()));
                    lblMargen.setText(r.margenPorcentaje().setScale(1, RoundingMode.HALF_UP) + "%");
                    tblVentas.setItems(FXCollections.observableArrayList(ventas));
                    tblDesglosePago.setItems(FXCollections.observableArrayList(r.desglosePorMetodo()));
                });
            } catch (Exception ex) {
                log.error("Error cargando reporte ventas", ex);
            }
        });
    }

    @FXML
    public void limpiarFiltros() {
        LocalDate hoy = LocalDate.now();
        dpDesde.setValue(hoy.withDayOfMonth(1));
        dpHasta.setValue(hoy);
        cbUsuario.setValue(null);
        cbMetodo.setValue(null);
        chkAnuladas.setSelected(false);
        aplicarFiltros();
    }

    @FXML
    public void exportarPDF() {
        FiltroReporteDTO filtro = buildFiltro();
        guardarArchivo("reporte_ventas.pdf", "Archivos PDF", "*.pdf",
                () -> exportService.exportarReporteVentasPDF(filtro));
    }

    @FXML
    public void exportarExcel() {
        FiltroReporteDTO filtro = buildFiltro();
        guardarArchivo("reporte_ventas.xlsx", "Archivos Excel", "*.xlsx",
                () -> exportService.exportarReporteVentasExcel(filtro));
    }

    private FiltroReporteDTO buildFiltro() {
        return new FiltroReporteDTO(
                dpDesde.getValue() != null ? dpDesde.getValue() : LocalDate.now().minusDays(29),
                dpHasta.getValue() != null ? dpHasta.getValue() : LocalDate.now(),
                cbUsuario.getValue() != null ? cbUsuario.getValue().getId() : null,
                null, null,
                cbMetodo.getValue(),
                chkAnuladas.isSelected());
    }

    private void guardarArchivo(String nombre, String desc, String ext, Supplier<byte[]> datos) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar archivo");
        fc.setInitialFileName(nombre);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, ext));
        File file = fc.showSaveDialog(progressExport.getScene().getWindow());
        if (file == null) return;
        progressExport.setVisible(true);
        CompletableFuture.runAsync(() -> {
            try {
                Files.write(file.toPath(), datos.get());
            } catch (Exception ex) {
                log.error("Error exportando {}", nombre, ex);
            } finally {
                Platform.runLater(() -> progressExport.setVisible(false));
            }
        });
    }

    // ── Format helpers ────────────────────────────────────────────────────────

    private String cop(BigDecimal v) {
        return MoneyCOP.format(v);
    }

    private static class PctCell<T> extends TableCell<T, BigDecimal> {
        @Override protected void updateItem(BigDecimal v, boolean empty) {
            super.updateItem(v, empty);
            setText(empty || v == null ? null : v.setScale(1, java.math.RoundingMode.HALF_UP) + "%");
        }
    }
}
