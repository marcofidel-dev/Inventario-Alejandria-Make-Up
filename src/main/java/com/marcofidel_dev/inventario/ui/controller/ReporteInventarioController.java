package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.analytics.ExportService;
import com.marcofidel_dev.inventario.application.analytics.ReporteInventarioService;
import com.marcofidel_dev.inventario.application.analytics.ReporteVentasService;
import com.marcofidel_dev.inventario.application.analytics.dto.*;
import com.marcofidel_dev.inventario.shared.money.MoneyCOP;
import com.marcofidel_dev.inventario.ui.common.MoneyTableCell;
import com.marcofidel_dev.inventario.application.service.DashboardService;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReporteInventarioController {

    private final ReporteInventarioService reporteInventarioService;
    private final ReporteVentasService reporteVentasService;
    private final DashboardService dashboardService;
    private final ExportService exportService;

    @FXML private Label lblValorCosto;
    @FXML private Label lblValorVenta;
    @FXML private Label lblUtilPot;
    @FXML private Label lblMargenProm;
    @FXML private Label lblProdUnid;

    // Tab 1 — Inventario por valor
    @FXML private TableView<InventarioPorValorDTO>        tblInventario;
    @FXML private TableColumn<InventarioPorValorDTO, String>     colInvNombre;
    @FXML private TableColumn<InventarioPorValorDTO, String>     colInvCodigo;
    @FXML private TableColumn<InventarioPorValorDTO, Integer>    colInvStock;
    @FXML private TableColumn<InventarioPorValorDTO, BigDecimal> colInvCosto;
    @FXML private TableColumn<InventarioPorValorDTO, BigDecimal> colInvPrecio;
    @FXML private TableColumn<InventarioPorValorDTO, BigDecimal> colInvValorC;
    @FXML private TableColumn<InventarioPorValorDTO, BigDecimal> colInvValorV;

    // Tab 2 — Stock crítico
    @FXML private Label lblStockCriticoCount;
    @FXML private TableView<ProductoStockCriticoDTO>      tblStockCritico;
    @FXML private TableColumn<ProductoStockCriticoDTO, String>  colSCNombre;
    @FXML private TableColumn<ProductoStockCriticoDTO, String>  colSCCodigo;
    @FXML private TableColumn<ProductoStockCriticoDTO, Integer> colSCStock;
    @FXML private TableColumn<ProductoStockCriticoDTO, Integer> colSCMinimo;
    @FXML private TableColumn<ProductoStockCriticoDTO, Integer> colSCDif;

    // Tab 3 — Sin rotación
    @FXML private Label lblSinRotCount;
    @FXML private TableView<ProductoSinRotacionDTO>       tblSinRotacion;
    @FXML private TableColumn<ProductoSinRotacionDTO, String>  colSRNombre;
    @FXML private TableColumn<ProductoSinRotacionDTO, Integer> colSRStock;
    @FXML private TableColumn<ProductoSinRotacionDTO, String>  colSRUltVenta;

    // Tab 4 — Análisis ABC
    @FXML private TableView<ProductoABCDTO> tblABC_A;
    @FXML private TableColumn<ProductoABCDTO, String>     colAA_Nombre;
    @FXML private TableColumn<ProductoABCDTO, Integer>    colAA_Unid;
    @FXML private TableColumn<ProductoABCDTO, BigDecimal> colAA_Ingreso;
    @FXML private TableColumn<ProductoABCDTO, BigDecimal> colAA_Pct;

    @FXML private TableView<ProductoABCDTO> tblABC_B;
    @FXML private TableColumn<ProductoABCDTO, String>     colAB_Nombre;
    @FXML private TableColumn<ProductoABCDTO, Integer>    colAB_Unid;
    @FXML private TableColumn<ProductoABCDTO, BigDecimal> colAB_Ingreso;
    @FXML private TableColumn<ProductoABCDTO, BigDecimal> colAB_Pct;

    @FXML private TableView<ProductoABCDTO> tblABC_C;
    @FXML private TableColumn<ProductoABCDTO, String>     colAC_Nombre;
    @FXML private TableColumn<ProductoABCDTO, Integer>    colAC_Unid;
    @FXML private TableColumn<ProductoABCDTO, BigDecimal> colAC_Ingreso;
    @FXML private TableColumn<ProductoABCDTO, BigDecimal> colAC_Pct;

    // Tab 5 — Márgenes
    @FXML private TableView<MargenProductoDTO>            tblMargenes;
    @FXML private TableColumn<MargenProductoDTO, String>     colMgNombre;
    @FXML private TableColumn<MargenProductoDTO, String>     colMgCodigo;
    @FXML private TableColumn<MargenProductoDTO, BigDecimal> colMgCosto;
    @FXML private TableColumn<MargenProductoDTO, BigDecimal> colMgPrecio;
    @FXML private TableColumn<MargenProductoDTO, BigDecimal> colMgPesos;
    @FXML private TableColumn<MargenProductoDTO, BigDecimal> colMgPct;

    @FXML
    public void initialize() {
        configurarTablas();
        cargarDatos();
    }

    private void configurarTablas() {
        colInvNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colInvCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().codigoProducto()));
        colInvStock.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().stockActual()));
        colInvCosto.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().costo()));
        colInvCosto.setCellFactory(MoneyTableCell.factory());
        colInvPrecio.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().precioVenta()));
        colInvPrecio.setCellFactory(MoneyTableCell.factory());
        colInvValorC.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().valorACosto()));
        colInvValorC.setCellFactory(MoneyTableCell.factory());
        colInvValorV.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().valorAPrecioVenta()));
        colInvValorV.setCellFactory(MoneyTableCell.factory());

        colSCNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colSCCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().codigoProducto()));
        colSCStock.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().stockActual()));
        colSCMinimo.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().stockMinimo()));
        colSCDif.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().diferencia()));

        colSRNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colSRStock.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().stockActual()));
        colSRUltVenta.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().ultimaVenta() != null ? c.getValue().ultimaVenta().toString() : "Nunca"));

        wireABCColumns(tblABC_A, colAA_Nombre, colAA_Unid, colAA_Ingreso, colAA_Pct);
        wireABCColumns(tblABC_B, colAB_Nombre, colAB_Unid, colAB_Ingreso, colAB_Pct);
        wireABCColumns(tblABC_C, colAC_Nombre, colAC_Unid, colAC_Ingreso, colAC_Pct);

        colMgNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        colMgCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().codigoProducto()));
        colMgCosto.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().costo()));
        colMgCosto.setCellFactory(MoneyTableCell.factory());
        colMgPrecio.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().precioVenta()));
        colMgPrecio.setCellFactory(MoneyTableCell.factory());
        colMgPesos.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().margenPesos()));
        colMgPesos.setCellFactory(MoneyTableCell.factory());
        colMgPct.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().margenPorcentaje()));
        colMgPct.setCellFactory(col -> new PctCell<>());
    }

    private void wireABCColumns(TableView<ProductoABCDTO> tbl,
                                TableColumn<ProductoABCDTO, String> cNombre,
                                TableColumn<ProductoABCDTO, Integer> cUnid,
                                TableColumn<ProductoABCDTO, BigDecimal> cIngreso,
                                TableColumn<ProductoABCDTO, BigDecimal> cPct) {
        cNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nombre()));
        cUnid.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().unidadesVendidas()));
        cIngreso.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().ingreso()));
        cIngreso.setCellFactory(MoneyTableCell.factory());
        cPct.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().porcentajeAcumulado()));
        cPct.setCellFactory(col -> new PctCell<>());
    }

    private void cargarDatos() {
        LocalDate desde = LocalDate.now().minusDays(29);
        LocalDate hasta = LocalDate.now();
        CompletableFuture.runAsync(() -> {
            try {
                ValoracionInventarioDTO val               = reporteInventarioService.getValoracionActual();
                List<InventarioPorValorDTO> inv           = reporteInventarioService.getInventarioPorValor();
                List<ProductoStockCriticoDTO> critico     = dashboardService.getProductosStockCritico();
                List<ProductoSinRotacionDTO> sinRot       = dashboardService.getProductosSinRotacion(30);
                AnalisisABCDTO abc                        = reporteVentasService.getAnalisisABC(desde, hasta);
                List<MargenProductoDTO> margenes          = reporteInventarioService.getMargenes();

                Platform.runLater(() -> {
                    lblValorCosto.setText(cop(val.valorACosto()));
                    lblValorVenta.setText(cop(val.valorAPrecioVenta()));
                    lblUtilPot.setText(cop(val.utilidadPotencial()));
                    lblMargenProm.setText(val.margenPromedio().setScale(1, RoundingMode.HALF_UP) + "%");
                    lblProdUnid.setText(val.totalProductos() + " / " + val.totalUnidades());

                    tblInventario.setItems(FXCollections.observableArrayList(inv));

                    tblStockCritico.setItems(FXCollections.observableArrayList(critico));
                    lblStockCriticoCount.setText(critico.isEmpty()
                            ? "Sin productos en stock crítico"
                            : critico.size() + " producto(s) por debajo del stock mínimo");

                    tblSinRotacion.setItems(FXCollections.observableArrayList(sinRot));
                    lblSinRotCount.setText(sinRot.isEmpty()
                            ? "Sin productos sin rotación en 30 días"
                            : sinRot.size() + " producto(s) sin ventas en 30+ días");

                    tblABC_A.setItems(FXCollections.observableArrayList(abc.categoriaA()));
                    tblABC_B.setItems(FXCollections.observableArrayList(abc.categoriaB()));
                    tblABC_C.setItems(FXCollections.observableArrayList(abc.categoriaC()));

                    tblMargenes.setItems(FXCollections.observableArrayList(margenes));
                });
            } catch (Exception ex) {
                log.error("Error cargando reporte inventario", ex);
            }
        });
    }

    @FXML
    public void exportarPDF() {
        guardarArchivo("reporte_inventario.pdf", "Archivos PDF", "*.pdf",
                () -> exportService.exportarInventarioPDF());
    }

    @FXML
    public void exportarExcel() {
        guardarArchivo("reporte_inventario.xlsx", "Archivos Excel", "*.xlsx",
                () -> exportService.exportarInventarioExcel());
    }

    private void guardarArchivo(String nombre, String desc, String ext,
                                java.util.function.Supplier<byte[]> datos) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar archivo");
        fc.setInitialFileName(nombre);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, ext));
        File file = fc.showSaveDialog(tblInventario.getScene().getWindow());
        if (file == null) return;
        CompletableFuture.runAsync(() -> {
            try { Files.write(file.toPath(), datos.get()); }
            catch (Exception ex) { log.error("Error exportando {}", nombre, ex); }
        });
    }

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
