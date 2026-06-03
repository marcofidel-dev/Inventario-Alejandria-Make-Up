# Corrección del Tamaño de la Ventana de Compras

## Resumen de Cambios
Se han implementado correcciones significativas para resolver los problemas de tamaño en la ventana de compras del sistema de inventario.

## Problemas Identificados

### 1. **Falta de Tamaño Predefinido en el Stage**
- El controlador `ComprasController` no estaba estableciendo un tamaño específico al crear el `Stage` del diálogo
- Se usaba `new Scene(root)` sin especificar ancho y alto
- Esto causaba que las ventanas se abrieran con tamaño mínimo o inconsistente

### 2. **Falta de Preferencias de Tamaño en el FXML**
- El archivo `nueva-compra.fxml` solo tenía `minWidth` y `minHeight`, pero no `prefWidth` y `prefHeight`
- Faltaba una `ScrollPane` para mejor manejo del contenido en ventanas más pequeñas
- La tabla de items no tenía un alto mínimo definido

### 3. **Falta de Restricciones Visuales en la Tabla Principal**
- El archivo `compras.fxml` no tenía restricciones de altura mínima en la tabla
- La tabla principal podía colapsar si la ventana era redimensionada

## Soluciones Implementadas

### 1. **Mejoras en `nueva-compra.fxml`**

#### Cambios Principales:
```xml
<!-- ANTES: Sin ScrollPane, solo VBox -->
<VBox minWidth="900" minHeight="700">
    ...
</VBox>

<!-- DESPUÉS: Con ScrollPane envuelto -->
<ScrollPane fitToWidth="true" fitToHeight="false">
    <VBox minWidth="900" prefWidth="950" minHeight="700" prefHeight="750">
        ...
    </VBox>
</ScrollPane>
```

#### Beneficios:
- **ScrollPane**: Permite desplazamiento vertical cuando el contenido excede la ventana
- **prefWidth y prefHeight**: Establecen tamaños preferidos (950x750) que se aplican por defecto
- **minWidth y minHeight**: Mantienen limites mínimos (900x700) para no permitir ventanas muy pequeñas
- **TableView con minHeight**: La tabla ahora tiene un alto mínimo de 250px garantizado
- **VBox.vgrow="ALWAYS"**: La tabla se expande con la ventana

### 2. **Mejoras en `ComprasController.java`**

#### Método `nuevaCompra()`:
```java
// ANTES
Scene scene = new Scene(root);
dialogStage.setScene(scene);

// DESPUÉS
Scene scene = new Scene(root, 950, 750);
dialogStage.setScene(scene);
dialogStage.setMinWidth(900);
dialogStage.setMinHeight(700);
dialogStage.setResizable(true);
```

#### Método `editarCompra()`:
Se aplicaron los mismos cambios para consistencia.

#### Beneficios:
- **Tamaño inicial consistente**: Todas las ventanas de compra se abren con 950x750 píxeles
- **Restricciones de redimensionamiento**: No se pueden hacer ventanas menores a 900x700
- **Resizable: true**: Permite que el usuario ajuste el tamaño según lo necesite

### 3. **Mejoras en `compras.fxml`**

```xml
<!-- ANTES -->
<VBox spacing="10" VBox.vgrow="ALWAYS" style="...">
    <TableView fx:id="tblCompras" VBox.vgrow="ALWAYS" style="...">

<!-- DESPUÉS -->
<VBox spacing="10" VBox.vgrow="ALWAYS" style="...-fx-min-height: 400;">
    <TableView fx:id="tblCompras" VBox.vgrow="ALWAYS" style="...-fx-min-height: 350;">
```

#### Beneficios:
- **VBox con min-height: 400**: El contenedor de tabla tiene altura mínima
- **TableView con min-height: 350**: La tabla siempre es visible con altura mínima

## Opciones Evaluadas

### Opción 1: **Solo FXML (NO RECOMENDADA)**
- Establecer tamaño fijo en FXML no es flexible
- No permite al usuario adaptar la ventana a su pantalla
- Problema: Pantallas pequeñas no tenían espacio suficiente

### Opción 2: **Solo Java Controller (PARCIAL)**
- Establecer tamaño en el controlador sin cambiar FXML
- Funciona pero no optimiza el layout interno
- Problema: Contenido puede no distribuirse bien

### Opción 3: **Combinado FXML + Java (RECOMENDADO - IMPLEMENTADO)**
- FXML establece preferencias y mínimos internos
- Java Controller establece tamaño inicial y restricciones del Stage
- ScrollPane maneja contenido que excede la ventana
- **Ventajas:**
  - Flexibilidad máxima
  - Mejor distribución del contenido
  - Respeta límites visuales
  - Compatible con diferentes tamaños de pantalla

### Opción 4: **Responsive Design (FUTURA)**
- Usar CSS para ajustar tamaños según pantalla
- Crear estilos específicos para desktop/tablet
- Estado: FUTURA MEJORA

## Archivos Modificados

1. **`src/main/resources/fxml/nueva-compra.fxml`**
   - Envuelto con ScrollPane
   - Agregados prefWidth y prefHeight
   - Agregados minHeight en tabla

2. **`src/main/resources/fxml/compras.fxml`**
   - Agregados minHeight en tabla principal
   - Agregados minHeight en contenedor VBox

3. **`src/main/java/com/marcofidel_dev/inventario/ui/controller/ComprasController.java`**
   - Modificado método `nuevaCompra()`
   - Modificado método `editarCompra()`
   - Establecimiento de tamaño específico del Stage

## Dimensiones Finales

| Componente | Ancho | Alto | Tipo |
|-----------|-------|------|------|
| Ventana Nueva Compra | 950px | 750px | Preferido |
| Mínimo Nueva Compra | 900px | 700px | Mínimo |
| Tabla en Nueva Compra | - | 250px | Mínimo |
| Tabla Principal | - | 350px | Mínimo |
| Contenedor Tabla Principal | - | 400px | Mínimo |

## Pruebas Realizadas

### ✓ Compilación
- El proyecto compila sin errores
- Sin warnings de FXML

### ✓ Cambios de Tamaño
- La ventana puede redimensionarse
- No se puede hacer más pequeña que 900x700
- El contenido se distribuye correctamente

### ✓ ScrollPane
- Aparece automáticamente si el contenido excede el tamaño
- Desaparece cuando hay espacio disponible

## Recomendaciones Futuras

1. **CSS Theming**: Crear estilos reutilizables para tamaños de componentes
2. **Responsive Layout**: Ajustar tamaños según resolución de pantalla
3. **Persistencia de Tamaño**: Guardar las dimensiones de ventana del usuario
4. **Testing**: Implementar pruebas de UI para diferentes resoluciones
5. **Zoom**: Permitir zoom en la interfaz para accesibilidad

## Conclusión

Los cambios implementados resuelven los problemas de tamaño de ventana mediante una combinación de:
- Definiciones de tamaño en FXML (preferencias)
- Configuración del Stage en Java (control)
- Uso de ScrollPane (flexibilidad)

Esta solución proporciona una experiencia de usuario mejorada manteniendo la flexibilidad y permitiendo que el usuario ajuste los tamaños según sea necesario.

