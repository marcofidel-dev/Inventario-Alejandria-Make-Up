package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.service.ProductoService;
import com.marcofidel_dev.inventario.domain.entity.Producto;
import com.marcofidel_dev.inventario.shared.money.InvalidMoneyFormatException;
import com.marcofidel_dev.inventario.shared.money.MoneyCOP;
import com.marcofidel_dev.inventario.ui.common.MoneyTableCell;
import com.marcofidel_dev.inventario.ui.common.UIPermissionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.ScrollPane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductosController {

    private final ProductoService productoService;
    private final UIPermissionService uiPermissionService;

    @FXML
    private TextField txtBuscar;
    @FXML
    private ComboBox<String> cmbFiltroTipo;
    @FXML
    private CheckBox chkStockBajo;

    @FXML
    private TableView<Producto> tblProductos;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, String> colTipo;
    @FXML
    private TableColumn<Producto, String> colMarca;
    @FXML
    private TableColumn<Producto, String> colSku;
    @FXML
    private TableColumn<Producto, BigDecimal> colCosto;
    @FXML
    private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML
    private TableColumn<Producto, Integer> colStock;
    @FXML
    private TableColumn<Producto, Integer> colStockMin;

    // Formulario
    @FXML
    private ComboBox<Producto.TipoProducto> cmbTipo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtMarca;
    @FXML
    private TextField txtSku;
    @FXML
    private TextField txtCosto;
    @FXML
    private TextField txtPrecioVenta;
    @FXML
    private TextField txtStockActual;
    @FXML
    private TextField txtStockMinimo;
    @FXML
    private TextField txtTonoColor;
    @FXML
    private CheckBox chkActivo;

    // Permission-controlled nodes (colCosto declared above with other columns)
    @FXML private Button btnNuevo;
    @FXML private ScrollPane pnlForm;

    private Producto productoSeleccionado;

    @FXML
    public void initialize() {
        log.info("ProductosController inicializado");
        configurarTabla();
        configurarFiltros();
        configurarFormatoPrecios();
        aplicarPermisos();
        cargarProductos();
    }

    private void aplicarPermisos() {
        boolean esAdmin = uiPermissionService.isAdmin();
        // TableColumn is not a Node — use setVisible directly
        colCosto.setVisible(esAdmin);
        // Buttons and panels are Nodes — delegate to UIPermissionService
        uiPermissionService.hideIfNotAdmin(btnNuevo);
        uiPermissionService.hideIfNotAdmin(pnlForm);
    }

    private void configurarFormatoPrecios() {
        attachMoneyFocusListener(txtCosto);
        attachMoneyFocusListener(txtPrecioVenta);
    }

    private void attachMoneyFocusListener(javafx.scene.control.TextField field) {
        field.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            String raw = field.getText();
            if (isNowFocused && !raw.isBlank()) {
                field.setText(raw.replace(".", ""));
                field.selectAll();
            } else if (!isNowFocused && !raw.isBlank()) {
                try {
                    BigDecimal value = MoneyCOP.parse(raw);
                    field.setText(value.signum() == 0 ? "" : MoneyCOP.formatPlain(value));
                } catch (InvalidMoneyFormatException e) {
                    // leave as-is; validation on submit
                }
            }
        });
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colSku.setCellValueFactory(new PropertyValueFactory<>("codigoProducto"));

        colCosto.setCellValueFactory(new PropertyValueFactory<>("costo"));
        colCosto.setCellFactory(MoneyTableCell.factory());

        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colPrecio.setCellFactory(MoneyTableCell.factory());

        colStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));

        // Resaltar productos con stock bajo
        tblProductos.setRowFactory(tv -> new TableRow<Producto>() {
            @Override
            protected void updateItem(Producto item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.isStockBajo()) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else {
                    setStyle("");
                }
            }
        });

        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                cargarFormulario(newSel);
            }
        });
    }

    private void configurarFiltros() {
        cmbFiltroTipo.setItems(FXCollections.observableArrayList("TODOS", "MAQUILLAJE", "BOLSO", "BISUTERIA"));
        cmbFiltroTipo.setValue("TODOS");

        cmbTipo.setItems(FXCollections.observableArrayList(Producto.TipoProducto.values()));
    }

    @FXML
    private void cargarProductos() {
        try {
            List<Producto> productos;

            if (chkStockBajo.isSelected()) {
                productos = productoService.listarConStockBajo();
            } else if (txtBuscar.getText() != null && !txtBuscar.getText().trim().isEmpty()) {
                productos = productoService.buscar(txtBuscar.getText());
            } else if (!"TODOS".equals(cmbFiltroTipo.getValue())) {
                Producto.TipoProducto tipo = Producto.TipoProducto.valueOf(cmbFiltroTipo.getValue());
                productos = productoService.filtrarPorTipo(tipo);
            } else {
                productos = productoService.listarActivos();
            }

            tblProductos.setItems(FXCollections.observableArrayList(productos));
            log.info("Productos cargados: {}", productos.size());
        } catch (Exception e) {
            log.error("Error al cargar productos", e);
            mostrarError("Error al cargar productos: " + e.getMessage());
        }
    }

    @FXML
    private void nuevo() {
        limpiarFormulario();
        productoSeleccionado = null;
    }

    @FXML
    private void guardar() {
        try {
            if (!validarFormulario()) {
                return;
            }

            Producto producto = productoSeleccionado != null ? productoSeleccionado : new Producto();

            producto.setTipo(cmbTipo.getValue());
            producto.setNombre(txtNombre.getText().trim());
            producto.setMarca(txtMarca.getText().trim().isEmpty() ? null : txtMarca.getText().trim());
            producto.setCodigoProducto(txtSku.getText().trim().isEmpty() ? null : txtSku.getText().trim());

            producto.setCosto(MoneyCOP.parse(txtCosto.getText()));
            producto.setPrecioVenta(MoneyCOP.parse(txtPrecioVenta.getText()));

            producto.setStockActual(Integer.parseInt(txtStockActual.getText()));
            producto.setStockMinimo(Integer.parseInt(txtStockMinimo.getText()));
            producto.setActivo(chkActivo.isSelected());

            producto.setTonoColor(txtTonoColor.getText().trim().isEmpty() ? null : txtTonoColor.getText().trim());

            productoService.guardar(producto);
            mostrarInfo("Producto guardado exitosamente");
            cargarProductos();
            limpiarFormulario();
            productoSeleccionado = null;

        } catch (Exception e) {
            log.error("Error al guardar producto", e);
            mostrarError("Error al guardar producto: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Debe seleccionar un producto");
            return;
        }

        if (confirmar("¿Está seguro de desactivar el producto: " + seleccionado.getNombre() + "?")) {
            try {
                productoService.eliminar(seleccionado.getId());
                mostrarInfo("Producto desactivado exitosamente");
                cargarProductos();
                limpiarFormulario();
            } catch (Exception e) {
                log.error("Error al eliminar producto", e);
                mostrarError("Error al eliminar producto: " + e.getMessage());
            }
        }
    }

    private void cargarFormulario(Producto producto) {
        productoSeleccionado = producto;

        cmbTipo.setValue(producto.getTipo());
        txtNombre.setText(producto.getNombre());
        txtMarca.setText(producto.getMarca() != null ? producto.getMarca() : "");
        txtSku.setText(producto.getCodigoProducto() != null ? producto.getCodigoProducto() : "");

        txtCosto.setText(MoneyCOP.formatPlain(producto.getCosto()));
        txtPrecioVenta.setText(MoneyCOP.formatPlain(producto.getPrecioVenta()));

        txtStockActual.setText(producto.getStockActual().toString());
        txtStockMinimo.setText(producto.getStockMinimo().toString());
        chkActivo.setSelected(producto.getActivo());

        txtTonoColor.setText(producto.getTonoColor() != null ? producto.getTonoColor() : "");
    }

    private void limpiarFormulario() {
        cmbTipo.setValue(null);
        txtNombre.clear();
        txtMarca.clear();
        txtSku.clear();
        txtCosto.clear();
        txtPrecioVenta.clear();
        txtStockActual.setText("0");
        txtStockMinimo.setText("0");
        chkActivo.setSelected(true);
        txtTonoColor.clear();
        tblProductos.getSelectionModel().clearSelection();
    }

    private boolean validarFormulario() {
        if (cmbTipo.getValue() == null) {
            mostrarAdvertencia("Debe seleccionar un tipo de producto");
            return false;
        }
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarAdvertencia("El nombre es obligatorio");
            return false;
        }
        try {
            MoneyCOP.parse(txtCosto.getText());
        } catch (InvalidMoneyFormatException e) {
            mostrarAdvertencia("Costo inválido: " + e.getMessage());
            return false;
        }
        try {
            MoneyCOP.parse(txtPrecioVenta.getText());
        } catch (InvalidMoneyFormatException e) {
            mostrarAdvertencia("Precio de venta inválido: " + e.getMessage());
            return false;
        }
        try {
            int stock = Integer.parseInt(txtStockActual.getText());
            if (stock < 0) {
                mostrarAdvertencia("El stock actual debe ser mayor o igual a 0");
                return false;
            }
        } catch (Exception e) {
            mostrarAdvertencia("El stock actual debe ser un número válido");
            return false;
        }
        try {
            int stockMin = Integer.parseInt(txtStockMinimo.getText());
            if (stockMin < 0) {
                mostrarAdvertencia("El stock mínimo debe ser mayor o igual a 0");
                return false;
            }
        } catch (Exception e) {
            mostrarAdvertencia("El stock mínimo debe ser un número válido");
            return false;
        }
        return true;
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private boolean confirmar(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}

