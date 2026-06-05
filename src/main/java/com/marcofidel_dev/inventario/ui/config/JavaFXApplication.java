package com.marcofidel_dev.inventario.ui.config;

import com.marcofidel_dev.inventario.ui.controller.LoginController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

@Slf4j
public class JavaFXApplication extends Application {

    private ConfigurableApplicationContext context;
    private Parent loginRoot;
    private LoginController loginController;

    @Override
    public void init() throws Exception {
        log.info("Inicializando Spring Application Context...");

        String[] args = getParameters().getRaw().toArray(new String[0]);
        this.context = new SpringApplicationBuilder()
                .sources(com.marcofidel_dev.inventario.InventarioApplication.class)
                .run(args);

        SpringFXMLLoader fxmlLoader = context.getBean(SpringFXMLLoader.class);
        Map<String, Object> fxmlData = fxmlLoader.loadWithController("/fxml/login.fxml");
        this.loginRoot = (Parent) fxmlData.get("root");
        this.loginController = (LoginController) fxmlData.get("controller");
    }

    @Override
    public void start(Stage primaryStage) {
        log.info("Mostrando ventana de inicio de sesión...");

        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/logo-tienda.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            log.warn("No se pudo cargar el ícono: {}", e.getMessage());
        }

        loginController.setLoginStage(primaryStage);

        primaryStage.setTitle("Sistema de Inventario - Alejandria Make-Up");
        primaryStage.setScene(new Scene(loginRoot, 420, 480));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        log.info("Cerrando aplicación...");
        context.close();
        Platform.exit();
    }
}
