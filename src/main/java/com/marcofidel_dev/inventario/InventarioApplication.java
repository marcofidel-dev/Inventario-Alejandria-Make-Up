package com.marcofidel_dev.inventario;

import com.marcofidel_dev.inventario.ui.config.JavaFXApplication;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventarioApplication {

    public static void main(String[] args) {
        // Lanzar JavaFX en lugar de Spring Boot directamente
        Application.launch(JavaFXApplication.class, args);
    }

}
