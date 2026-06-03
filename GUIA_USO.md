# 📚 Guía de Uso - Sistema de Inventario

## 🎬 Primeros Pasos

### 1. Ejecutar por Primera Vez

**Opción A: Usar el script run.bat (Windows)**
```bash
# Doble clic en run.bat
# o desde terminal:
.\run.bat
```

**Opción B: Maven directo**
```bash
mvn spring-boot:run
```

### 2. Primera Ejecución

La aplicación automáticamente:
1. ✅ Crea el archivo `inventario.db`
2. ✅ Ejecuta la migración `V1__init.sql`
3. ✅ Inserta 5 productos de ejemplo
4. ✅ Abre la ventana principal

---

## 📦 Gestión de Productos

### Agregar un Producto Nuevo

1. Click en **"📦 Productos"** en el sidebar
2. Click en botón **"➕ Nuevo"**
3. Llenar formulario:
   - **Tipo**: Seleccionar MAQUILLAJE/BOLSO/BISUTERIA
   - **Nombre**: Ej. "Labial Matte Rojo"
   - **Marca**: Ej. "MAC" (opcional)
   - **SKU**: Ej. "MAC-LAB-RED-001" (opcional, único)
   - **Costo**: Ej. 15.50
   - **Precio Venta**: Ej. 30.00
   - **Stock Actual**: Ej. 10
   - **Stock Mínimo**: Ej. 3
   - **Activo**: ✅ (marcado)
   
   **Campos opcionales según tipo**:
   - Maquillaje: Tono/Color, Fecha Vencimiento
   - Todos: Color, Tamaño, Material, Talla

4. Click en **"💾 Guardar"**

### Buscar Productos

**Por nombre o SKU**:
```
1. Escribir en campo "Buscar por nombre o SKU..."
2. Click "🔍 Buscar"
```

**Por tipo**:
```
1. Seleccionar tipo en combo: MAQUILLAJE/BOLSO/BISUTERIA
2. Click "🔍 Buscar"
```

**Solo stock bajo**:
```
1. Marcar checkbox "Solo stock bajo"
2. Click "🔍 Buscar"
```

### Editar un Producto

```
1. Click en una fila de la tabla
2. Datos se cargan en formulario
3. Modificar campos deseados
4. Click "💾 Guardar"
```

### Desactivar un Producto

```
1. Seleccionar producto en tabla
2. Click "🗑️ Eliminar"
3. Confirmar
```

**Nota**: No se elimina físicamente, solo se marca como `activo=false`

---

## 🛍️ Gestión de Pedidos

### Ver Pedidos

**Dashboard**: Muestra pedidos pendientes
**Vista completa**: Click en **"🛍️ Pedidos"**

### Filtrar Pedidos

**Por estado**:
```
1. Seleccionar en combo: PENDIENTE/ENTREGADO/CANCELADO
2. Click "🔍 Filtrar"
```

**Por rango de fechas**:
```
1. Seleccionar "Desde" y "Hasta"
2. Click "🔍 Filtrar"
```

### Entregar un Pedido (IMPORTANTE)

```
1. Seleccionar pedido con estado PENDIENTE
2. Click "✅ Marcar Entregado"
3. Sistema verifica stock disponible
4. Si hay stock: descuenta y cambia estado
5. Si NO hay stock: muestra error
```

**⚠️ REGLA**: El stock se descuenta SOLO al marcar como ENTREGADO

### Cancelar un Pedido

```
1. Seleccionar pedido PENDIENTE
2. Click "❌ Cancelar Pedido"
3. Confirmar
```

**Nota**: No se puede cancelar si ya está ENTREGADO

---

## 📥 Gestión de Compras

### Ver Compras

Click en **"📥 Compras"**

### Filtrar por Fechas

```
1. Seleccionar "Desde" y "Hasta"
2. Click "🔍 Filtrar"
```

### ⚠️ Regla de Compras

Al guardar una compra:
1. ✅ Stock se incrementa AUTOMÁTICAMENTE
2. ✅ No se puede "deshacer" fácilmente
3. ⚠️ Verificar datos antes de guardar

**Flujo lógico**:
```
Usuario crea Compra
    → CompraService.guardar()
    → Por cada CompraItem:
        → ProductoService.incrementarStock()
    → Stock actualizado ✅
```

---

## 📊 Dashboard

### Información Mostrada

**Contadores**:
- **Total Productos**: Cantidad de productos activos
- **Pedidos Pendientes**: Pedidos en estado PENDIENTE
- **Stock Bajo**: Productos donde `stock_actual ≤ stock_minimo`

**Tablas**:
1. **Productos con Stock Bajo** (máximo prioridad)
   - Productos que necesitan reposición
   - Color rojo de alerta

2. **Pedidos Pendientes**
   - Pedidos que faltan entregar
   - Ordenados por fecha

### Actualizar Dashboard

```
Click en botón "🔄 Actualizar"
```

---

## 💾 Backups

### Crear un Backup

```
1. Click en "💾 Backup" en sidebar
2. Click "📁 Seleccionar carpeta y crear backup"
3. Elegir carpeta de destino
4. Sistema crea archivo: inventario_2026-02-05_14-30.db
5. Mensaje de éxito muestra ruta completa
```

**Frecuencia recomendada**:
- ✅ Diario (al inicio/fin del día)
- ✅ Antes de operaciones masivas
- ✅ Antes de actualizar la aplicación

### Restaurar un Backup

```
1. Click en "📂 Seleccionar archivo y restaurar"
2. LEER advertencia: se reemplazará BD actual
3. Confirmar
4. Seleccionar archivo .db
5. Sistema restaura
6. ⚠️ CERRAR Y REINICIAR LA APLICACIÓN
```

**⚠️ IMPORTANTE**: 
- Se crea backup de seguridad antes de restaurar
- Archivo: `inventario.db.before-restore`
- DEBE reiniciar la app para ver cambios

### Estrategia de Backups

```
Mi Carpeta de Backups/
├── inventario_2026-02-01_09-00.db  (inicio de mes)
├── inventario_2026-02-05_09-00.db  (diario)
├── inventario_2026-02-05_18-00.db  (fin de día)
└── inventario_2026-02-05_antes-actualizacion.db  (antes de cambios)
```

---

## 🎨 Colores y Alertas Visuales

### Productos con Stock Bajo

**En tabla de productos**:
- Fondo ROJO (#ffcccc) si `stockActual ≤ stockMinimo`

**Ejemplo**:
```
Producto: Base Líquida
Stock Actual: 2
Stock Mínimo: 5
→ Se muestra con fondo ROJO
```

### Contadores del Dashboard

- **Azul**: Información general (Total Productos)
- **Rojo**: Atención urgente (Pedidos Pendientes)
- **Naranja**: Advertencia (Stock Bajo)

---

## 📋 Casos de Uso Comunes

### Caso 1: Llega una Clienta con un Pedido

**Sin la app**:
```
❌ Anotar en cuaderno
❌ ¿Hay stock? No se sabe
❌ Difícil hacer seguimiento
```

**Con la app**:
```
✅ Crear pedido (estado: PENDIENTE)
✅ Sistema muestra stock disponible
✅ Al entregar: marcar ENTREGADO
✅ Stock se descuenta automáticamente
```

**Nota**: Los pedidos creados directamente desde UI serían del futuro (fuera del MVP actual que solo lista/gestiona)

### Caso 2: Producto se está Agotando

**Dashboard**:
```
Stock Bajo: 3  ← Alerta visible
```

**Ver detalle**:
```
1. Click en tabla "Productos con Stock Bajo"
2. Ver qué productos necesitan reposición
```

**Acción**:
```
1. Hacer pedido a proveedor
2. Al recibir: Crear Compra en la app
3. Stock se incrementa automáticamente
```

### Caso 3: Fin de Mes - Análisis

```
1. Crear backup del mes
2. Ver tabla de Compras (cuánto se gastó)
3. Ver tabla de Pedidos ENTREGADOS (cuánto se vendió)
4. Identificar productos más vendidos
5. Decidir qué reponer
```

### Caso 4: Error en un Pedido

**Si aún está PENDIENTE**:
```
1. Cancelar pedido
2. Crear uno nuevo correcto
```

**Si ya está ENTREGADO**:
```
❌ NO se puede cambiar (stock ya descontado)
✅ Crear "compra" de devolución si aplica
```

---

## 🔍 Validaciones del Sistema

### Productos

| Campo | Validación |
|-------|------------|
| Tipo | Obligatorio |
| Nombre | Obligatorio, no vacío |
| SKU | Opcional, pero debe ser único |
| Costo | ≥ 0 |
| Precio Venta | ≥ 0 |
| Stock Actual | ≥ 0, entero |
| Stock Mínimo | ≥ 0, entero |

### Pedidos

| Acción | Validación |
|--------|------------|
| Marcar Entregado | Stock debe ser suficiente |
| Cancelar | No puede estar ENTREGADO |
| Eliminar | No puede estar ENTREGADO |

---

## 🐛 Solución de Problemas

### "No se ven los productos seed"

**Causa**: La BD ya existía de una ejecución anterior
**Solución**:
```
1. Cerrar app
2. Eliminar archivo inventario.db
3. Reiniciar app (se crea nuevo con seed)
```

### "Error: Stock insuficiente"

**Al marcar pedido como ENTREGADO**

**Causa**: Producto no tiene stock suficiente
**Solución**:
```
1. Ir a Productos
2. Editar producto
3. Incrementar Stock Actual
4. O crear una Compra para reponerlo
```

### "La app no inicia"

**Posibles causas**:
1. Java no instalado → Instalar JDK 17
2. Maven no instalado → Usar run.bat o instalar Maven
3. Puerto ocupado → Cerrar otras apps Java

### "Los cambios después de restaurar backup no se ven"

**Causa**: No reiniciaste la app
**Solución**:
```
1. Cerrar completamente la aplicación
2. Volver a ejecutar run.bat o mvn spring-boot:run
```

---

## 💡 Tips y Mejores Prácticas

### 1. Gestión de Stock

✅ **Hacer**:
- Revisar Dashboard diariamente
- Reponer cuando `Stock Bajo > 0`
- Usar SKU para productos similares

❌ **Evitar**:
- Marcar pedidos como entregados sin verificar
- Eliminar productos con historial

### 2. Pedidos

✅ **Hacer**:
- Crear pedido PENDIENTE al recibirlo
- Marcar ENTREGADO al momento de entregar
- Cancelar si la clienta no recoge

❌ **Evitar**:
- Marcar ENTREGADO antes de confirmar stock
- Eliminar pedidos entregados (pierdes historial)

### 3. Compras

✅ **Hacer**:
- Registrar compra al recibir productos
- Verificar cantidades antes de guardar

❌ **Evitar**:
- Registrar compra antes de recibirla
- Duplicar compras

### 4. Backups

✅ **Hacer**:
- Backup diario
- Backup antes de actualizar app
- Guardar backups en carpeta separada

❌ **Evitar**:
- Sobrescribir backups antiguos importantes
- Backup solo cuando hay problema

---

## 📖 Glosario

| Término | Significado |
|---------|-------------|
| **Stock Actual** | Cantidad disponible ahora |
| **Stock Mínimo** | Umbral de alerta |
| **Stock Bajo** | Cuando Actual ≤ Mínimo |
| **SKU** | Stock Keeping Unit (código único) |
| **PENDIENTE** | Pedido creado, no entregado |
| **ENTREGADO** | Pedido entregado (stock descontado) |
| **CANCELADO** | Pedido no se concretó |
| **Activo** | Producto visible en sistema |
| **Seed Data** | Datos de ejemplo iniciales |

---

## 📞 Soporte Rápido

### Logs

Los logs en consola muestran:
```
DEBUG: Operaciones detalladas
INFO: Acciones importantes
ERROR: Problemas con stack trace
```

### Base de Datos

**Ubicación**: `inventario.db` (raíz del proyecto)
**Herramientas**: DB Browser for SQLite, DBeaver

### Consulta Rápida SQL

```sql
-- Ver todos los productos
SELECT * FROM producto WHERE activo = 1;

-- Ver stock bajo
SELECT nombre, stock_actual, stock_minimo 
FROM producto 
WHERE stock_actual <= stock_minimo;

-- Ver pedidos pendientes
SELECT * FROM pedido WHERE estado = 'PENDIENTE';
```

---

**Última actualización**: 2026-02-05  
**Versión de la guía**: 1.0

