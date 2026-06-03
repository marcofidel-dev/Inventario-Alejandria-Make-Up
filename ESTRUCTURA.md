# 🌳 Estructura Completa del Proyecto

```
Inventario/
│
├── 📄 pom.xml                                     # Maven dependencies y plugins
├── 📄 README.md                                   # Documentación principal
├── 📄 ARQUITECTURA.md                             # Decisiones técnicas
├── 📄 run.bat                                     # Script de inicio (Windows)
├── 📄 mvnw                                        # Maven Wrapper (Unix)
├── 📄 mvnw.cmd                                    # Maven Wrapper (Windows)
├── 📄 HELP.md                                     # Ayuda generada por Spring Initializr
├── 📄 inventario.db                               # Base de datos SQLite (se crea al ejecutar)
│
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/com/marcofidel_dev/inventario/
│   │   │   │
│   │   │   ├── 📄 InventarioApplication.java     # ⭐ PUNTO DE ENTRADA - Lanza JavaFX
│   │   │   │
│   │   │   ├── 📁 domain/entity/                  # 🏛️ ENTIDADES DEL DOMINIO
│   │   │   │   ├── 📄 Producto.java              # Entidad producto con enum TipoProducto
│   │   │   │   ├── 📄 Pedido.java                # Entidad pedido con enum EstadoPedido
│   │   │   │   ├── 📄 PedidoItem.java            # Items de un pedido (relación N:1)
│   │   │   │   ├── 📄 Compra.java                # Entidad compra (reposición)
│   │   │   │   └── 📄 CompraItem.java            # Items de una compra (relación N:1)
│   │   │   │
│   │   │   ├── 📁 infrastructure/repository/     # 💾 ACCESO A DATOS (JPA)
│   │   │   │   ├── 📄 ProductoRepository.java    # CRUD + búsquedas de productos
│   │   │   │   ├── 📄 PedidoRepository.java      # CRUD + filtros de pedidos
│   │   │   │   └── 📄 CompraRepository.java      # CRUD + filtros de compras
│   │   │   │
│   │   │   ├── 📁 application/service/           # 🎯 LÓGICA DE NEGOCIO
│   │   │   │   ├── 📄 ProductoService.java       # Gestión productos, stock, validaciones
│   │   │   │   ├── 📄 PedidoService.java         # Gestión pedidos, descuento de stock
│   │   │   │   ├── 📄 CompraService.java         # Gestión compras, incremento de stock
│   │   │   │   ├── 📄 BackupService.java         # Crear/restaurar backups de BD
│   │   │   │   └── 📄 DashboardService.java      # Datos agregados para dashboard
│   │   │   │
│   │   │   └── 📁 ui/                            # 🖥️ INTERFAZ JAVAFX
│   │   │       │
│   │   │       ├── 📁 config/                     # Configuración JavaFX + Spring
│   │   │       │   ���── 📄 JavaFXApplication.java  # Inicializa Spring Boot dentro de JavaFX
│   │   │       │   └── 📄 SpringFXMLLoader.java   # Carga FXML con inyección de dependencias
│   │   │       │
│   │   │       └── 📁 controller/                 # Controllers JavaFX (delgados, sin lógica)
│   │   │           ├── 📄 MainController.java     # Controller principal con navegación
│   │   │           ├── 📄 DashboardController.java # Vista del dashboard
│   │   │           ├── 📄 ProductosController.java # CRUD de productos
│   │   │           ├── 📄 PedidosController.java  # Gestión de pedidos
│   │   │           ├── 📄 ComprasController.java  # Gestión de compras
│   │   │           └── 📄 BackupController.java   # Interfaz de backups
│   │   │
│   │   └── 📁 resources/
│   │       │
│   │       ├── 📄 application.properties          # ⚙️ CONFIGURACIÓN
│   │       │                                      # - Datasource SQLite
│   │       │                                      # - JPA/Hibernate
│   │       │                                      # - Flyway
│   │       │                                      # - Logging
│   │       │
│   │       ├── 📁 db/migration/                   # 🗄️ MIGRACIONES FLYWAY
│   │       │   └── 📄 V1__init.sql               # Script inicial: tablas + seed data
│   │       │
│   │       └── 📁 fxml/                           # 🎨 VISTAS JAVAFX (XML)
│   │           ├── 📄 main.fxml                   # Layout principal con sidebar
│   │           ├── 📄 dashboard.fxml              # Vista dashboard
│   │           ├── 📄 productos.fxml              # Vista CRUD productos
│   │           ├── 📄 pedidos.fxml                # Vista gestión pedidos
│   │           ├── 📄 compras.fxml                # Vista gestión compras
│   │           └── 📄 backup.fxml                 # Vista backups
│   │
│   └── 📁 test/
│       └── 📁 java/com/marcofidel_dev/inventario/
│           └── 📄 InventarioApplicationTests.java # Test básico de Spring Boot
│
└── 📁 target/                                     # (generado por Maven)
    ├── 📄 Inventario-0.0.1-SNAPSHOT.jar           # JAR ejecutable (después de mvn package)
    └── 📁 classes/                                # Clases compiladas
```

---

## 📊 Estadísticas del Proyecto

### Archivos por Tipo

| Tipo | Cantidad | Descripción |
|------|----------|-------------|
| Java | 22 | Clases (entities, repos, services, controllers) |
| FXML | 6 | Vistas JavaFX |
| SQL | 1 | Migración Flyway |
| Properties | 1 | Configuración |
| XML | 1 | pom.xml |
| Markdown | 3 | Documentación |
| Batch | 1 | Script de inicio |

### Líneas de Código (estimado)

| Componente | LOC |
|------------|-----|
| Entidades | ~400 |
| Repositorios | ~80 |
| Servicios | ~450 |
| Controllers | ~800 |
| FXML | ~400 |
| Config | ~100 |
| SQL | ~100 |
| **TOTAL** | **~2330** |

---

## 🔍 Archivos Clave

### 1. Punto de Entrada
```
src/main/java/.../InventarioApplication.java
```
- Lanza JavaFX con `Application.launch()`

### 2. Configuración de BD
```
src/main/resources/application.properties
```
- URL de SQLite
- Dialect de Hibernate
- Configuración de Flyway

### 3. Migración Inicial
```
src/main/resources/db/migration/V1__init.sql
```
- Crea todas las tablas
- Inserta 5 productos seed

### 4. Vista Principal
```
src/main/resources/fxml/main.fxml
```
- Sidebar con navegación
- StackPane para contenido dinámico

### 5. Integración Spring + JavaFX
```
src/main/java/.../ui/config/JavaFXApplication.java
src/main/java/.../ui/config/SpringFXMLLoader.java
```
- Inicializa Spring antes de UI
- Inyecta controllers con `setControllerFactory`

---

## 📦 Dependencias Maven

### Core
- `spring-boot-starter` (Spring Boot base)
- `spring-boot-starter-data-jpa` (Persistencia)
- `spring-boot-starter-validation` (Bean Validation)

### Base de Datos
- `sqlite-jdbc:3.45.0.0` (Driver SQLite)
- `hibernate-community-dialects` (Dialect para SQLite)
- `flyway-core` (Migraciones)

### JavaFX
- `javafx-controls:21.0.2` (Controles UI)
- `javafx-fxml:21.0.2` (Soporte FXML)

### Utilidades
- `lombok` (Reduce boilerplate)

### Testing
- `spring-boot-starter-test` (JUnit, Mockito, etc.)

---

## 🎯 Flujo de Datos

```
┌──────────────────┐
│   Usuario hace   │
│   clic en UI     │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  JavaFX          │  @FXML
│  Controller      │  private void guardar()
└────────┬─────────┘
         │ llama
         ▼
┌──────────────────┐
│  Service         │  @Service
│  (Lógica de      │  @Transactional
│   negocio)       │  productoService.guardar()
└────────┬─────────┘
         │ usa
         ▼
┌──────────────────┐
│  Repository      │  extends JpaRepository<Producto, Long>
│  (Acceso a BD)   │  findByActivoTrue()
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  Hibernate       │  Genera SQL
│  + SQLite        │  INSERT INTO producto...
└──────────────────┘
         │
         ▼
┌──────────────────┐
│  inventario.db   ��  Archivo en disco
└──────────────────┘
```

---

## 🚀 Comandos Útiles

### Compilar
```bash
mvn clean compile
```

### Descargar dependencias
```bash
mvn dependency:resolve
```

### Empaquetar JAR
```bash
mvn clean package
```

### Ejecutar aplicación
```bash
mvn spring-boot:run
# o
mvn javafx:run
# o (Windows)
run.bat
```

### Limpiar
```bash
mvn clean
```

---

## 📝 Archivos Generados en Ejecución

Al ejecutar la aplicación por primera vez:

1. **inventario.db**
   - Base de datos SQLite
   - Se crea en la raíz del proyecto

2. **flyway_schema_history**
   - Tabla dentro de inventario.db
   - Registra migraciones aplicadas

3. **Logs en consola**
   - Nivel DEBUG activado
   - Queries SQL visibles

---

## 🔧 Personalización

### Cambiar puerto (si se agrega API REST)
```properties
# application.properties
server.port=8080
```

### Cambiar ubicación de BD
```properties
spring.datasource.url=jdbc:sqlite:C:/datos/inventario.db
```

### Desactivar Flyway
```properties
spring.flyway.enabled=false
```

### Cambiar nivel de logging
```properties
logging.level.com.marcofidel_dev.inventario=INFO
```

---

**Última actualización**: 2026-02-05

