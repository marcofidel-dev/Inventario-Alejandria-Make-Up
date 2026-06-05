package com.marcofidel_dev.inventario.ui.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Component
@Slf4j
public class SpringFXMLLoader {

    private final ApplicationContext context;

    public SpringFXMLLoader(ApplicationContext context) {
        this.context = context;
    }

    public Parent load(String fxmlPath) throws IOException {
        log.debug("Cargando FXML: {}", fxmlPath);

        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            throw new IOException("No se encontró el archivo FXML: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(context::getBean);

        return loader.load();
    }

    /** Loads an FXML and returns both the root node and the Spring-managed controller. */
    public java.util.Map<String, Object> loadWithController(String fxmlPath) throws IOException {
        log.debug("Cargando FXML con controller: {}", fxmlPath);

        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            throw new IOException("No se encontró el archivo FXML: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(context::getBean);
        Parent root = loader.load();

        return java.util.Map.of("root", root, "controller", loader.getController());
    }
}

