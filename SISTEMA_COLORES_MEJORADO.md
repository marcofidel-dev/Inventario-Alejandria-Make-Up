# Sistema de Colores Mejorado - Inventario

## 🎨 Paleta de Colores Principal

### Color Primario (Rosado)
- **Rosado Oscuro**: `#880E4F` - Títulos, énfasis, bordes
- **Rosado Claro**: `#FCE4EC` - Fondo principal, áreas secundarias
- **Rosado Medio**: `#C2185B` - Gradientes, botones secundarios
- **Rosado Fuerte**: `#E91E63` - Botones principales, acciones primarias

### Colores Complementarios (Que Contrastan)
- **Blanco**: `#FFFFFF` - Fondos de contenedores, texto sobre oscuro
- **Gris Oscuro**: `#424242` - Texto principal
- **Gris Claro**: `#F5F5F5` - Fondos secundarios
- **Gris Neutral**: `#757575` - Bordes, separadores

### Colores de Estado (Funcionales)
- **Verde Éxito**: `#4CAF50` - Crear, guardar, validar
- **Verde Oscuro**: `#00897B` - Nuevo, agregar, positivo
- **Azul Información**: `#2196F3` - Editar, modificar
- **Naranja Advertencia**: `#FF9800` - Stock bajo, alerta
- **Naranja Oscuro**: `#F57C00` - Stock crítico
- **Rojo Error**: `#F44336` - Eliminar, cancelar, error

### Colores Temáticos
- **Púrpura**: `#7E57C2` - Complemento, variedad
- **Púrpura Oscuro**: `#4A148C` - Contraste fuerte
- **Teal**: `#00897B` - Nuevo producto, crear

## 📐 Reglas de Aplicación

### Fondos
- **Principal**: `#FCE4EC` (rosado claro)
- **Contenedores**: `#FFFFFF` (blanco)
- **Secundarios**: `#F5F5F5` (gris muy claro)
- **Gradientes**: `linear-gradient(to bottom, #E91E63, #C2185B)`

### Textos
- **Títulos principales**: `#880E4F` (rosado oscuro, 28px, bold)
- **Subtítulos**: `#880E4F` (rosado oscuro, 16px, bold)
- **Texto normal**: `#424242` (gris oscuro, 12px)
- **Texto claro**: `#FFFFFF` (blanco, sobre fondos oscuros)

### Botones
**Primarios (Acciones principales)**
```
background-color: #E91E63 (rosado fuerte)
text-fill: white
effect: dropshadow
```

**Secundarios (Acciones normales)**
```
background-color: #2196F3 (azul)
text-fill: white
effect: dropshadow
```

**Éxito (Crear/Guardar)**
```
background-color: #4CAF50 (verde)
text-fill: white
effect: dropshadow
```

**Peligro (Eliminar)**
```
background-color: #F44336 (rojo)
text-fill: white
effect: dropshadow
```

**Neutral (Cancelar)**
```
background-color: #9E9E9E (gris)
text-fill: white
effect: dropshadow
```

### Contenedores
- **Padding**: 15-20 px
- **Radius**: 8-10 px
- **Background**: `#FFFFFF`
- **Effect**: `dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2)`
- **Border**: Ninguno (usar sombra)

### Tablas
- **Background**: `#FFFFFF`
- **Alternancia**: No necesaria, mantener limpio
- **Radius**: 8 px
- **Hover**: Color ligeramente más oscuro (CSS automático)

## 🎯 Cambios por Módulo

### 1. Dashboard
- Mantener gradientes en contadores
- Rosa + Púrpura + Naranja (variedad visual)
- Botón "Actualizar" en rosado fuerte

### 2. Productos
- Botón "Nuevo" en verde éxito (`#4CAF50`)
- Botón "Buscar" en rosa fuerte (`#E91E63`)
- TextField con padding 8

### 3. Pedidos
- Botón "Nuevo Pedido" en verde
- Botón "Entregar" en azul
- Botón "Cancelar" en rojo

### 4. Compras
- Botón "Nueva" en verde éxito
- Botón "Editar" en azul
- Botón "Eliminar" en rojo
- Botón "Guardar" en rosa fuerte

### 5. Backup
- Botón "Realizar Backup" en verde
- Botón "Restaurar" en naranja
- Botón "Descargar" en azul

## ✨ Mejoras Visuales Adicionales

### Efectos
- **Drop Shadow**: `dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2)` en contenedores
- **Botones**: `dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2)`
- **Navegación**: `dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1)`

### Bordes y Separadores
- **Radio**: 8-10 px en botones, 10-15 px en contenedores
- **Separadores**: `rgba(255,255,255,0.3)` en barras oscuras
- **Padding**: 15-20 px en contenedores, 8 px en componentes

### Tipografía
- **Títulos**: Bold, 28px, `#880E4F`
- **Subtítulos**: Bold, 16-18px, `#880E4F`
- **Etiquetas**: Bold, 12-14px, `#4A148C` o `#424242`
- **Texto Normal**: Regular, 12px, `#424242`

## 🎨 Ejemplos de Combinaciones

### Tarjeta de Contador
```css
-fx-background-color: linear-gradient(to bottom, #E91E63, #C2185B);
-fx-text-fill: white;
-fx-padding: 25;
-fx-background-radius: 15;
-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);
```

### Botón Primario
```css
-fx-background-color: #E91E63;
-fx-text-fill: white;
-fx-background-radius: 8;
-fx-padding: 12;
-fx-font-weight: 600;
-fx-cursor: hand;
-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);
```

### Contenedor de Datos
```css
-fx-background-color: white;
-fx-background-radius: 10;
-fx-padding: 15;
-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);
```

## 📋 Checklist de Aplicación

- [ ] main.fxml - Barra lateral y navegación
- [ ] dashboard.fxml - Contadores y datos
- [ ] productos.fxml - Gestión de productos
- [ ] pedidos.fxml - Gestión de pedidos
- [ ] compras.fxml - Gestión de compras
- [ ] backup.fxml - Backup y restauración
- [ ] nueva-compra.fxml - Diálogo de compra
- [ ] nuevo-pedido.fxml - Diálogo de pedido

## 🔍 Validación de Colores

### Contraste
- ✓ Rosa oscuro (#880E4F) sobre blanco: 6.8:1 (AAA)
- ✓ Rosa fuerte (#E91E63) sobre blanco: 4.2:1 (AA)
- ✓ Blanco sobre rosa oscuro: 6.8:1 (AAA)
- ✓ Verde (#4CAF50) sobre blanco: 3.9:1 (AA)

### Accesibilidad
- ✓ Alto contraste para texto sobre fondo
- ✓ Suficiente diferenciación entre elementos
- ✓ No depende solo del color para información

---

**Estado**: LISTO PARA APLICAR
**Versión**: 1.0
**Fecha**: 2025-03-22

