package com.marcofidel_dev.inventario.ui.config;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public class JavaFXApplication extends Application {

    private ConfigurableApplicationContext context;
    private Parent rootNode;

    @Override
    public void init() throws Exception {
        log.info("Inicializando Spring Application Context...");

        // Inicializar Spring Boot sin servidor web
        String[] args = getParameters().getRaw().toArray(new String[0]);
        this.context = new SpringApplicationBuilder()
                .sources(com.marcofidel_dev.inventario.InventarioApplication.class)
                .run(args);

        // Cargar la vista principal
        SpringFXMLLoader fxmlLoader = context.getBean(SpringFXMLLoader.class);
        this.rootNode = fxmlLoader.load("/fxml/main.fxml");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        log.info("Iniciando aplicación JavaFX...");

        // Establecer el ícono de la aplicación
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/logo-tienda.png"));
            primaryStage.getIcons().add(icon);
            log.info("Ícono de la aplicación cargado exitosamente");
        } catch (Exception e) {
            log.warn("No se pudo cargar el ícono de la aplicación: {}", e.getMessage());
        }

        primaryStage.setTitle("Sistema de Inventario - Alejandria Make-Up");
        primaryStage.setScene(new Scene(rootNode, 1200, 700));
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);

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

