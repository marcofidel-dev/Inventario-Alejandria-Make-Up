package com.marcofidel_dev.inventario.ui.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.marcofidel_dev.inventario.application.service.DashboardService;
import com.marcofidel_dev.inventario.domain.entity.Producto;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    @FXML
    private Label lblTotalProductos;
    @FXML
    private Label lblProductosStockBajo;

    @FXML
    private TableView<Producto> tblStockBajo;
    @FXML
    private TableColumn<Producto, String> colProductoNombre;
    @FXML
    private TableColumn<Producto, String> colProductoTipo;
    @FXML
    private TableColumn<Producto, Integer> colProductoStock;
    @FXML
    private TableColumn<Producto, Integer> colProductoStockMin;

    @FXML
    public void initialize() {
        log.info("DashboardController inicializado");
        configurarTablas();
        cargarDatos();
    }

    private void configurarTablas() {
        // Tabla de stock bajo
        colProductoNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProductoTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colProductoStock.setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colProductoStockMin.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
    }

    @FXML
    private void cargarDatos() {
        try {
            Map<String, Object> datos = dashboardService.obtenerDatosDashboard();

            // Actualizar contadores
            lblTotalProductos.setText(datos.get("totalProductos").toString());
            lblProductosStockBajo.setText(datos.get("productosStockBajo").toString());

            // Actualizar tablas
            @SuppressWarnings("unchecked")
            List<Producto> productosStockBajo = (List<Producto>) datos.get("listaProductosStockBajo");
            tblStockBajo.setItems(FXCollections.observableArrayList(productosStockBajo));

            log.info("Dashboard actualizado exitosamente");
        } catch (Exception e) {
            log.error("Error al cargar datos del dashboard", e);
        }
    }
}
