package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.analytics.dto.*;
import com.marcofidel_dev.inventario.application.service.DashboardService;
import com.marcofidel_dev.inventario.shared.money.MoneyCOP;
import com.marcofidel_dev.inventario.ui.common.MoneyTableCell;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
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
public class DashboardController {

    private final DashboardService dashboardService;

    // ── Header ───────────────────────────────────────────────────────────────
    @FXML private ComboBox<String> cbPeriodo;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;

    // ── KPI Cards ────────────────────────────────────────────────────────────
    @FXML private Label lblVendido;
    @FXML private Label lblVendidoCambio;
    @FXML private Label lblUtilidad;
    @FXML private Label lblMargen;
    @FXML private Label lblVentas;
    @FXML private Label lblMejorVendedor;
    @FXML private Label lblTicket;
    @FXML private Label lblProductosVendidos;

    // ── Alertas ──────────────────────────────────────────────────────────────
    @FXML private VBox contenedorAlertas;

    // ── Charts ───────────────────────────────────────────────────────────────
    @FXML private LineChart<String, Number> chartVentas;
    @FXML private CategoryAxis xAxisVentas;
    @FXML private NumberAxis yAxisVentas;
    @FXML private BarChart<String, Number> chartHoras;

    // ── Top productos ────────────────────────────────────────────────────────
    @FXML private TableView<TopProductoDTO> tblTopProductos;
    @FXML private TableColumn<TopProductoDTO, String>     colTopNombre;
    @FXML private TableColumn<TopProductoDTO, Integer>    colTopUnidades;
    @FXML private TableColumn<TopProductoDTO, BigDecimal> colTopIngreso;
    @FXML private TableColumn<TopProductoDTO, BigDecimal> colTopUtilidad;
    @FXML private TableColumn<TopProductoDTO, BigDecimal> colTopMargen;

    // ── Usuarios ─────────────────────────────────────────────────────────────
    @FXML private TableView<DesgloseUsuarioDTO> tblUsuarios;
    @FXML private TableColumn<DesgloseUsuarioDTO, String>     colUserNombre;
    @FXML private TableColumn<DesgloseUsuarioDTO, Integer>    colUserVentas;
    @FXML private TableColumn<DesgloseUsuarioDTO, BigDecimal> colUserTotal;
    @FXML private TableColumn<DesgloseUsuarioDTO, BigDecimal> colUserTicket;

    // ── Valoración inventario ────────────────────────────────────────────────
    @FXML private Label lblValorCosto;
    @FXML private Label lblValorVenta;
    @FXML private Label lblUtilPotencial;
    @FXML private Label lblMargenInv;

    private static final DateTimeFormatter CHART_FMT = DateTimeFormatter.ofPattern("dd/MM");
    private Timeline autoRefresh;

    @FXML
    public void initialize() {
        configurarCombobox();
        configurarTablas();
        cargarDatos();
        configurarAutoRefresh();
    }

    private void configurarCombobox() {
        cbPeriodo.setItems(FXCollections.observableArrayList(
                "Hoy", "Ayer", "Últimos 7 días", "Este mes", "Mes anterior",
                "Últimos 30 días", "Este año", "Personalizar"));
        cbPeriodo.setValue("Hoy");
        cbPeriodo.setOnAction(e -> {
            boolean custom = "Personalizar".equals(cbPeriodo.getValue());
            dpDesde.setVisible(custom); dpDesde.setManaged(custom);
            dpHasta.setVisible(custom); dpHasta.setManaged(custom);
            if (!custom) cargarDatos();
        });
    }

    private void configurarTablas() {
        colTopNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().nombreProducto()));
        colTopUnidades.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().unidadesVendidas()));
        colTopIngreso.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().ingresoTotal()));
        colTopIngreso.setCellFactory(MoneyTableCell.factory());
        colTopUtilidad.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().utilidadTotal()));
        colTopUtilidad.setCellFactory(MoneyTableCell.factory());
        colTopMargen.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().margenPorcentaje()));
        colTopMargen.setCellFactory(col -> pctCell());

        colUserNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().nombreUsuario()));
        colUserVentas.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().cantidadVentas()));
        colUserTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().totalVendido()));
        colUserTotal.setCellFactory(MoneyTableCell.factory());
        colUserTicket.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().ticketPromedio()));
        colUserTicket.setCellFactory(MoneyTableCell.factory());
    }

    @FXML
    public void cargarDatos() {
        LocalDate[] rango = resolverRango();
        LocalDate desde = rango[0];
        LocalDate hasta = rango[1];
        boolean esHoy = LocalDate.now().equals(desde) && LocalDate.now().equals(hasta);

        CompletableFuture.runAsync(() -> {
            try {
                KPIsDelDiaDTO kpis         = esHoy ? dashboardService.getKPIsDelDia() : null;
                KPIsRangoDTO  kpisRango    = esHoy ? null : dashboardService.getKPIsRango(desde, hasta);
                AlertasResumenDTO alertas  = dashboardService.getAlertas();
                List<VentaDiariaDTO> series = dashboardService.getSeriesVentas(desde, hasta);
                List<VentaPorHoraDTO> horas = dashboardService.getVentasPorHora(LocalDate.now());
                List<TopProductoDTO> top    = dashboardService.getTopProductos(desde, hasta, 5);
                List<DesgloseUsuarioDTO> us = dashboardService.getDesglosePorUsuario(desde, hasta);
                ValoracionInventarioDTO val = dashboardService.getValoracionInventario();

                Platform.runLater(() -> {
                    if (esHoy && kpis != null) renderKPIsDelDia(kpis);
                    else if (kpisRango != null) renderKPIsRango(kpisRango);
                    renderAlertas(alertas);
                    renderChart(series);
                    renderChartHoras(horas);
                    tblTopProductos.setItems(FXCollections.observableArrayList(top));
                    tblUsuarios.setItems(FXCollections.observableArrayList(us));
                    renderValoracion(val);
                });
            } catch (Exception ex) {
                log.error("Error cargando dashboard", ex);
            }
        });
    }

    // ─── Render helpers ──────────────────────────────────────────────────────

    private void renderKPIsDelDia(KPIsDelDiaDTO k) {
        lblVendido.setText(cop(k.totalVendido()));
        BigDecimal pctAyer = k.porcentajeVsAyer();
        lblVendidoCambio.setText(formatPct(pctAyer) + " vs ayer");
        lblVendidoCambio.setStyle("-fx-text-fill: " +
                (pctAyer.compareTo(BigDecimal.ZERO) >= 0 ? "#A5D6A7" : "#FFCDD2") + ";");

        lblUtilidad.setText(cop(k.utilidadBruta()));
        BigDecimal margen = k.totalVendido().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                : k.utilidadBruta().divide(k.totalVendido(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP);
        lblMargen.setText("Margen " + margen + "%");

        lblVentas.setText(String.valueOf(k.cantidadVentas()));
        lblMejorVendedor.setText(k.mejorVendedor());
        lblTicket.setText(cop(k.ticketPromedio()));
        lblProductosVendidos.setText(k.cantidadProductosVendidos() + " productos");
    }

    private void renderKPIsRango(KPIsRangoDTO k) {
        lblVendido.setText(cop(k.totalVendido()));
        lblVendidoCambio.setText("");
        lblUtilidad.setText(cop(k.utilidadBruta()));
        lblMargen.setText("Margen " + k.margenPorcentaje() + "%");
        lblVentas.setText(String.valueOf(k.cantidadVentas()));
        lblMejorVendedor.setText("");
        lblTicket.setText(cop(k.ticketPromedio()));
        lblProductosVendidos.setText(k.cantidadProductosVendidos() + " productos");
    }

    private void renderAlertas(AlertasResumenDTO a) {
        contenedorAlertas.getChildren().clear();
        if (a.stockCritico() > 0)
            contenedorAlertas.getChildren().add(alertaLabel(
                    "🔴  " + a.stockCritico() + " producto(s) en stock crítico"));
        if (a.sinRotacion() > 0)
            contenedorAlertas.getChildren().add(alertaLabel(
                    "🟡  " + a.sinRotacion() + " producto(s) sin rotación (30d+)"));
        if (a.descuadresCaja() > 0)
            contenedorAlertas.getChildren().add(alertaLabel(
                    "🔴  " + a.descuadresCaja() + " descuadre(s) de caja en los últimos 30 días"));
        if (contenedorAlertas.getChildren().isEmpty())
            contenedorAlertas.getChildren().add(alertaLabel("✅  Sin alertas activas"));
    }

    private void renderChart(List<VentaDiariaDTO> series) {
        chartVentas.getData().clear();
        XYChart.Series<String, Number> vendidoS = new XYChart.Series<>();
        vendidoS.setName("Vendido");
        XYChart.Series<String, Number> utilidadS = new XYChart.Series<>();
        utilidadS.setName("Utilidad");

        for (VentaDiariaDTO d : series) {
            String fecha = d.fecha().format(CHART_FMT);
            vendidoS.getData().add(new XYChart.Data<>(fecha, d.totalVendido()));
            utilidadS.getData().add(new XYChart.Data<>(fecha, d.utilidad()));
        }
        chartVentas.getData().addAll(vendidoS, utilidadS);
    }

    private void renderChartHoras(List<VentaPorHoraDTO> horas) {
        chartHoras.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        for (VentaPorHoraDTO h : horas) {
            s.getData().add(new XYChart.Data<>(
                    String.format("%02d:00", h.hora()), h.cantidadVentas()));
        }
        chartHoras.getData().add(s);
    }

    private void renderValoracion(ValoracionInventarioDTO v) {
        lblValorCosto.setText(cop(v.valorACosto()));
        lblValorVenta.setText(cop(v.valorAPrecioVenta()));
        lblUtilPotencial.setText(cop(v.utilidadPotencial()));
        lblMargenInv.setText(v.margenPromedio() + "%");
    }

    // ─── Period resolution ───────────────────────────────────────────────────

    private LocalDate[] resolverRango() {
        LocalDate hoy = LocalDate.now();
        return switch (cbPeriodo.getValue()) {
            case "Hoy"            -> new LocalDate[]{hoy, hoy};
            case "Ayer"           -> new LocalDate[]{hoy.minusDays(1), hoy.minusDays(1)};
            case "Últimos 7 días" -> new LocalDate[]{hoy.minusDays(6), hoy};
            case "Este mes"       -> new LocalDate[]{hoy.withDayOfMonth(1), hoy};
            case "Mes anterior"   -> {
                LocalDate primerDiaMesAnt = hoy.minusMonths(1).withDayOfMonth(1);
                yield new LocalDate[]{primerDiaMesAnt, primerDiaMesAnt.plusMonths(1).minusDays(1)};
            }
            case "Este año"       -> new LocalDate[]{hoy.withDayOfYear(1), hoy};
            case "Personalizar"   -> {
                LocalDate d = dpDesde.getValue() != null ? dpDesde.getValue() : hoy.minusDays(29);
                LocalDate h = dpHasta.getValue() != null ? dpHasta.getValue() : hoy;
                yield new LocalDate[]{d, h};
            }
            default               -> new LocalDate[]{hoy.minusDays(29), hoy};
        };
    }

    private void configurarAutoRefresh() {
        autoRefresh = new Timeline(new KeyFrame(Duration.minutes(2), e -> cargarDatos()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    // ─── UI helpers ──────────────────────────────────────────────────────────

    private Label alertaLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        return lbl;
    }

    private <T> TableCell<T, BigDecimal> pctCell() {
        return new TableCell<>() {
            @Override protected void updateItem(BigDecimal v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.setScale(1, RoundingMode.HALF_UP) + "%");
            }
        };
    }

    private String cop(BigDecimal v) {
        return MoneyCOP.format(v);
    }

    private String formatPct(BigDecimal v) {
        if (v == null || v.compareTo(BigDecimal.ZERO) == 0) return "—";
        String sign = v.compareTo(BigDecimal.ZERO) > 0 ? "↑ +" : "↓ ";
        return sign + v.abs().setScale(1, RoundingMode.HALF_UP) + "%";
    }
}
