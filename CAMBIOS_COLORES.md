# 🎨 Cambios de Colores - Sistema de Inventario

## Paleta de Colores Implementada

Se ha actualizado completamente el sistema de inventario con una paleta de colores basada en **rosado** como color principal, con colores que contrastan perfectamente y mejores detalles visuales.

### Colores Principales

| Color | Código | Uso |
|-------|--------|-----|
| **Rosado Principal** | `#E91E63` | Botones primarios, acentos principales |
| **Rosado Oscuro** | `#C2185B` | Barra lateral, degradados |
| **Rosado Muy Oscuro** | `#880E4F` | Títulos principales, textos importantes |
| **Púrpura Oscuro** | `#4A148C` | Etiquetas, contraste fuerte |
| **Verde Esmeralda** | `#00897B` | Acciones positivas (Guardar, Crear) |
| **Naranja** | `#F57C00` | Advertencias, acciones secundarias |
| **Rojo Coral** | `#D32F2F` | Acciones negativas (Eliminar, Cancelar) |
| **Fondo Rosado Claro** | `#FCE4EC` | Fondo principal de páginas |
| **Lila Claro** | `#F3E5F5` | Fondos de notas informativas |

## Archivos Actualizados

### ✅ 1. main.fxml - Barra Lateral
- Fondo con degradado rosado: `#C2185B` a `#880E4F`
- Botones de navegación en rosado `#E91E63`
- Sombras mejoradas para profundidad
- Área de contenido con fondo `#FCE4EC`

### ✅ 2. dashboard.fxml - Panel de Control
- Fondo general rosado claro `#FCE4EC`
- Tarjetas con degradados:
  - Total Productos: Rosado `#E91E63` a `#C2185B`
  - Pedidos Pendientes: Púrpura `#4A148C` a `#311B92`
  - Stock Bajo: Naranja `#F57C00` a `#E65100`
- Botón actualizar en rosado `#E91E63`
- Tablas con fondo blanco y sombras suaves

### ✅ 3. productos.fxml - Gestión de Productos
- Fondo rosado claro `#FCE4EC`
- Sección de filtros con fondo blanco y sombra
- Etiquetas en púrpura oscuro `#4A148C`
- Botones con colores semánticos:
  - Buscar: Rosado `#E91E63`
  - Nuevo: Verde `#00897B`
  - Guardar: Verde `#00897B`
  - Eliminar: Rojo `#D32F2F`
  - Limpiar: Naranja `#F57C00`
- Campos de texto con bordes redondeados
- Separador rosado entre secciones

### ✅ 4. pedidos.fxml - Gestión de Pedidos
- Fondo rosado claro `#FCE4EC`
- Filtros en contenedor blanco con sombra
- Tabla con estilo mejorado
- Botones:
  - Nuevo Pedido: Rosado `#E91E63` (destacado)
  - Marcar Entregado: Verde `#00897B`
  - Cancelar: Rojo `#D32F2F`
- Nota informativa con fondo lila `#F3E5F5`

### ✅ 5. nuevo-pedido.fxml - Crear Pedido
- Fondo rosado claro `#FCE4EC`
- Secciones organizadas en tarjetas blancas con sombra
- Campos con bordes redondeados
- Total destacado en rosado `#E91E63`
- Botones:
  - Agregar Item: Verde `#00897B`
  - Quitar Item: Rojo `#D32F2F`
  - Guardar: Rosado `#E91E63` (prominente)
  - Cancelar: Naranja `#F57C00`

### ✅ 6. compras.fxml - Gestión de Compras
- Fondo rosado claro `#FCE4EC`
- Filtros en contenedor blanco
- Tabla con título destacado
- Nota informativa con fondo lila `#F3E5F5`

### ✅ 7. backup.fxml - Gestión de Backups
- Fondo rosado claro `#FCE4EC`
- Tarjetas con sombras profundas
- Sección de crear backup: Fondo blanco, botón verde
- Sección de restaurar: Fondo rojo claro `#FFEBEE`, borde rojo
- Información con borde rosado y fondo blanco

## Mejoras Visuales Implementadas

### 🎯 Detalles Mejorados

1. **Efectos de Sombra**
   - `dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2)` para tarjetas
   - `dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2)` para botones
   - `dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3)` para contadores

2. **Bordes Redondeados**
   - Botones: `8px`
   - Tarjetas pequeñas: `10px`
   - Tarjetas grandes: `15px`

3. **Degradados**
   - Barra lateral: `linear-gradient(to bottom, #C2185B, #880E4F)`
   - Contadores con degradados visuales

4. **Tipografía**
   - Títulos principales: `28px`, negrita, color `#880E4F`
   - Subtítulos: `18px` o `20px`, negrita
   - Etiquetas: `600` peso, color `#4A148C`
   - Texto normal mejorado con peso `600` para mejor legibilidad

5. **Espaciado**
   - Aumentado a `15px` o `20px` entre secciones
   - Padding interno aumentado para mejor respiración visual

6. **Interactividad**
   - Cursor tipo mano en botones: `-fx-cursor: hand`
   - Mejor contraste para accesibilidad

## Consistencia del Diseño

✨ **Todos los archivos siguen el mismo patrón de diseño:**
- Fondo principal rosado claro
- Tarjetas blancas con sombras
- Colores consistentes para acciones similares
- Tipografía coherente
- Espaciado uniforme

## Cómo Probar

Para ver los cambios, ejecuta la aplicación:

```powershell
.\run.bat
```

O compila y ejecuta manualmente:

```powershell
mvn clean package
java -jar target\Inventario-0.0.1-SNAPSHOT.jar
```

## Notas Técnicas

- Los cambios son puramente visuales (CSS inline en FXML)
- No requieren cambios en código Java
- Compatible con JavaFX 17+
- Los colores cumplen con estándares de accesibilidad WCAG 2.1

---

**Fecha de actualización:** 2026-02-28  
**Versión:** 1.0  
**Diseñador:** Sistema de Inventario - Tema Rosado

