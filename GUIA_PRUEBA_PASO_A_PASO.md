# 🧪 Guía Paso a Paso - Probar Ventana de Compras

## ✅ Verificación de Funcionamiento

Esta guía te permite validar que la ventana de agregar compras funciona correctamente.

---

## 🚀 Paso 1: Preparar la Aplicación

### Opción A: Script Automatizado
```bash
cd C:\my-proyects\Inventario
validar-cambios.bat
```

### Opción B: Compilar Manualmente
```bash
cd C:\my-proyects\Inventario
mvnw clean compile
mvnw spring-boot:run
```

---

## 🎯 Paso 2: Ejecutar la Aplicación

```bash
cd C:\my-proyects\Inventario
run.bat
```

**Esperado**: Se abre la aplicación Inventario con menú lateral

---

## 📝 Paso 3: Navegar al Módulo de Compras

1. En la ventana principal, busca el **menú lateral izquierdo**
2. Haz clic en **"📥 Compras"** (ícono rojo)
3. Se abre el módulo "Gestión de Compras"

**Esperado**: Ventana muestra tabla de compras existentes

---

## ➕ Paso 4: Crear Nueva Compra

1. En la ventana de Compras, haz clic en botón **"➕ Nueva"** (verde)
2. Se abre diálogo **"Nueva Compra a Proveedor"**

**Verifica**: 
- ✅ Ventana abre con tamaño apropiado (~950x750)
- ✅ Fondo es rosa claro (#FCE4EC)
- ✅ Título dice "Nueva Compra a Proveedor" en rosa oscuro
- ✅ Todos los campos son visibles

---

## 📋 Paso 5: Completar Datos del Proveedor

### En la sección "Datos del Proveedor":

1. **Campo "Proveedor"**:
   - Ingresa: `Distribuidor Test ABC`
   - Presiona Tab

2. **Campo "Fecha"**:
   - Haz clic en DatePicker
   - Selecciona la fecha de hoy
   - Se asigna automáticamente

**Esperado**: Los datos se cargan en los campos

---

## 🛒 Paso 6: Agregar Primer Producto

### En la sección "Agregar Productos a la Compra":

1. **ComboBox "Producto"**:
   - Haz clic en dropdown
   - Selecciona un producto (ej: "Shirt Azul")

2. **Campo "Cant"** (Cantidad):
   - Ingresa: `10`

3. **Campo "Costo"**:
   - Ingresa: `15.50`

4. **Botón "➕ Agregar"**:
   - Haz clic (botón verde con sombra)

**Esperado**: 
- ✅ Item aparece en tabla "Productos en la Compra"
- ✅ Cantidad: 10
- ✅ Costo Unitario: 15.50
- ✅ Subtotal: 155.000

---

## 🧮 Paso 7: Verificar Cálculos

En la tabla de items:

```
Producto          | Cantidad | Costo     | Subtotal
Shirt Azul        | 10       | 15.50     | 155.000
```

En la etiqueta de Total:
```
Total: $ 155.000
```

**Esperado**: Los cálculos son precisos (10 × 15.50 = 155.000)

---

## ➕ Paso 8: Agregar Segundo Producto (Opcional)

Repite el proceso:

1. Selecciona otro producto (ej: "Shirt Rojo")
2. Cantidad: `5`
3. Costo: `12.00`
4. Clic "➕ Agregar"

**Esperado**:
- Aparece nuevo item en tabla
- Subtotal: 60.000
- **Total actualizado**: 155.000 + 60.000 = **215.000**

---

## 💾 Paso 9: Guardar Compra

En la parte inferior de la ventana:

1. Haz clic en botón **"✅ GUARDAR COMPRA"** (rosa fuerte)

**Esperado**:
- ✅ Aparece mensaje: "Compra guardada correctamente"
- ✅ Ventana se cierra automáticamente
- ✅ Vuelves a tabla de Compras

---

## ✔️ Paso 10: Verificar Guardado

En la tabla principal de Compras:

1. Busca la nueva compra (proveedor "Distribuidor Test ABC")
2. Verifica:
   - **ID**: Se asignó automáticamente ✓
   - **Proveedor**: "Distribuidor Test ABC" ✓
   - **Fecha**: Fecha actual ✓
   - **Total Costo**: 155.000 o 215.000 ✓

**Esperado**: Compra aparece en la tabla

---

## 📊 Resumen de Verificación

| Prueba | ✅ Esperado | Status |
|--------|-----------|--------|
| Ventana abre | Sí | ✅ |
| Tamaño correcto | 950x750 | ✅ |
| Ingresar proveedor | Funciona | ✅ |
| Seleccionar fecha | Funciona | ✅ |
| Agregar producto | Funciona | ✅ |
| Cálculo subtotal | 155.000 | ✅ |
| Cálculo total | Correcto | ✅ |
| Guardar | Funciona | ✅ |
| Dato persiste | En tabla | ✅ |

---

## 🎯 Casos Adicionales a Probar

### Validación: Cantidad Negativa
```
1. En Cantidad, ingresa: -5
2. Haz clic "➕ Agregar"
Resultado: ✅ Debería mostrar error
```

### Validación: Campo Vacío
```
1. Deja Cantidad vacía
2. Haz clic "➕ Agregar"
Resultado: ✅ Debería mostrar error
```

### ScrollPane: Ventana Pequeña
```
1. Reduce ventana al tamaño mínimo (900x700)
2. Intenta desplazarte
Resultado: ✅ ScrollPane debe aparecer si hay contenido
```

### Redimensionamiento
```
1. Intenta hacer ventana menor que 900x700
Resultado: ✅ No debe permitir (mínimo impuesto)
```

---

## ❓ Si Algo No Funciona

### Error: Ventana no abre
```
→ Verifica compilación: mvnw clean compile
→ Revisa consola por errores
→ Verifica que módulo Compras está visible
```

### Error: Tabla vacía
```
→ Asegúrate de que hay productos en la BD
→ Crea un producto primero en módulo "Productos"
→ Luego intenta agregar compra
```

### Error: Tamaño incorrecto
```
→ Verifica archivo: nueva-compra.fxml
→ Debe tener: prefWidth="950" prefHeight="750"
→ Verifica ComprasController.java
→ Debe tener: new Scene(root, 950, 750)
```

### Error: Cálculos incorrectos
```
→ Verifica que ingresaste números válidos
→ Formato decimal: 15.50 (no 15,50)
→ Verifica que cantidad es número entero
```

---

## ✨ Resultado Esperado Final

```
┌────────────────────────────────────┐
│ ✅ COMPRA CREADA EXITOSAMENTE     │
│                                   │
│ Proveedor: Distribuidor Test ABC  │
│ Productos: 1 o 2                  │
│ Total: 155.000 o 215.000          │
│                                   │
│ 🟢 VENTANA FUNCIONANDO BIEN       │
└────────────────────────────────────┘
```

---

## 📞 Información Adicional

Para más detalles técnicos:
- `VERIFICACION_VENTANA_COMPRAS.md` - Detalles técnicos completos
- `RESULTADO_VERIFICACION.md` - Resumen de verificación
- `GUIA_VERIFICACION_COMPRAS.md` - Otras pruebas

---

**Duración estimada**: 5-10 minutos
**Dificultad**: Muy fácil
**Requisitos**: Aplicación ejecutándose

✅ **Guía completada. ¡A probar!**

