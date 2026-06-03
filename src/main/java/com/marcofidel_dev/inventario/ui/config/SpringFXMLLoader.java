package com.marcofidel_dev.inventario.ui.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * Carga un FXML y devuelve tanto el root como el controlador
     * @param fxmlPath ruta del archivo FXML
     * @return mapa con "root" y "controller"
     */
    public Map<String, Object> loadWithController(String fxmlPath) throws IOException {
        log.debug("Cargando FXML con controlador: {}", fxmlPath);

        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            throw new IOException("No se encontró el archivo FXML: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(context::getBean);

        Parent root = loader.load();
        
        Map<String, Object> result = new HashMap<>();
        result.put("root", root);
        result.put("controller", loader.getController());
        
        return result;
    }
}

