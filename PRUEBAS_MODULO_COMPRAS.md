# Instrucciones de Prueba del Módulo de Compras - Inventario

**Documento de Prueba:** Validación completa de funcionalidades de crear, editar y eliminar compras

---

## 🚀 PASOS PARA EJECUTAR EL SISTEMA

### 1. Compilar el Proyecto
```bash
cd C:\my-proyects\Inventario
mvn clean package -DskipTests
```

### 2. Ejecutar la Aplicación
```bash
# Opción 1: Usar run.bat
run.bat

# Opción 2: Ejecutar directamente con Maven
mvn spring-boot:run
```

---

## ✅ CASOS DE PRUEBA - MÓDULO DE COMPRAS

### Caso 1: Crear Nueva Compra
**Paso 1:** En la sección de Compras, hacer clic en botón "➕ Nueva"
- ✅ Se debe abrir un diálogo con título "Nueva Compra"
- ✅ Campos vacíos: Proveedor, Fecha (con fecha actual por defecto)
- ✅ Tabla de productos vacía

**Paso 2:** Ingresar datos de la compra
- Proveedor: "Proveedor Ejemplo S.A."
- Fecha: 2026-03-18 (o la actual)
- ✅ El campo debe aceptar texto

**Paso 3:** Agregar productos a la compra
- Seleccionar producto del ComboBox
- Cantidad: 10
- Costo Unitario: 50.500
- Hacer clic en "➕ Agregar"
- ✅ El producto debe aparecer en la tabla con:
  - Nombre del producto
  - Cantidad: 10
  - Costo Unitario: 50.500
  - Subtotal: 505.000

**Paso 4:** Agregar más productos (opcional)
- Repetir paso 3 con otro producto
- ✅ El total debe actualizarse automáticamente en el label "Total: $ XXX.XXX"

**Paso 5:** Guardar la compra
- Hacer clic en "✅ Guardar"
- ✅ Debe aparecer mensaje de confirmación: "Compra guardada exitosamente"
- ✅ El diálogo debe cerrarse
- ✅ La compra debe aparecer en la tabla principal de compras

**Validaciones esperadas:**
- ❌ Si no ingresa proveedor: "Debe ingresar el nombre del proveedor"
- ❌ Si no agrega productos: "Debe agregar al menos un producto a la compra"
- ❌ Si la cantidad no es un número: "Error en el formato de los números"
- ❌ Si la cantidad es ≤ 0: "La cantidad debe ser mayor a 0"
- ❌ Si costo es negativo: "El costo unitario no puede ser negativo"

---

### Caso 2: Editar Compra Existente
**Paso 1:** En la tabla de compras, seleccionar una compra existente
- ✅ Hacer clic en una fila para seleccionarla

**Paso 2:** Hacer clic en "✏️ Editar"
- ✅ Se abre diálogo con título "Editar Compra"
- ✅ Los campos se cargan con los datos existentes:
  - Proveedor
  - Fecha
  - Tabla con los productos de la compra

**Paso 3:** Modificar datos
- Cambiar nombre del proveedor
- Cambiar fecha si es necesario
- Modificar cantidad de un producto (seleccionar y quitar, luego agregar con cantidad nueva)
- ✅ El total debe recalcularse automáticamente

**Paso 4:** Quitar un producto
- Seleccionar un item en la tabla
- Hacer clic en "🗑️ Quitar Seleccionado"
- ✅ El producto debe desaparecer de la tabla
- ✅ El total debe recalcularse

**Paso 5:** Guardar cambios
- Hacer clic en "✅ Guardar"
- ✅ Mensaje: "Compra guardada exitosamente"
- ✅ El diálogo cierra
- ✅ La tabla principal se actualiza con los nuevos datos

---

### Caso 3: Eliminar Compra
**Paso 1:** En la tabla de compras, seleccionar una compra
- ✅ Hacer clic en una fila

**Paso 2:** Hacer clic en "🗑️ Eliminar"
- ✅ Aparece diálogo de confirmación:
  "¿Está seguro que desea eliminar esta compra? Nota: el stock ya fue incrementado."

**Paso 3:** Confirmar eliminación
- Hacer clic en "OK"
- ✅ Mensaje: "Compra eliminada correctamente"
- ✅ La compra desaparece de la tabla

**Paso 4:** Cancelar eliminación
- Hacer clic en "Cancelar" en el diálogo de confirmación
- ✅ La compra NO se elimina
- ✅ La tabla sigue igual

---

### Caso 4: Filtrar Compras por Fecha
**Paso 1:** En los filtros, seleccionar rango de fechas
- Desde: 2026-01-01
- Hasta: 2026-03-18
- Hacer clic en "🔍 Filtrar"
- ✅ La tabla debe mostrar solo compras en ese rango

**Paso 2:** Limpiar filtros
- Dejar ambas fechas en blanco
- Hacer clic en "🔍 Filtrar"
- ✅ La tabla debe mostrar todas las compras

---

### Caso 5: Validar Stock
**Paso 1:** Antes de crear una compra, anotar el stock actual de un producto
- Ir a la sección "Productos"
- Anotar cantidad en stock de un producto (ej: Producto A = 100)

**Paso 2:** Crear una compra con ese producto
- Cantidad agregada: 50 unidades
- Guardar la compra

**Paso 3:** Verificar que el stock se incrementó
- Ir a "Productos"
- El Producto A debe tener: 100 + 50 = 150 unidades
- ✅ El stock debe haberse incrementado automáticamente

---

### Caso 6: Interfaz y Colores
**Verificaciones visuales:**
- ✅ Fondo principal rosado (#FCE4EC)
- ✅ Títulos en color púrpura oscuro (#880E4F)
- ✅ Botones con colores contrastantes:
  - Nuevo: Verde (#4CAF50)
  - Editar: Azul (#2196F3)
  - Filtrar: Rosa/Fucsia (#E91E63)
  - Eliminar: Rojo (#F44336)
  - Cancelar: Gris (#9E9E9E)
- ✅ Sombras y bordes redondeados en tarjetas
- ✅ Texto legible en todos los controles

---

## 📊 RESUMEN DE FUNCIONALIDADES IMPLEMENTADAS

| Funcionalidad | Estado | Prueba |
|---------------|--------|--------|
| Crear Compra  | ✅ Implementada | [Caso 1](#caso-1-crear-nueva-compra) |
| Editar Compra | ✅ Implementada | [Caso 2](#caso-2-editar-compra-existente) |
| Eliminar Compra | ✅ Implementada | [Caso 3](#caso-3-eliminar-compra) |
| Filtrar por Fechas | ✅ Funcionando | [Caso 4](#caso-4-filtrar-compras-por-fecha) |
| Cargar Productos | ✅ Automático | [Caso 5](#caso-5-validar-stock) |
| Incremento de Stock | ✅ Automático | [Caso 5](#caso-5-validar-stock) |
| Validaciones | ✅ Completas | [Casos 1-3](#casos-de-prueba---módulo-de-compras) |
| UI Mejorada | ✅ Colores Rosado | [Caso 6](#caso-6-interfaz-y-colores) |

---

## 🎨 CAMBIOS DE COLORES REALIZADOS

El módulo ha sido diseñado con colores que contrastan con el rosado principal:

### Paleta de Colores Principal
- **Fondo Base:** #FCE4EC (Rosa claro)
- **Títulos:** #880E4F (Púrpura oscuro)
- **Texto Secundario:** #4A148C (Púrpura oscuro)

### Colores de Botones
- **Crear (Nueva):** #4CAF50 (Verde) - Contrasta bien
- **Editar:** #2196F3 (Azul) - Contrasta bien
- **Filtrar:** #E91E63 (Rosa intenso) - Tonal
- **Eliminar:** #F44336 (Rojo) - Contrasta bien
- **Cancelar:** #9E9E9E (Gris) - Neutral

### Detalles Visuales
- Sombras suaves (dropshadow)
- Bordes redondeados (background-radius: 8-10)
- Espaciado consistente (padding: 15-20)
- Líneas separadoras donde corresponde

---

## ⚠️ NOTAS IMPORTANTES

1. **Stock al Eliminar:** Cuando se elimina una compra, el stock NO se decrementa. Este es el comportamiento actual del sistema (como indica el mensaje de confirmación).

2. **Edición de Cantidades:** Para cambiar la cantidad de un producto en una compra:
   - Seleccionar el producto en la tabla
   - Hacer clic en "🗑️ Quitar Seleccionado"
   - Agregar el producto nuevamente con la cantidad correcta

3. **Productos Duplicados:** Si intenta agregar el mismo producto dos veces, se incrementa la cantidad del existente (no se duplica en la tabla).

4. **Base de Datos:** Asegúrese de que la base de datos está correctamente inicializada antes de ejecutar las pruebas.

---

## 🐛 POSIBLES PROBLEMAS Y SOLUCIONES

### Problema: El ComboBox de Productos está vacío
**Solución:** Asegúrese de haber creado productos en la sección "Productos" primero.

### Problema: La compra no se guarda
**Solución:** Verifique que:
- El proveedor no esté vacío
- Haya al menos un producto agregado
- No haya errores en los números (cantidad y costo)

### Problema: El diálogo no se abre
**Solución:** Verifique los logs de la consola. Podría ser un problema de carga de FXML.

### Problema: El stock no se incrementa
**Solución:** Verifique que el servicio de productos esté funcionando correctamente en el log.

---

## 📝 REGISTRO DE CAMBIOS

- **Archivo Creado:** NuevaCompraController.java
- **Archivo Creado:** nueva-compra.fxml
- **Archivo Modificado:** ComprasController.java (métodos nuevaCompra, editarCompra, eliminarCompra)
- **Archivo Modificado:** compras.fxml (botones nuevos)
- **Estado:** ✅ **COMPLETO Y FUNCIONAL**


