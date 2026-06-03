# Guía Rápida - Verificar Cambios en Módulo de Compras

## 🚀 Inicio Rápido

### Paso 1: Validar Cambios
```bash
# Opción A: Ejecutar script (Windows)
validar-cambios.bat

# Opción B: Manualmente compilar
mvnw clean compile
```

### Paso 2: Ejecutar la Aplicación
```bash
# Opción A: Script (Windows)
run.bat

# Opción B: Manualmente
mvnw spring-boot:run
```

### Paso 3: Abrir Módulo de Compras
1. En la aplicación, selecciona "Compras" en el menú
2. Se abre la ventana "Gestión de Compras"

## ✅ Verificaciones Rápidas

### Verificación 1: Tamaño de Ventana
**Para: Nueva Compra**
```
1. Haz clic en botón "➕ Nueva"
2. La ventana debería abrirse con tamaño ~950x750
3. Intenta hacerla más pequeña que 900x700 - no debería permitir
4. La ventana debe ser redimensionable hacia arriba
```

**Esperado**: ✓ Ventana abre con tamaño consistente

### Verificación 2: ScrollPane
```
1. En ventana "Nueva Compra", intenta hacerla muy pequeña
2. Debería aparecer barra de desplazamiento vertical
3. Intenta hacerla grande - desaparece barra de desplazamiento
```

**Esperado**: ✓ ScrollPane aparece/desaparece dinámicamente

### Verificación 3: Tabla Visible
```
1. Abre "Nueva Compra"
2. La tabla "Productos en la Compra" siempre visible
3. Aunque hagas la ventana pequeña, mínimo 250px de alto
```

**Esperado**: ✓ Tabla siempre visible con min-height

### Verificación 4: Crear Compra
```
1. Haz clic "➕ Nueva"
2. Ingresa proveedor: "Distribuidor Test"
3. Selecciona un producto
4. Cantidad: 10
5. Costo: 15.50
6. Haz clic "➕ Agregar"
7. El item aparece en la tabla
8. Total se calcula: 10 × 15.50 = 155.000
```

**Esperado**: ✓ Item agregado y cálculo correcto

### Verificación 5: Editar Compra
```
1. En tabla principal, selecciona una compra
2. Haz clic "✏️ Editar"
3. Datos previos se cargan
4. Modifica el proveedor
5. Agrega un item más
6. Haz clic "✅ GUARDAR COMPRA"
7. Cambios aparecen en tabla
```

**Esperado**: ✓ Cambios guardados correctamente

### Verificación 6: Eliminar Compra
```
1. Selecciona una compra
2. Haz clic "🗑️ Eliminar"
3. Confirma eliminación
4. Compra desaparece de tabla
```

**Esperado**: ✓ Compra eliminada sin errores

## 🔍 Verificaciones Técnicas

### Compilación
```bash
mvnw clean compile -q
# Debería completarse sin errores
```

### Empaquetamiento
```bash
mvnw clean package -q -DskipTests
# Debería completarse sin errores
```

### Logs de Error
Si hay errores, revisa:
1. `hs_err_pid*.log` - Errores JVM
2. Consola de output - Mensajes de error
3. Base de datos - Conexión correcta

## 📋 Checklist de Verificación

### UI/UX
- [ ] Ventana "Nueva Compra" abre con tamaño 950x750
- [ ] No se puede redimensionar menor que 900x700
- [ ] ScrollPane aparece cuando contenido es grande
- [ ] Tabla de items tiene altura mínima 250px
- [ ] Tabla principal tiene altura mínima 350px
- [ ] Color rosado (#FCE4EC) es consistente
- [ ] Botones son accesibles y visibles

### Funcionalidad
- [ ] Se puede crear nueva compra
- [ ] Se puede agregar items a compra
- [ ] Se puede editar compra existente
- [ ] Se puede eliminar compra
- [ ] Stock se actualiza correctamente
- [ ] Totales se calculan correctamente
- [ ] Filtros por fecha funcionan
- [ ] Mensajes de error son claros

### Datos
- [ ] Compras se guardan en BD
- [ ] Datos persisten después de cerrar
- [ ] Stock no se decrementa al eliminar compra
- [ ] Múltiples items en una compra funcionan

## 🐛 Troubleshooting

### Problema: Ventana abre pequeña
**Solución**: Verifica que ComprasController tiene:
```java
Scene scene = new Scene(root, 950, 750);
dialogStage.setMinWidth(900);
dialogStage.setMinHeight(700);
```

### Problema: ScrollPane no aparece
**Solución**: Verifica nueva-compra.fxml tiene:
```xml
<ScrollPane fitToWidth="true" fitToHeight="false">
```

### Problema: Tabla invisible o cortada
**Solución**: Verifica FXML tiene:
- `minHeight="250"` en TableView (nueva-compra)
- `minHeight="350"` en TableView (compras)
- `VBox.vgrow="ALWAYS"` para expansión

### Problema: Stock no se actualiza
**Solución**: Revisa que CompraService.guardar() llama a:
```java
compraService.guardar(compra);
// Esto incrementa automáticamente stock
```

## 📞 Contacto y Soporte

**Documentos de Referencia:**
1. `CORRECCION_TAMAÑO_VENTANA_COMPRAS.md` - Análisis detallado
2. `PRUEBAS_MODULO_COMPRAS_COMPLETO.md` - Plan de pruebas
3. `RESUMEN_CORRECCION_VENTANA_COMPRAS.md` - Resumen de cambios

**Archivos Modificados:**
- `src/main/resources/fxml/nueva-compra.fxml`
- `src/main/resources/fxml/compras.fxml`
- `src/main/java/com/marcofidel_dev/inventario/ui/controller/ComprasController.java`

---

**Última Actualización**: 2025-03-22
**Estado**: ✅ LISTO PARA PRUEBAS

