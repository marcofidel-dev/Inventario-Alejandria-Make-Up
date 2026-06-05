package com.marcofidel_dev.inventario.ui.controller;

import com.marcofidel_dev.inventario.application.dto.CreateUserDTO;
import com.marcofidel_dev.inventario.application.dto.UserDTO;
import com.marcofidel_dev.inventario.application.service.UserService;
import com.marcofidel_dev.inventario.domain.entity.Role;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserManagementController {

    private final UserService userService;

    @FXML private TableView<UserDTO> tblUsuarios;
    @FXML private TableColumn<UserDTO, Long>    colId;
    @FXML private TableColumn<UserDTO, String>  colUsername;
    @FXML private TableColumn<UserDTO, String>  colNombre;
    @FXML private TableColumn<UserDTO, Role>    colRol;
    @FXML private TableColumn<UserDTO, Boolean> colActivo;

    @FXML
    public void initialize() {
        log.info("UserManagementController inicializado");
        configurarTabla();
        cargarUsuarios();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("role"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("active"));
        colActivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Activo" : "Inactivo"));
                setStyle(empty || item == null ? "" : (item ? "-fx-text-fill: #2E7D32;" : "-fx-text-fill: #C62828;"));
            }
        });
    }

    private void cargarUsuarios() {
        try {
            List<UserDTO> usuarios = userService.listUsers();
            tblUsuarios.setItems(FXCollections.observableArrayList(usuarios));
        } catch (Exception e) {
            log.error("Error al cargar usuarios", e);
            mostrarError("Error al cargar usuarios: " + e.getMessage());
        }
    }

    @FXML
    private void nuevoUsuario() {
        Dialog<CreateUserDTO> dialog = crearDialogoNuevoUsuario();
        Optional<CreateUserDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            try {
                userService.createUser(dto);
                mostrarInfo("Usuario creado exitosamente. Deberá cambiar su contraseña al iniciar sesión.");
                cargarUsuarios();
            } catch (Exception e) {
                log.error("Error al crear usuario", e);
                mostrarError("Error al crear usuario: " + e.getMessage());
            }
        });
    }

    @FXML
    private void desactivarUsuario() {
        UserDTO seleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Seleccione un usuario.");
            return;
        }
        if (!confirmar("¿Desactivar el usuario '" + seleccionado.getUsername() + "'?")) return;
        try {
            userService.deactivateUser(seleccionado.getId());
            mostrarInfo("Usuario desactivado.");
            cargarUsuarios();
        } catch (Exception e) {
            log.error("Error al desactivar usuario", e);
            mostrarError("Error: " + e.getMessage());
        }
    }

    @FXML
    private void cambiarRol() {
        UserDTO seleccionado = tblUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Seleccione un usuario.");
            return;
        }

        Role nuevoRol = seleccionado.getRole() == Role.ADMIN ? Role.COLABORADOR : Role.ADMIN;
        if (!confirmar("¿Cambiar el rol de '" + seleccionado.getUsername() + "' a " + nuevoRol + "?")) return;

        try {
            userService.changeRole(seleccionado.getId(), nuevoRol);
            mostrarInfo("Rol cambiado a " + nuevoRol + ".");
            cargarUsuarios();
        } catch (Exception e) {
            log.error("Error al cambiar rol", e);
            mostrarError("Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Helper: dialog for creating a new user
    // -------------------------------------------------------

    private Dialog<CreateUserDTO> crearDialogoNuevoUsuario() {
        Dialog<CreateUserDTO> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Usuario");
        dialog.setHeaderText("Ingrese los datos del nuevo usuario");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        TextField txtUser = new TextField();
        txtUser.setPromptText("Nombre de usuario");
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre completo");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Contraseña temporal");
        ComboBox<Role> cmbRol = new ComboBox<>(FXCollections.observableArrayList(Role.values()));
        cmbRol.setValue(Role.COLABORADOR);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        grid.add(new Label("Usuario:"), 0, 0);   grid.add(txtUser, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);    grid.add(txtNombre, 1, 1);
        grid.add(new Label("Contraseña:"), 0, 2); grid.add(txtPass, 1, 2);
        grid.add(new Label("Rol:"), 0, 3);        grid.add(cmbRol, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt == btnGuardar) {
                CreateUserDTO dto = new CreateUserDTO();
                dto.setUsername(txtUser.getText().trim());
                dto.setFullName(txtNombre.getText().trim());
                dto.setPassword(txtPass.getText());
                dto.setRole(cmbRol.getValue());
                return dto;
            }
            return null;
        });

        return dialog;
    }

    // -------------------------------------------------------
    // Alert helpers
    // -------------------------------------------------------

    private void mostrarInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void mostrarAdvertencia(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void mostrarError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private boolean confirmar(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText(null); a.setContentText(msg);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
