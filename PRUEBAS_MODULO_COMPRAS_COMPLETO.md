# Pruebas del Módulo de Compras - Plan de Validación

## Descripción General
Plan de pruebas comprehensive para validar la funcionalidad completa del módulo de Compras en el sistema de Inventario.

## 1. Pruebas de Interfaz (UI)

### 1.1 Visualización Principal
- [ ] **PR-UI-001**: Verificar que la ventana "Gestión de Compras" se abre correctamente
- [ ] **PR-UI-002**: Confirmar que la tabla de historial se muestra con datos existentes
- [ ] **PR-UI-003**: Verificar que el título tiene color rosado (#880E4F)
- [ ] **PR-UI-004**: Confirmar que el fondo es color rosado claro (#FCE4EC)
- [ ] **PR-UI-005**: Verificar que todos los botones (Nueva, Editar, Eliminar, Filtrar) son visibles

### 1.2 Tamaño y Redimensionamiento de Ventanas
- [ ] **PR-SIZE-001**: Confirmar que "Nueva Compra" abre con tamaño 950x750
- [ ] **PR-SIZE-002**: Verificar que "Nueva Compra" tiene tamaño mínimo 900x700
- [ ] **PR-SIZE-003**: Intentar redimensionar a menos de 900x700, debe impedirse
- [ ] **PR-SIZE-004**: Redimensionar ventana mayor a 950x750, debe funcionar
- [ ] **PR-SIZE-005**: Verificar ScrollPane aparece cuando contenido es muy grande
- [ ] **PR-SIZE-006**: Confirmar tabla en "Nueva Compra" tiene altura mínima 250px

### 1.3 Elementos de la Ventana Nueva Compra
- [ ] **PR-FORM-001**: Campo "Proveedor" existe y es editable
- [ ] **PR-FORM-002**: Campo "Fecha" tiene DatePicker funcional
- [ ] **PR-FORM-003**: ComboBox de productos carga la lista correctamente
- [ ] **PR-FORM-004**: Campo "Cantidad" acepta números
- [ ] **PR-FORM-005**: Campo "Costo Unitario" acepta decimales
- [ ] **PR-FORM-006**: Tabla de items en compra es visible
- [ ] **PR-FORM-007**: Etiqueta de "Total" se muestra correctamente
- [ ] **PR-FORM-008**: Botones "GUARDAR COMPRA" y "CANCELAR" son visibles

## 2. Pruebas de Funcionalidad - Crear Compra

### 2.1 Agregar Items a la Compra
- [ ] **PR-CREATE-001**: Hacer clic en "Nueva" abre ventana de nueva compra
- [ ] **PR-CREATE-002**: Seleccionar un producto del ComboBox
- [ ] **PR-CREATE-003**: Ingresar cantidad válida (ej: 10)
- [ ] **PR-CREATE-004**: Ingresar costo unitario válido (ej: 15.50)
- [ ] **PR-CREATE-005**: Hacer clic en "Agregar" añade item a la tabla
- [ ] **PR-CREATE-006**: El subtotal se calcula correctamente (Cantidad × Costo)
- [ ] **PR-CREATE-007**: El total se actualiza correctamente después de agregar
- [ ] **PR-CREATE-008**: Se puede agregar múltiples items
- [ ] **PR-CREATE-009**: Si se agrega mismo producto dos veces, se incrementa cantidad
- [ ] **PR-CREATE-010**: Se puede quitar items de la tabla

### 2.2 Validaciones al Agregar Items
- [ ] **PR-VAL-001**: No permite cantidad cero
- [ ] **PR-VAL-002**: No permite cantidad negativa
- [ ] **PR-VAL-003**: No permite costo negativo
- [ ] **PR-VAL-004**: Muestra error si campos están vacíos
- [ ] **PR-VAL-005**: Rechaza valores no numéricos
- [ ] **PR-VAL-006**: Mensaje de error es claro y amigable

### 2.3 Crear Compra Completa
- [ ] **PR-SAVE-001**: Ingresar nombre del proveedor válido
- [ ] **PR-SAVE-002**: Seleccionar fecha actual o anterior
- [ ] **PR-SAVE-003**: Agregar al menos un item válido
- [ ] **PR-SAVE-004**: Hacer clic en "GUARDAR COMPRA"
- [ ] **PR-SAVE-005**: Sistema muestra mensaje de éxito
- [ ] **PR-SAVE-006**: La nueva compra aparece en la tabla principal
- [ ] **PR-SAVE-007**: El ID de la compra se asigna correctamente
- [ ] **PR-SAVE-008**: La fecha se muestra correctamente en la tabla
- [ ] **PR-SAVE-009**: El total es correcto (suma de subtotales)
- [ ] **PR-SAVE-010**: Stock de productos se incrementa automáticamente

## 3. Pruebas de Funcionalidad - Editar Compra

### 3.1 Abrir Edición
- [ ] **PR-EDIT-001**: Seleccionar una compra en la tabla
- [ ] **PR-EDIT-002**: Hacer clic en "Editar"
- [ ] **PR-EDIT-003**: Se abre ventana con datos de la compra cargados
- [ ] **PR-EDIT-004**: Campo "Proveedor" muestra valor anterior
- [ ] **PR-EDIT-005**: Campo "Fecha" muestra fecha anterior
- [ ] **PR-EDIT-006**: Tabla muestra items anteriores

### 3.2 Modificar Compra
- [ ] **PR-EDIT-007**: Se puede cambiar nombre del proveedor
- [ ] **PR-EDIT-008**: Se puede cambiar fecha
- [ ] **PR-EDIT-009**: Se puede agregar nuevo item
- [ ] **PR-EDIT-010**: Se puede eliminar un item existente
- [ ] **PR-EDIT-011**: El total se recalcula correctamente
- [ ] **PR-EDIT-012**: Hacer clic en "GUARDAR COMPRA" guarda cambios
- [ ] **PR-EDIT-013**: La compra actualizada aparece en tabla principal
- [ ] **PR-EDIT-014**: Los cambios se persisten en la base de datos

### 3.3 Cancelar Edición
- [ ] **PR-EDIT-015**: Hacer clic en "CANCELAR" cierra ventana
- [ ] **PR-EDIT-016**: Los cambios no guardados se descartan
- [ ] **PR-EDIT-017**: Compra original permanece sin cambios

## 4. Pruebas de Funcionalidad - Eliminar Compra

### 4.1 Eliminación
- [ ] **PR-DEL-001**: Seleccionar una compra en la tabla
- [ ] **PR-DEL-002**: Hacer clic en "Eliminar"
- [ ] **PR-DEL-003**: Sistema solicita confirmación
- [ ] **PR-DEL-004**: Aceptar confirmación elimina la compra
- [ ] **PR-DEL-005**: La compra desaparece de la tabla
- [ ] **PR-DEL-006**: Mensaje de éxito se muestra
- [ ] **PR-DEL-007**: La base de datos se actualiza

### 4.2 Cancelar Eliminación
- [ ] **PR-DEL-008**: Rechazar confirmación cancela eliminación
- [ ] **PR-DEL-009**: La compra permanece en la tabla
- [ ] **PR-DEL-010**: Stock de productos no se afecta

### 4.3 Stock Adjustment
- [ ] **PR-DEL-011**: Al eliminar, nota dice "stock ya fue incrementado"
- [ ] **PR-DEL-012**: Stock NO se decrementa al eliminar compra
- [ ] **PR-DEL-013**: Nota es clara sobre el comportamiento

## 5. Pruebas de Filtros

### 5.1 Filtrar por Fecha
- [ ] **PR-FILTER-001**: Seleccionar fecha inicio válida
- [ ] **PR-FILTER-002**: Seleccionar fecha fin válida (después de inicio)
- [ ] **PR-FILTER-003**: Hacer clic en "Filtrar"
- [ ] **PR-FILTER-004**: Tabla muestra solo compras en el rango de fechas
- [ ] **PR-FILTER-005**: El resultado es correcto

### 5.2 Limpiar Filtros
- [ ] **PR-FILTER-006**: Dejar campos de fecha vacíos
- [ ] **PR-FILTER-007**: Hacer clic en "Filtrar"
- [ ] **PR-FILTER-008**: Tabla muestra TODAS las compras

## 6. Pruebas de Integración

### 6.1 Integridad de Stock
- [ ] **PR-INT-001**: Crear compra con 3 productos diferentes
- [ ] **PR-INT-002**: Verificar en módulo Productos que stock se incrementó
- [ ] **PR-INT-003**: Editar compra aumentando cantidad
- [ ] **PR-INT-004**: Verificar stock se incrementó con el cambio
- [ ] **PR-INT-005**: Eliminar compra
- [ ] **PR-INT-006**: Verificar stock permanece igual (no disminuye)

### 6.2 Validación de Datos
- [ ] **PR-INT-007**: Proveedor con caracteres especiales se guarda
- [ ] **PR-INT-008**: Productos duplicados se manejan correctamente
- [ ] **PR-INT-009**: Números decimales se guardan con precisión

## 7. Casos de Error

### 7.1 Errores de Validación
- [ ] **PR-ERR-001**: Campo proveedor vacío muestra error
- [ ] **PR-ERR-002**: Sin items en la compra muestra error
- [ ] **PR-ERR-003**: Valores negativos son rechazados
- [ ] **PR-ERR-004**: Errores de formato se manejan

### 7.2 Errores de Base de Datos
- [ ] **PR-ERR-005**: Desconexión BD muestra mensaje claro
- [ ] **PR-ERR-006**: Datos corruptos se detectan
- [ ] **PR-ERR-007**: La aplicación no se crashea

## 8. Casos Edge / Límites

### 8.1 Valores Extremos
- [ ] **PR-EDGE-001**: Compra con 1000+ items
- [ ] **PR-EDGE-002**: Cantidad muy grande (999999)
- [ ] **PR-EDGE-003**: Costo muy pequeño (0.001)
- [ ] **PR-EDGE-004**: Costo muy grande (999999.999)
- [ ] **PR-EDGE-005**: Proveedor con nombre muy largo (500+ caracteres)

### 8.2 Respuesta a Situaciones Límite
- [ ] **PR-EDGE-006**: Tabla con miles de compras se carga
- [ ] **PR-EDGE-007**: Buscar en tabla grande es rápido
- [ ] **PR-EDGE-008**: Aplicación no consume exceso de memoria

## 9. Pruebas de Performance

### 9.1 Velocidad de Operaciones
- [ ] **PR-PERF-001**: Nueva compra se crea en < 1 segundo
- [ ] **PR-PERF-002**: Edición se guarda en < 1 segundo
- [ ] **PR-PERF-003**: Eliminación se completa en < 1 segundo
- [ ] **PR-PERF-004**: Tabla carga datos en < 2 segundos
- [ ] **PR-PERF-005**: Filtro se aplica instantáneamente

## 10. Rutas de Usuario Completas

### 10.1 Flujo Completo: Crear → Editar → Eliminar
```
1. Abrir módulo Compras
2. Hacer clic "Nueva"
3. Ingresar proveedor "Distribuidor ABC"
4. Agregar 2 productos diferentes
5. Guardar compra
6. Verificar en tabla
7. Seleccionar la compra creada
8. Hacer clic "Editar"
9. Cambiar proveedor a "Distribuidor XYZ"
10. Agregar un producto más
11. Guardar cambios
12. Verificar cambios en tabla
13. Seleccionar compra
14. Hacer clic "Eliminar"
15. Confirmar eliminación
16. Verificar desaparece de tabla
```

### 10.2 Flujo de Múltiples Compras
```
1. Crear 5 compras diferentes
2. Filtrar por fecha de la 3era compra
3. Editar la 2da compra
4. Filtrar nuevamente
5. Eliminar la 1era compra
6. Limpiar filtros
7. Verificar estado final correcto
```

## Criterios de Éxito

- ✓ Todas las pruebas de funcionalidad pasan
- ✓ No hay errores en logs
- ✓ UI responsiva sin lag
- ✓ Datos consistentes en BD
- ✓ Stock se actualiza correctamente
- ✓ Tamaño de ventana es apropiado
- ✓ Mensajes de usuario son claros

## Checklist de Signoff

- [ ] QA: Todas las pruebas completadas
- [ ] QA: Sin bugs críticos encontrados
- [ ] DEV: Código revisado y aprobado
- [ ] DEV: Cambios compilados sin errores
- [ ] PRODUCT: Funcionalidad cumple requisitos
- [ ] Usuario: Acepta el módulo

---

**Fecha de Creación**: 2025-03-22
**Versión**: 1.0
**Estado**: LISTO PARA PRUEBAS

