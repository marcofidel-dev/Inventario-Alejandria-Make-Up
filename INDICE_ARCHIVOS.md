# 📑 Índice Completo de Archivos - MVP Inventario

## 📂 Raíz del Proyecto

| Archivo | Descripción | Líneas |
|---------|-------------|--------|
| `pom.xml` | Configuración Maven con todas las dependencias | 120 |
| `README.md` | Documentación principal del proyecto | 420 |
| `ARQUITECTURA.md` | Decisiones técnicas detalladas | 530 |
| `ESTRUCTURA.md` | Árbol del proyecto y flujo de datos | 380 |
| `GUIA_USO.md` | Manual de usuario con ejemplos | 520 |
| `ENTREGA_FINAL.md` | Resumen ejecutivo de entrega | 380 |
| `run.bat` | Script de inicio para Windows | 35 |
| `mvnw` | Maven Wrapper para Unix | - |
| `mvnw.cmd` | Maven Wrapper para Windows | - |
| `HELP.md` | Ayuda de Spring Initializr | - |
| `.gitignore` | Archivos ignorados por Git | - |

**Archivos generados en ejecución**:
- `inventario.db` - Base de datos SQLite

---

## 📁 src/main/java/com/marcofidel_dev/inventario/

### 🎯 Entry Point

| Archivo | Descripción | Líneas |
|---------|-------------|--------|
| `InventarioApplication.java` | Punto de entrada que lanza JavaFX | 15 |

---

### 🏛️ domain/entity/

| Archivo | Descripción | Líneas |
|---------|-------------|--------|
| `Producto.java` | Entidad producto con validaciones y enum TipoProducto | 115 |
| `Pedido.java` | Entidad pedido con items y enum EstadoPedido | 90 |
| `PedidoItem.java` | Item de pedido con cálculo de subtotal | 50 |
| `Compra.java` | Entidad compra con items | 80 |
| `CompraItem.java` | Item de compra | 40 |

**Total entidades**: 375 líneas

---

### 💾 infrastructure/repository/

| Archivo | Descripción | Líneas |
|---------|-------------|--------|
| `ProductoRepository.java` | Repositorio JPA para productos con queries custom | 25 |
| `PedidoRepository.java` | Repositorio JPA para pedidos con queries custom | 20 |
| `CompraRepository.java` | Repositorio JPA para compras | 15 |

**Total repositorios**: 60 líneas

---

### 🎯 application/service/

| Archivo | Descripción | Líneas |
|---------|-------------|--------|
| `ProductoService.java` | Lógica de negocio de productos (CRUD, stock, validaciones) | 105 |
| `PedidoService.java` | Lógica de pedidos (estados, descuento de stock) | 130 |
| `CompraService.java` | Lógica de compras (incremento de stock) | 70 |
| `BackupService.java` | Crear/restaurar backups de BD | 65 |
| `DashboardService.java` | Datos agregados para dashboard | 35 |

**Total servicios**: 405 líneas

---

### 🖥️ ui/config/

| Archivo | Descripción | Líneas |
|---------|-------------|--------|
| `JavaFXApplication.java` | Integración Spring Boot con JavaFX | 55 |
| `SpringFXMLLoader.java` | Carga FXML con inyección de dependencias | 30 |

**Total config**: 85 líneas

---

### 🎮 ui/controller/

| Archivo | Descripción | Líneas |
|---------|-------------|--------|
| `MainController.java` | Controller principal con navegación | 60 |
| `DashboardController.java` | Controller del dashboard | 85 |
| `ProductosController.java` | Controller CRUD de productos | 340 |
| `PedidosController.java` | Controller de gestión de pedidos | 155 |
| `ComprasController.java` | Controller de gestión de compras | 70 |
| `BackupController.java` | Controller de backups | 95 |

**Total controllers**: 805 líneas

---

## 📁 src/main/resources/

### ⚙️ Configuración

| Archivo | Descripción | Líneas |
|---------|-------------|--------|
| `application.properties` | Configuración SQLite, JPA, Flyway, Logging | 18 |

---

### 🗄️ db/migration/

| Archivo | Descripción | Líneas |
|---------|-------------|--------|
| `V1__init.sql` | Script Flyway: crea tablas, índices, seed data | 100 |

---

### 🎨 fxml/

| Archivo | Descripción | Líneas |
|---------|-------------|--------|
| `main.fxml` | Layout principal con sidebar de navegación | 45 |
| `dashboard.fxml` | Vista dashboard con contadores y tablas | 60 |
| `productos.fxml` | Vista CRUD productos con formulario | 110 |
| `pedidos.fxml` | Vista gestión de pedidos con filtros | 45 |
| `compras.fxml` | Vista gestión de compras con filtros | 35 |
| `backup.fxml` | Vista de backups con botones | 55 |

**Total FXML**: 350 líneas

---

## 📊 Resumen por Categoría

| Categoría | Archivos | Líneas de Código |
|-----------|----------|------------------|
| **Entidades** | 5 | 375 |
| **Repositorios** | 3 | 60 |
| **Servicios** | 5 | 405 |
| **Config UI** | 2 | 85 |
| **Controllers** | 6 | 805 |
| **FXML** | 6 | 350 |
| **SQL** | 1 | 100 |
| **Config** | 1 | 18 |
| **Entry Point** | 1 | 15 |
| **TOTAL CÓDIGO** | **30** | **~2213** |

---

| Categoría | Archivos | Líneas |
|-----------|----------|--------|
| **Documentación** | 5 | 2230 |
| **Scripts** | 1 | 35 |
| **Maven** | 1 | 120 |
| **TOTAL PROYECTO** | **37** | **~4600** |

---

## 🔍 Detalle de Documentación

| Archivo | Propósito | Líneas |
|---------|-----------|--------|
| `README.md` | Instalación, features, ejecución | 420 |
| `ARQUITECTURA.md` | Decisiones técnicas, comparativas | 530 |
| `ESTRUCTURA.md` | Árbol, flujos, estadísticas | 380 |
| `GUIA_USO.md` | Manual de usuario, casos de uso | 520 |
| `ENTREGA_FINAL.md` | Resumen ejecutivo, checklist MVP | 380 |
| **TOTAL** | | **2230** |

---

## 📦 Archivos por Tecnología

### Java (22 archivos)
```
src/main/java/.../
├── InventarioApplication.java
├── domain/entity/
│   ├── Producto.java
│   ├── Pedido.java
│   ├── PedidoItem.java
│   ├── Compra.java
│   └── CompraItem.java
├── infrastructure/repository/
│   ├── ProductoRepository.java
│   ├── PedidoRepository.java
│   └── CompraRepository.java
├── application/service/
│   ├── ProductoService.java
│   ├── PedidoService.java
│   ├── CompraService.java
│   ├── BackupService.java
│   └── DashboardService.java
└── ui/
    ├── config/
    │   ├── JavaFXApplication.java
    │   └── SpringFXMLLoader.java
    └── controller/
        ├── MainController.java
        ├── DashboardController.java
        ├── ProductosController.java
        ├── PedidosController.java
        ├── ComprasController.java
        └── BackupController.java
```

### FXML (6 archivos)
```
src/main/resources/fxml/
├── main.fxml
├── dashboard.fxml
├── productos.fxml
├── pedidos.fxml
├── compras.fxml
└── backup.fxml
```

### SQL (1 archivo)
```
src/main/resources/db/migration/
└── V1__init.sql
```

### Config (2 archivos)
```
src/main/resources/
├── application.properties
└── (fxml/ y db/ dentro)
```

### Maven (1 archivo)
```
pom.xml
```

### Scripts (1 archivo)
```
run.bat
```

### Docs (5 archivos)
```
README.md
ARQUITECTURA.md
ESTRUCTURA.md
GUIA_USO.md
ENTREGA_FINAL.md
```

---

## 🎯 Archivos Clave por Funcionalidad

### Gestión de Stock
- `Producto.java` - Entidad con métodos `incrementarStock()`, `decrementarStock()`
- `ProductoService.java` - Validaciones y lógica
- `PedidoService.java` - Descuento al entregar
- `CompraService.java` - Incremento al guardar

### Integración Spring + JavaFX
- `InventarioApplication.java` - Lanza JavaFX
- `JavaFXApplication.java` - Inicializa Spring en `init()`
- `SpringFXMLLoader.java` - `setControllerFactory(context::getBean)`

### Persistencia
- `application.properties` - Datasource SQLite
- `V1__init.sql` - Esquema completo
- Repositorios - Acceso JPA
- Servicios - Transacciones

### UI
- `main.fxml` + `MainController.java` - Navegación
- Controllers específicos - Lógica de vista
- FXML específicos - Layouts

---

## 📈 Complejidad por Archivo

### Top 10 archivos más complejos

| Archivo | Líneas | Complejidad |
|---------|--------|-------------|
| 1. `ProductosController.java` | 340 | ⭐⭐⭐⭐⭐ |
| 2. `ARQUITECTURA.md` | 530 | ⭐⭐⭐⭐ |
| 3. `GUIA_USO.md` | 520 | ⭐⭐⭐⭐ |
| 4. `README.md` | 420 | ⭐⭐⭐⭐ |
| 5. `PedidosController.java` | 155 | ⭐⭐⭐ |
| 6. `PedidoService.java` | 130 | ⭐⭐⭐⭐ |
| 7. `Producto.java` | 115 | ⭐⭐⭐ |
| 8. `productos.fxml` | 110 | ⭐⭐⭐ |
| 9. `ProductoService.java` | 105 | ⭐⭐⭐ |
| 10. `V1__init.sql` | 100 | ⭐⭐⭐ |

---

## ✅ Checklist de Archivos Entregados

### Código Java
- [x] InventarioApplication.java
- [x] 5 Entidades (Producto, Pedido, PedidoItem, Compra, CompraItem)
- [x] 3 Repositorios
- [x] 5 Servicios
- [x] 2 Configs UI
- [x] 6 Controllers

### Vistas y Config
- [x] 6 archivos FXML
- [x] application.properties
- [x] V1__init.sql (migración)

### Build y Ejecución
- [x] pom.xml
- [x] run.bat
- [x] mvnw / mvnw.cmd

### Documentación
- [x] README.md
- [x] ARQUITECTURA.md
- [x] ESTRUCTURA.md
- [x] GUIA_USO.md
- [x] ENTREGA_FINAL.md

---

## 🎓 Archivos para Diferentes Audiencias

### Para el Desarrollador
1. `ARQUITECTURA.md` - Entender decisiones técnicas
2. `ESTRUCTURA.md` - Navegar el código
3. `pom.xml` - Ver dependencias
4. Código fuente - Implementación

### Para el Usuario Final
1. `README.md` - Qué es y cómo instalarlo
2. `GUIA_USO.md` - Cómo usarlo día a día
3. `run.bat` - Ejecutar fácilmente

### Para el Product Owner
1. `ENTREGA_FINAL.md` - Resumen ejecutivo
2. `README.md` - Features implementadas
3. `GUIA_USO.md` - Casos de uso

---

## 📞 Navegación Rápida

**¿Quieres ejecutar?** → `run.bat` o `README.md` sección "Ejecución"

**¿Entender arquitectura?** → `ARQUITECTURA.md`

**¿Ver estructura?** → `ESTRUCTURA.md`

**¿Aprender a usar?** → `GUIA_USO.md`

**¿Verificar entrega?** → `ENTREGA_FINAL.md`

**¿Modificar código?** → `src/main/java/.../`

**¿Modificar UI?** → `src/main/resources/fxml/`

**¿Cambiar BD?** → `src/main/resources/db/migration/V1__init.sql`

---

**Generado**: 2026-02-05  
**Total de archivos**: 37 archivos principales + dependencias  
**Total de líneas**: ~4600 (código + docs)  
**Estado**: ✅ COMPLETO

