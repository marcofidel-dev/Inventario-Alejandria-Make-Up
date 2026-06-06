package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.service.CompraService;
import com.marcofidel_dev.inventario.application.service.ProductoService;
import com.marcofidel_dev.inventario.shared.money.InvalidMoneyFormatException;
import com.marcofidel_dev.inventario.shared.money.MoneyCOP;
import com.marcofidel_dev.inventario.ui.common.MoneyTableCell;
import com.marcofidel_dev.inventario.domain.entity.Compra;
import com.marcofidel_dev.inventario.domain.entity.CompraItem;
import com.marcofidel_dev.inventario.domain.entity.Producto;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NuevaCompraController {

    private final CompraService compraService;
    private final ProductoService productoService;

    @FXML
    private TextField tfProveedor;
    @FXML
    private DatePicker dpFecha;

    @FXML
    private ComboBox<Producto> cmbProducto;
    @FXML
    private TextField tfCantidad;
    @FXML
    private TextField tfCostoUnitario;

    @FXML
    private TableView<CompraItem> tblItems;
    @FXML
    private TableColumn<CompraItem, String> colProducto;
    @FXML
    private TableColumn<CompraItem, Integer> colCantidad;
    @FXML
    private TableColumn<CompraItem, BigDecimal> colCostoUnitario;
    @FXML
    private TableColumn<CompraItem, BigDecimal> colSubtotal;

    @FXML
    private Label lblTotal;

    private Stage dialogStage;
    private Compra compra;
    private boolean guardado = false;

    @FXML
    public void initialize() {
        log.info("NuevaCompraController inicializado");
        configurarTabla();
        cargarProductos();
        
        // Agregar listener para recargar la lista de productos justo antes de que el combobox se despliegue.
        // Esto permite ver cualquier producto creado o modificar recientemente sin cerrar el formulario.
        cmbProducto.setOnShowing(event -> cargarProductos());
        
        dpFecha.setValue(LocalDate.now());
        compra = new Compra();
    }

    private void configurarTabla() {
        colProducto.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getProducto().getNombre()
            )
        );
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        colCostoUnitario.setCellValueFactory(new PropertyValueFactory<>("costoUnitario"));
        colCostoUnitario.setCellFactory(MoneyTableCell.factory());

        colSubtotal.setCellFactory(col -> new TableCell<CompraItem, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                CompraItem compraItem = getTableRow().getItem();
                if (compraItem != null) {
                    setText(MoneyCOP.format(MoneyCOP.multiply(
                            compraItem.getCostoUnitario(), compraItem.getCantidad())));
                }
            }
        });
    }

    private void cargarProductos() {
        try {
            // Cargar solo los productos activos para que coincida con el panel principal
            List<Producto> productos = productoService.listarActivos();

            // Guardar el valor seleccionado actual para restablecerlo luego de recargar
            Producto seleccionado = cmbProducto.getValue();

            cmbProducto.setItems(FXCollections.observableArrayList(productos));
            
            // Restaurar la selección si el producto sigue estando activo
            if (seleccionado != null && productos.stream().anyMatch(p -> p.getId().equals(seleccionado.getId()))) {
                cmbProducto.setValue(seleccionado);
            }
            
            log.info("Productos cargados: {}", productos.size());
        } catch (Exception e) {
            log.error("Error al cargar productos", e);
            mostrarError("Error al cargar productos: " + e.getMessage());
        }
    }

    @FXML
    private void crearNuevoProducto() {
        Dialog<Producto> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Producto Rápido");
        dialog.setHeaderText("Ingrese los datos básicos del nuevo producto:");
        // No es necesario stage.initOwner() si usamos dialog estándar de JavaFX

        ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del producto...");
        
        ComboBox<Producto.TipoProducto> cmbTipo = new ComboBox<>(FXCollections.observableArrayList(Producto.TipoProducto.values()));
        cmbTipo.setPromptText("Seleccione...");

        TextField txtCosto = new TextField();
        txtCosto.setPromptText("0.0");

        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("0.0");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(new Label("Tipo:"), 0, 1);
        grid.add(cmbTipo, 1, 1);
        grid.add(new Label("Costo Unit.:"), 0, 2);
        grid.add(txtCosto, 1, 2);
        grid.add(new Label("Precio Venta:"), 0, 3);
        grid.add(txtPrecio, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == guardarButtonType) {
                try {
                    Producto p = new Producto();
                    p.setNombre(txtNombre.getText().trim());
                    p.setTipo(cmbTipo.getValue());
                    p.setCosto(MoneyCOP.parse(txtCosto.getText()));
                    p.setPrecioVenta(MoneyCOP.parse(txtPrecio.getText()));
                    p.setStockActual(0);
                    p.setStockMinimo(0);
                    p.setActivo(true);
                    return p;
                } catch (Exception e) {
                    mostrarError("Datos inválidos. Por favor, asegúrese de ingresar números válidos en los costos.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(producto -> {
            try {
                if(producto.getNombre().isEmpty() || producto.getTipo() == null) {
                    mostrarAdvertencia("El nombre y el tipo son obligatorios para crear el producto.");
                    return;
                }
                Producto guardado = productoService.guardar(producto);
                cargarProductos();
                cmbProducto.setValue(guardado);
                mostrarInfo("Producto creado exitosamente.");
            } catch (Exception e) {
                mostrarError("Ocurrió un error al guardar: " + e.getMessage());
            }
        });
    }

    @FXML
    private void agregarItem() {
        try {
            if (cmbProducto.getValue() == null) {
                mostrarAdvertencia("Debe seleccionar un producto");
                return;
            }

            if (tfCantidad.getText().isEmpty()) {
                mostrarAdvertencia("Debe ingresar la cantidad");
                return;
            }

            if (tfCostoUnitario.getText().isEmpty()) {
                mostrarAdvertencia("Debe ingresar el costo unitario");
                return;
            }

            Producto producto = cmbProducto.getValue();
            Integer cantidad = Integer.parseInt(tfCantidad.getText());
            BigDecimal costoUnitario = MoneyCOP.parse(tfCostoUnitario.getText());

            if (cantidad <= 0) {
                mostrarAdvertencia("La cantidad debe ser mayor a 0");
                return;
            }

            if (costoUnitario.compareTo(BigDecimal.ZERO) < 0) {
                mostrarAdvertencia("El costo unitario no puede ser negativo");
                return;
            }

            // Verificar si el producto ya existe en la tabla
            Optional<CompraItem> existente = compra.getItems().stream()
                    .filter(item -> item.getProducto().getId().equals(producto.getId()))
                    .findFirst();

            if (existente.isPresent()) {
                CompraItem item = existente.get();
                item.setCantidad(item.getCantidad() + cantidad);
                log.info("Cantidad incrementada para producto: {}", producto.getNombre());
            } else {
                CompraItem item = new CompraItem(producto, cantidad, costoUnitario);
                compra.addItem(item);
                log.info("Item agregado: {} - Cantidad: {}", producto.getNombre(), cantidad);
            }

            actualizarTabla();
            limpiarCamposProducto();

        } catch (NumberFormatException e) {
            mostrarError("Error en el formato de los números: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al agregar item", e);
            mostrarError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void quitarItem() {
        CompraItem seleccionado = tblItems.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Debe seleccionar un item para eliminar");
            return;
        }

        if (confirmar("¿Eliminar este item de la compra?")) {
            compra.removeItem(seleccionado);
            actualizarTabla();
            log.info("Item eliminado: {}", seleccionado.getProducto().getNombre());
        }
    }

    private void actualizarTabla() {
        tblItems.setItems(FXCollections.observableArrayList(compra.getItems()));
        compra.recalcularTotal();
        actualizarTotal();
    }

    private void actualizarTotal() {
        lblTotal.setText("Total: " + MoneyCOP.format(compra.getTotalCosto()));
    }

    private void limpiarCamposProducto() {
        tfCantidad.clear();
        tfCostoUnitario.clear();
        cmbProducto.setValue(null);
    }

    @FXML
    private void guardar() {
        try {
            if (tfProveedor.getText().trim().isEmpty()) {
                mostrarAdvertencia("Debe ingresar el nombre del proveedor");
                return;
            }

            if (compra.getItems().isEmpty()) {
                mostrarAdvertencia("Debe agregar al menos un producto a la compra");
                return;
            }

            compra.setProveedor(tfProveedor.getText().trim());
            compra.setFecha(dpFecha.getValue());

            Compra compraSaved = compraService.guardar(compra);
            log.info("Compra guardada con ID: {}", compraSaved.getId());

            guardado = true;
            mostrarInfo("Compra guardada exitosamente");
            dialogStage.close();

        } catch (Exception e) {
            log.error("Error al guardar compra", e);
            mostrarError("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        dialogStage.close();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void cargarCompra(Compra compraExistente) {
        this.compra = compraExistente;
        tfProveedor.setText(compra.getProveedor() != null ? compra.getProveedor() : "");
        dpFecha.setValue(compra.getFecha() != null ? compra.getFecha() : LocalDate.now());
        actualizarTabla();
        log.info("Compra cargada para edición: {}", compra.getId());
    }

    public boolean isGuardado() {
        return guardado;
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



