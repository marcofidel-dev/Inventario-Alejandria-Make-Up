package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.service.CompraService;
import com.marcofidel_dev.inventario.domain.entity.Compra;
import com.marcofidel_dev.inventario.ui.common.MoneyTableCell;
import com.marcofidel_dev.inventario.ui.config.SpringFXMLLoader;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ComprasController {

    private final CompraService compraService;
    private final ApplicationContext applicationContext;
    private final SpringFXMLLoader fxmlLoader;

    @FXML
    private DatePicker dpFechaInicio;
    @FXML
    private DatePicker dpFechaFin;

    @FXML
    private TableView<Compra> tblCompras;
    @FXML
    private TableColumn<Compra, Long> colId;
    @FXML
    private TableColumn<Compra, String> colProveedor;
    @FXML
    private TableColumn<Compra, LocalDate> colFecha;
    @FXML
    private TableColumn<Compra, BigDecimal> colTotal;

    @FXML
    public void initialize() {
        log.info("ComprasController inicializado");
        configurarTabla();
        cargarCompras();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalCosto"));
        colTotal.setCellFactory(MoneyTableCell.factory());
    }

    @FXML
    private void cargarCompras() {
        try {
            List<Compra> compras;

            LocalDate fechaInicio = dpFechaInicio.getValue();
            LocalDate fechaFin = dpFechaFin.getValue();

            if (fechaInicio != null && fechaFin != null) {
                compras = compraService.filtrarPorFechas(fechaInicio, fechaFin);
            } else {
                compras = compraService.listarTodos();
            }

            tblCompras.setItems(FXCollections.observableArrayList(compras));
            log.info("Compras cargadas: {}", compras.size());
        } catch (Exception e) {
            log.error("Error al cargar compras", e);
            mostrarError("Error al cargar compras: " + e.getMessage());
        }
    }

    @FXML
    private void nuevaCompra() {
        try {
            log.info("Abriendo diálogo de nueva compra");

            // Cargar el FXML del diálogo con el controlador
            var fxmlData = fxmlLoader.loadWithController("/fxml/nueva-compra.fxml");
            Parent root = (Parent) fxmlData.get("root");
            NuevaCompraController controller = (NuevaCompraController) fxmlData.get("controller");

            // Crear el stage del diálogo con tamaño específico
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Nueva Compra");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 950, 750);
            dialogStage.setScene(scene);
            dialogStage.setMinWidth(900);
            dialogStage.setMinHeight(700);
            dialogStage.setResizable(true);

            // Configurar el controlador
            controller.setDialogStage(dialogStage);

            // Mostrar y esperar
            dialogStage.showAndWait();

            // Si se guardó, recargar la tabla
            if (controller.isGuardado()) {
                cargarCompras();
            }

        } catch (Exception e) {
            log.error("Error al abrir diálogo de nueva compra", e);
            mostrarError("Error al abrir formulario: " + e.getMessage());
        }
    }

    @FXML
    private void editarCompra() {
        Compra seleccionada = tblCompras.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAdvertencia("Debe seleccionar una compra para editar");
            return;
        }

        try {
            log.info("Abriendo diálogo de editar compra ID: {}", seleccionada.getId());

            // Cargar el FXML del diálogo con el controlador
            var fxmlData = fxmlLoader.loadWithController("/fxml/nueva-compra.fxml");
            Parent root = (Parent) fxmlData.get("root");
            NuevaCompraController controller = (NuevaCompraController) fxmlData.get("controller");

            // Crear el stage del diálogo con tamaño específico
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Editar Compra");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 950, 750);
            dialogStage.setScene(scene);
            dialogStage.setMinWidth(900);
            dialogStage.setMinHeight(700);
            dialogStage.setResizable(true);

            // Configurar el controlador con los datos existentes
            controller.setDialogStage(dialogStage);
            controller.cargarCompra(seleccionada);

            // Mostrar y esperar
            dialogStage.showAndWait();

            // Si se guardó, recargar la tabla
            if (controller.isGuardado()) {
                cargarCompras();
            }

        } catch (Exception e) {
            log.error("Error al abrir diálogo de editar compra", e);
            mostrarError("Error al abrir formulario: " + e.getMessage());
        }
    }

    @FXML
    private void eliminarCompra() {
        Compra seleccionada = tblCompras.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAdvertencia("Debe seleccionar una compra para eliminar");
            return;
        }

        if (confirmar("¿Está seguro que desea eliminar esta compra? Nota: el stock ya fue incrementado.")) {
            try {
                compraService.eliminar(seleccionada.getId());
                mostrarInfo("Compra eliminada correctamente");
                cargarCompras();
                log.info("Compra eliminada: {}", seleccionada.getId());
            } catch (Exception e) {
                log.error("Error al eliminar compra", e);
                mostrarError("Error al eliminar: " + e.getMessage());
            }
        }
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
