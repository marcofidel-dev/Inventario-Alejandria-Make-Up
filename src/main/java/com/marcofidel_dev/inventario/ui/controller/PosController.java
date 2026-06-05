package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.dto.CustomerDTO;
import com.marcofidel_dev.inventario.application.dto.RegistrarVentaDTO;
import com.marcofidel_dev.inventario.application.dto.SaleItemInputDTO;
import com.marcofidel_dev.inventario.application.service.*;
import com.marcofidel_dev.inventario.domain.entity.Customer;
import com.marcofidel_dev.inventario.domain.entity.PaymentMethod;
import com.marcofidel_dev.inventario.domain.entity.Producto;
import com.marcofidel_dev.inventario.domain.entity.Sale;
import com.marcofidel_dev.inventario.ui.common.FormatUtils;
import com.marcofidel_dev.inventario.ui.config.SpringFXMLLoader;
import com.marcofidel_dev.inventario.ui.model.CarritoItem;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public class PosController {

    private final SaleService saleService;
    private final ProductoService productoService;
    private final CustomerService customerService;
    private final CashSessionService cashSessionService;
    private final ComprobanteService comprobanteService;
    private final SpringFXMLLoader fxmlLoader;

    // ── Root (for capturing keyboard shortcuts without polluting the scene) ─
    @FXML private BorderPane posRoot;

    // ── Search ───────────────────────────────────────────────────────────
    @FXML private TextField txtBuscar;
    @FXML private VBox vboxResultados;
    @FXML private ComboBox<Producto.TipoProducto> cmbTipoFiltro;
    @FXML private TextField txtMarcaFiltro;

    // ── Cart ─────────────────────────────────────────────────────────────
    @FXML private ListView<CarritoItem> lstCarrito;
    @FXML private Label lblSubtotal;
    @FXML private TextField txtDescuento;
    @FXML private Label lblDescuentoMonto;
    @FXML private Label lblTotal;

    // ── Payment ──────────────────────────────────────────────────────────
    @FXML private ComboBox<CustomerDTO> cmbCliente;
    @FXML private ToggleGroup tgMetodoPago;
    @FXML private RadioButton rbEfectivo;
    @FXML private RadioButton rbTarjeta;
    @FXML private RadioButton rbTransferencia;
    @FXML private RadioButton rbNequi;
    @FXML private RadioButton rbDaviplata;
    @FXML private TextField txtNotas;
    @FXML private Button btnCobrar;
    @FXML private Label lblError;
    @FXML private Label lblSesionInfo;

    // ── State ────────────────────────────────────────────────────────────
    final ObservableList<CarritoItem> carrito = FXCollections.observableArrayList();
    List<Producto> allProducts = new ArrayList<>();
    private PauseTransition searchDebounce;
    private Producto.TipoProducto currentTipoFilter;

    // ═══════════════════════════════════════════════════════════════════
    //  INITIALIZATION
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        allProducts = productoService.listarActivos();

        // 200ms debounce for live search
        searchDebounce = new PauseTransition(Duration.millis(200));
        searchDebounce.setOnFinished(e -> renderResultados(filtrarProductos()));

        setupCarrito();
        setupTipoFiltro();
        setupClienteCombo();
        setupDescuentoField();
        actualizarSesionInfo();
        setupKeyboardShortcuts();

        mostrarTodos();
        Platform.runLater(() -> txtBuscar.requestFocus());
    }

    private void setupCarrito() {
        lstCarrito.setItems(carrito);
        lstCarrito.setCellFactory(lv -> new CarritoItemCell());
        carrito.addListener((ListChangeListener<CarritoItem>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(item ->
                            item.cantidadProperty().addListener((obs, o, n) -> recalcularTotales()));
                }
            }
            recalcularTotales();
        });
        recalcularTotales();
    }

    private void setupTipoFiltro() {
        cmbTipoFiltro.setConverter(new StringConverter<>() {
            @Override
            public String toString(Producto.TipoProducto t) {
                if (t == null) return "Todos";
                String s = t.name();
                return s.charAt(0) + s.substring(1).toLowerCase(Locale.ROOT);
            }
            @Override public Producto.TipoProducto fromString(String s) { return null; }
        });
        cmbTipoFiltro.getItems().add(null);
        cmbTipoFiltro.getItems().addAll(Producto.TipoProducto.values());
        cmbTipoFiltro.setValue(null);
    }

    private void setupClienteCombo() {
        cmbCliente.setConverter(new StringConverter<>() {
            @Override public String toString(CustomerDTO c) { return c == null ? "Ocasional" : c.name(); }
            @Override public CustomerDTO fromString(String s) { return null; }
        });
        cargarClientes();
    }

    private void setupDescuentoField() {
        txtDescuento.textProperty().addListener((obs, old, nv) -> {
            if (!nv.matches("\\d*\\.?\\d*")) {
                txtDescuento.setText(old);
                return;
            }
            recalcularTotales();
        });
    }

    private void actualizarSesionInfo() {
        cashSessionService.getSesionActiva().ifPresentOrElse(
                s -> lblSesionInfo.setText("Sesión desde " +
                        s.getOpeningDate().format(DateTimeFormatter.ofPattern("hh:mm a"))),
                () -> lblSesionInfo.setText("Sin sesión activa")
        );
    }

    private void setupKeyboardShortcuts() {
        // Use addEventFilter on the root node (capture phase) so F2/F4/Esc work regardless
        // of which child has focus, and the handler is scoped to this POS instance only.
        // No scene-level handler → no leaks when POS is unloaded.
        posRoot.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case F2 -> { txtBuscar.requestFocus(); txtBuscar.selectAll(); event.consume(); }
                case F4 -> { cobrar(); event.consume(); }
                case ESCAPE -> { limpiarBusqueda(); event.consume(); }
                default -> { /* no-op */ }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SEARCH & FILTER
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    void onBuscarKeyReleased() {
        searchDebounce.playFromStart();
    }

    @FXML
    void limpiarBusqueda() {
        txtBuscar.clear();
        txtMarcaFiltro.clear();
        cmbTipoFiltro.setValue(null);
        currentTipoFilter = null;
        mostrarTodos();
        txtBuscar.requestFocus();
    }

    @FXML
    void aplicarFiltros() {
        currentTipoFilter = cmbTipoFiltro.getValue();
        renderResultados(filtrarProductos());
    }

    @FXML
    void mostrarTodos() {
        currentTipoFilter = null;
        cmbTipoFiltro.setValue(null);
        txtBuscar.clear();
        txtMarcaFiltro.clear();
        renderResultados(allProducts.stream().filter(Producto::getActivo).limit(20).toList());
    }

    @FXML void filtrarMaquillaje() { setTipoFilter(Producto.TipoProducto.MAQUILLAJE); }
    @FXML void filtrarBolsos()     { setTipoFilter(Producto.TipoProducto.BOLSO); }
    @FXML void filtrarBisuteria()  { setTipoFilter(Producto.TipoProducto.BISUTERIA); }

    private void setTipoFilter(Producto.TipoProducto tipo) {
        currentTipoFilter = tipo;
        cmbTipoFiltro.setValue(tipo);
        renderResultados(filtrarProductos());
    }

    private List<Producto> filtrarProductos() {
        String query = txtBuscar.getText().toLowerCase(Locale.ROOT).trim();
        String marcaQuery = txtMarcaFiltro.getText().toLowerCase(Locale.ROOT).trim();

        return allProducts.stream()
                .filter(Producto::getActivo)
                .filter(p -> currentTipoFilter == null || p.getTipo() == currentTipoFilter)
                .filter(p -> marcaQuery.isEmpty()
                        || (p.getMarca() != null && p.getMarca().toLowerCase(Locale.ROOT).contains(marcaQuery)))
                .filter(p -> query.isEmpty()
                        || p.getNombre().toLowerCase(Locale.ROOT).contains(query)
                        || (p.getMarca() != null && p.getMarca().toLowerCase(Locale.ROOT).contains(query))
                        || (p.getCodigoProducto() != null && p.getCodigoProducto().toLowerCase(Locale.ROOT).contains(query))
                        || (p.getTonoColor() != null && p.getTonoColor().toLowerCase(Locale.ROOT).contains(query)))
                .limit(20)
                .toList();
    }

    private void renderResultados(List<Producto> productos) {
        vboxResultados.getChildren().clear();

        if (productos.isEmpty()) {
            Label lbl = new Label("No se encontraron productos");
            lbl.setStyle("-fx-text-fill: #BBBBBB; -fx-font-style: italic; -fx-font-size: 12px;");
            vboxResultados.getChildren().add(lbl);
            return;
        }

        for (Producto p : productos) {
            vboxResultados.getChildren().add(buildProductCard(p));
        }
    }

    private HBox buildProductCard(Producto p) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 10, 8, 10));

        boolean hayStock = p.getStockActual() > 0;
        String baseStyle = "-fx-background-color: " + (hayStock ? "white" : "#FAFAFA") +
                "; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;" +
                (hayStock ? "" : " -fx-opacity: 0.65;");
        card.setStyle(baseStyle + " -fx-border-color: #F0F0F0;");

        // ── Info ──
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label lblNombre = new Label(p.getNombre());
        lblNombre.setStyle("-fx-font-weight: 700; -fx-font-size: 13px; -fx-text-fill: #333;");
        lblNombre.setWrapText(true);

        StringBuilder det = new StringBuilder();
        if (p.getMarca() != null && !p.getMarca().isBlank()) det.append(p.getMarca());
        if (p.getTonoColor() != null && !p.getTonoColor().isBlank()) {
            if (!det.isEmpty()) det.append("  •  ");
            det.append(p.getTonoColor());
        }
        if (p.getTipo() != null) {
            if (!det.isEmpty()) det.append("  •  ");
            String tn = p.getTipo().name();
            det.append(tn.charAt(0)).append(tn.substring(1).toLowerCase(Locale.ROOT));
        }
        Label lblDet = new Label(det.toString());
        lblDet.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

        String stockColor = p.getStockActual() <= 0 ? "#D32F2F"
                : (p.isStockBajo() ? "#E65100" : "#4CAF50");
        String stockTxt = p.getStockActual() <= 0 ? "Sin stock"
                : (p.isStockBajo() ? "Stock bajo: " + p.getStockActual() : "Stock: " + p.getStockActual());
        Label lblStock = new Label(stockTxt);
        lblStock.setStyle("-fx-font-size: 10px; -fx-text-fill: " + stockColor + ";");

        info.getChildren().addAll(lblNombre, lblDet, lblStock);

        // ── Price + add ──
        VBox right = new VBox(4);
        right.setAlignment(Pos.CENTER_RIGHT);

        Label lblPrecio = new Label(FormatUtils.money(p.getPrecioVenta()));
        lblPrecio.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #880E4F;");

        Button btnAdd = new Button("+");
        btnAdd.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-font-size: 16px; -fx-background-radius: 6; -fx-padding: 4 12; -fx-cursor: hand;");
        btnAdd.setDisable(!hayStock);
        btnAdd.setOnAction(e -> { agregarAlCarrito(p); e.consume(); });

        right.getChildren().addAll(lblPrecio, btnAdd);
        card.getChildren().addAll(info, right);

        if (hayStock) {
            card.setOnMouseClicked(e -> agregarAlCarrito(p));
            card.setOnMouseEntered(e -> card.setStyle(baseStyle + " -fx-border-color: #E91E63;"));
            card.setOnMouseExited(e -> card.setStyle(baseStyle + " -fx-border-color: #F0F0F0;"));
        }

        return card;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CART
    // ═══════════════════════════════════════════════════════════════════

    private void agregarAlCarrito(Producto producto) {
        for (CarritoItem item : carrito) {
            if (item.getProducto().getId().equals(producto.getId())) {
                if (item.getCantidad() >= producto.getStockActual()) {
                    mostrarError("Stock máximo disponible: " + producto.getStockActual()
                            + " — " + producto.getNombre());
                    return;
                }
                item.setCantidad(item.getCantidad() + 1);
                lstCarrito.refresh();
                recalcularTotales();
                return;
            }
        }
        carrito.add(new CarritoItem(producto, 1, producto.getPrecioVenta()));
    }

    @FXML
    void recalcularTotales() {
        BigDecimal subtotal = carrito.stream()
                .map(CarritoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal descPct = parseDescuento();
        BigDecimal descMonto = subtotal.multiply(descPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(descMonto).setScale(2, RoundingMode.HALF_UP);

        lblSubtotal.setText(FormatUtils.money(subtotal));
        lblDescuentoMonto.setText("-" + FormatUtils.money(descMonto));
        lblTotal.setText(FormatUtils.money(total));
        btnCobrar.setText("COBRAR  " + FormatUtils.money(total));
        btnCobrar.setDisable(carrito.isEmpty());
    }

    @FXML
    void limpiarCarrito() {
        if (carrito.isEmpty()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Vaciar carrito");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Vaciar el carrito? Los items sin cobrar se perderán.");
        confirm.showAndWait().filter(r -> r == ButtonType.OK).ifPresent(r -> {
            carrito.clear();
            lblError.setText("");
        });
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CLIENTE RÁPIDO
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    void agregarCliente() {
        try {
            Map<String, Object> data = fxmlLoader.loadWithController("/fxml/clientes-quick.fxml");
            Parent root = (Parent) data.get("root");
            ClienteQuickController ctrl = (ClienteQuickController) data.get("controller");

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnCobrar.getScene().getWindow());
            stage.setTitle("Nuevo Cliente");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            ctrl.setStage(stage);
            ctrl.setOnClienteCreado((Customer c) -> Platform.runLater(() -> {
                CustomerDTO dto = customerService.toDTO(c);
                cmbCliente.getItems().add(dto);
                cmbCliente.setValue(dto);
            }));

            stage.showAndWait();
        } catch (Exception e) {
            log.error("Error al abrir nuevo cliente", e);
            mostrarError("No se pudo abrir el formulario de cliente.");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  COBRO
    // ═══════════════════════════════════════════════════════════════════

    @FXML
    void cobrar() {
        if (carrito.isEmpty()) {
            mostrarError("Agrega al menos un producto al carrito.");
            return;
        }
        lblError.setText("");

        PaymentMethod metodo = getMetodoPago();
        BigDecimal total = calcularTotal();

        try {
            Map<String, Object> data = fxmlLoader.loadWithController("/fxml/cobro-modal.fxml");
            Parent root = (Parent) data.get("root");
            CobroModalController ctrl = (CobroModalController) data.get("controller");

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnCobrar.getScene().getWindow());
            stage.setTitle("Confirmar Venta");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            ctrl.setStage(stage);
            ctrl.setOnConfirmado(efectivoRecibido ->
                    Platform.runLater(() -> procesarVenta(metodo, efectivoRecibido)));

            ctrl.init(total, metodo);
            stage.showAndWait();

        } catch (Exception e) {
            log.error("Error al abrir modal de cobro", e);
            mostrarError("Error al procesar cobro: " + e.getMessage());
        }
    }

    private void procesarVenta(PaymentMethod metodo, BigDecimal efectivoRecibido) {
        try {
            List<SaleItemInputDTO> items = carrito.stream()
                    .map(c -> new SaleItemInputDTO(
                            c.getProducto().getId(),
                            c.getCantidad(),
                            c.getPrecioUnitario()))
                    .toList();

            CustomerDTO clienteDto = cmbCliente.getValue();
            String notas = txtNotas.getText().trim();

            RegistrarVentaDTO dto = new RegistrarVentaDTO(
                    items,
                    clienteDto != null ? clienteDto.id() : null,
                    metodo,
                    parseDescuento(),
                    notas.isEmpty() ? null : notas
            );

            Sale sale = saleService.registrarVenta(dto);
            log.info("Venta registrada: id={} total={}", sale.getId(), sale.getTotal());

            mostrarResultadoVenta(sale, metodo, efectivoRecibido);

        } catch (Exception e) {
            log.error("Error al registrar venta", e);
            mostrarError(e.getMessage() != null ? e.getMessage() : "Error al registrar la venta.");
        }
    }

    private void mostrarResultadoVenta(Sale sale, PaymentMethod metodo, BigDecimal efectivoRecibido) {
        String cuerpo = "Total: " + FormatUtils.money(sale.getTotal());
        if (metodo == PaymentMethod.EFECTIVO && efectivoRecibido != null) {
            BigDecimal cambio = efectivoRecibido.subtract(sale.getTotal()).setScale(2, RoundingMode.HALF_UP);
            if (cambio.compareTo(BigDecimal.ZERO) >= 0) {
                cuerpo += "\nCambio: " + FormatUtils.money(cambio);
            }
        }

        ButtonType btnPdf = new ButtonType("Guardar PDF");
        ButtonType btnNueva = new ButtonType("Nueva Venta");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Venta Registrada");
        alert.setHeaderText("✓  Venta N° " + String.format("%05d", sale.getId()) + " registrada");
        alert.setContentText(cuerpo);
        alert.getButtonTypes().setAll(btnPdf, btnNueva);

        alert.showAndWait().ifPresent(result -> {
            if (result == btnPdf) generarPdf(sale.getId(), efectivoRecibido);
        });

        resetPos();

        // Refresh product stock display
        allProducts = productoService.listarActivos();
        mostrarTodos();
    }

    private void generarPdf(Long ventaId, BigDecimal efectivoRecibido) {
        try {
            byte[] pdf = comprobanteService.generarPDF(ventaId,
                    efectivoRecibido != null ? efectivoRecibido : BigDecimal.ZERO);

            File tmp = File.createTempFile("comprobante_" + ventaId + "_", ".pdf");
            try (FileOutputStream fos = new FileOutputStream(tmp)) {
                fos.write(pdf);
            }
            java.awt.Desktop.getDesktop().open(tmp);

        } catch (Exception e) {
            log.error("Error generando PDF para venta {}", ventaId, e);
            new Alert(Alert.AlertType.ERROR, "No se pudo generar el PDF: " + e.getMessage()).showAndWait();
        }
    }

    private void resetPos() {
        carrito.clear();
        txtDescuento.setText("0");
        txtNotas.clear();
        cmbCliente.setValue(null);
        rbEfectivo.setSelected(true);
        lblError.setText("");
        Platform.runLater(() -> txtBuscar.requestFocus());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════

    private PaymentMethod getMetodoPago() {
        RadioButton sel = (RadioButton) tgMetodoPago.getSelectedToggle();
        if (sel == rbTarjeta)       return PaymentMethod.TARJETA;
        if (sel == rbTransferencia) return PaymentMethod.TRANSFERENCIA;
        if (sel == rbNequi)         return PaymentMethod.NEQUI;
        if (sel == rbDaviplata)     return PaymentMethod.DAVIPLATA;
        return PaymentMethod.EFECTIVO;
    }

    private BigDecimal calcularTotal() {
        BigDecimal sub = carrito.stream().map(CarritoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal desc = sub.multiply(parseDescuento())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return sub.subtract(desc).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal parseDescuento() {
        try {
            String t = txtDescuento.getText().trim();
            return t.isEmpty() ? BigDecimal.ZERO : new BigDecimal(t).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private void cargarClientes() {
        List<CustomerDTO> clientes = customerService.buscarPorNombreOTelefono("");
        cmbCliente.getItems().setAll(clientes);
    }

    private void mostrarError(String msg) {
        lblError.setText(msg);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  INNER CLASS: Cart cell
    // ═══════════════════════════════════════════════════════════════════

    private class CarritoItemCell extends ListCell<CarritoItem> {

        private final HBox root;
        private final Label lblNombre;
        private final Label lblCantidad;
        private final Label lblSubtotalItem;
        private final Button btnMenos;
        private final Button btnMas;
        private final Button btnEliminar;

        CarritoItemCell() {
            lblNombre = new Label();
            lblNombre.setStyle("-fx-font-size: 12px; -fx-text-fill: #333; -fx-wrap-text: true;");
            lblNombre.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lblNombre, Priority.ALWAYS);

            btnMenos = new Button("−");
            btnMenos.setStyle("-fx-background-color: #EEEEEE; -fx-background-radius: 4; " +
                    "-fx-padding: 3 8; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: bold;");

            lblCantidad = new Label();
            lblCantidad.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
            lblCantidad.setMinWidth(24);
            lblCantidad.setAlignment(Pos.CENTER);

            btnMas = new Button("+");
            btnMas.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white; -fx-background-radius: 4; " +
                    "-fx-padding: 3 8; -fx-cursor: hand; -fx-font-size: 14px; -fx-font-weight: bold;");

            lblSubtotalItem = new Label();
            lblSubtotalItem.setStyle("-fx-font-weight: bold; -fx-text-fill: #880E4F; -fx-font-size: 12px;");
            lblSubtotalItem.setMinWidth(65);
            lblSubtotalItem.setAlignment(Pos.CENTER_RIGHT);

            btnEliminar = new Button("✕");
            btnEliminar.setStyle("-fx-background-color: transparent; -fx-text-fill: #D32F2F; " +
                    "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 3 5;");

            root = new HBox(6);
            root.setAlignment(Pos.CENTER_LEFT);
            root.setPadding(new Insets(5, 6, 5, 6));
            root.getChildren().addAll(lblNombre, btnMenos, lblCantidad, btnMas, lblSubtotalItem, btnEliminar);

            btnMenos.setOnAction(e -> {
                CarritoItem item = getItem();
                if (item == null) return;
                if (item.getCantidad() <= 1) {
                    carrito.remove(item);
                } else {
                    item.setCantidad(item.getCantidad() - 1);
                    actualizarCelda(item);
                    recalcularTotales();
                }
            });

            btnMas.setOnAction(e -> {
                CarritoItem item = getItem();
                if (item == null) return;
                int maxStock = item.getProducto().getStockActual();
                if (item.getCantidad() >= maxStock) {
                    mostrarError("Stock máximo disponible: " + maxStock + " — " + item.getProducto().getNombre());
                    return;
                }
                item.setCantidad(item.getCantidad() + 1);
                actualizarCelda(item);
                recalcularTotales();
            });

            btnEliminar.setOnAction(e -> {
                CarritoItem item = getItem();
                if (item != null) carrito.remove(item);
            });
        }

        private void actualizarCelda(CarritoItem item) {
            lblNombre.setText(item.getProducto().getNombre());
            lblCantidad.setText(String.valueOf(item.getCantidad()));
            lblSubtotalItem.setText(FormatUtils.money(item.getSubtotal()));
        }

        @Override
        protected void updateItem(CarritoItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                actualizarCelda(item);
                setGraphic(root);
            }
        }
    }
}
