package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.analytics.ExportService;
import com.marcofidel_dev.inventario.application.analytics.ReporteClientesService;
import com.marcofidel_dev.inventario.application.analytics.dto.*;
import com.marcofidel_dev.inventario.application.service.CustomerService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class ReporteClientesController {

    private final ReporteClientesService reporteClientesService;
    private final CustomerService customerService;
    private final ExportService exportService;

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;

    @FXML private Label lblTotalClientes;
    @FXML private Label lblClientesActivos;
    @FXML private Label lblClientesInactivos;

    @FXML private TableView<TopClienteDTO>            tblTopClientes;
    @FXML private TableColumn<TopClienteDTO, String>     colTCNombre;
    @FXML private TableColumn<TopClienteDTO, String>     colTCPhone;
    @FXML private TableColumn<TopClienteDTO, Integer>    colTCCompras;
    @FXML private TableColumn<TopClienteDTO, BigDecimal> colTCTotal;
    @FXML private TableColumn<TopClienteDTO, String>     colTCUltima;

    @FXML private Spinner<Integer>                    spinerDias;

    @FXML private TableView<ClienteInactivoDTO>       tblClientesInactivos;
    @FXML private TableColumn<ClienteInactivoDTO, String>     colCINombre;
    @FXML private TableColumn<ClienteInactivoDTO, String>     colCIPhone;
    @FXML private TableColumn<ClienteInactivoDTO, String>     colCIUltima;
    @FXML private TableColumn<ClienteInactivoDTO, Long>       colCIDias;
    @FXML private TableColumn<ClienteInactivoDTO, BigDecimal> colCIHistorico;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        LocalDate hoy = LocalDate.now();
        dpDesde.setValue(hoy.withDayOfMonth(1));
        dpHasta.setValue(hoy);

        spinerDias.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(7, 365, 90));

        configurarTablas();
        aplicar();
    }

    private void configurarTablas() {
        colTCNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colTCPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().phone()));
        colTCCompras.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().cantidadCompras()));
        colTCTotal.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().totalComprado()));
        colTCTotal.setCellFactory(col -> new MoneyCell<>());
        colTCUltima.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().ultimaCompra() != null ? c.getValue().ultimaCompra().format(DT_FMT) : "—"));

        colCINombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colCIPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().phone()));
        colCIUltima.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().ultimaCompra() != null ? c.getValue().ultimaCompra().toString() : "Nunca"));
        colCIDias.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().diasSinComprar()));
        colCIHistorico.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().totalHistorico()));
        colCIHistorico.setCellFactory(col -> new MoneyCell<>());
    }

    @FXML
    public void aplicar() {
        LocalDate desde = dpDesde.getValue() != null ? dpDesde.getValue() : LocalDate.now().minusDays(29);
        LocalDate hasta = dpHasta.getValue() != null ? dpHasta.getValue() : LocalDate.now();
        CompletableFuture.runAsync(() -> {
            try {
                int totalClientes  = customerService.buscarPorNombreOTelefono("").size();
                List<TopClienteDTO> top = reporteClientesService.getTopClientes(desde, hasta, 10);
                List<ClienteInactivoDTO> inactivos90 = reporteClientesService.getClientesInactivos(90);

                Platform.runLater(() -> {
                    lblTotalClientes.setText(String.valueOf(totalClientes));
                    lblClientesActivos.setText(String.valueOf(top.size()));
                    lblClientesInactivos.setText(String.valueOf(inactivos90.size()));
                    tblTopClientes.setItems(FXCollections.observableArrayList(top));
                    tblClientesInactivos.setItems(FXCollections.observableArrayList(inactivos90));
                });
            } catch (Exception ex) {
                log.error("Error cargando reporte clientes", ex);
            }
        });
    }

    @FXML
    public void buscarInactivos() {
        int dias = spinerDias.getValue();
        CompletableFuture.runAsync(() -> {
            try {
                List<ClienteInactivoDTO> inactivos = reporteClientesService.getClientesInactivos(dias);
                Platform.runLater(() ->
                        tblClientesInactivos.setItems(FXCollections.observableArrayList(inactivos)));
            } catch (Exception ex) {
                log.error("Error buscando clientes inactivos", ex);
            }
        });
    }

    @FXML
    public void exportarExcel() {
        LocalDate desde = dpDesde.getValue() != null ? dpDesde.getValue() : LocalDate.now().minusDays(29);
        LocalDate hasta = dpHasta.getValue() != null ? dpHasta.getValue() : LocalDate.now();
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar archivo");
        fc.setInitialFileName("reporte_clientes.xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx"));
        File file = fc.showSaveDialog(tblTopClientes.getScene().getWindow());
        if (file == null) return;
        CompletableFuture.runAsync(() -> {
            try { Files.write(file.toPath(), exportService.exportarClientesExcel(desde, hasta)); }
            catch (Exception ex) { log.error("Error exportando clientes", ex); }
        });
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
}
