# 📦 Sistema de Inventario - MVP

Sistema de escritorio para gestión de inventario y pedidos para emprendimiento (maquillaje, bolsos y bisutería).

## 🛠️ Stack Tecnológico

- **Java 17**
- **Spring Boot 4.0.2** (sin servidor web)
- **JavaFX 21** (interfaz gráfica)
- **SQLite** (base de datos local)
- **Spring Data JPA + Hibernate** (persistencia)
- **Flyway** (migraciones de BD)
- **Lombok** (reducción de boilerplate)
- **Maven** (gestión de dependencias)

## 📁 Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/marcofidel_dev/inventario/
│   │   ├── InventarioApplication.java          # Punto de entrada
│   │   ├── domain/entity/                      # Entidades del dominio
│   │   │   ├── Producto.java
│   │   │   ├── Pedido.java
│   │   │   ├── PedidoItem.java
│   │   │   ├── Compra.java
│   │   │   └── CompraItem.java
│   │   ├── infrastructure/repository/          # Repositorios JPA
│   │   │   ├── ProductoRepository.java
│   │   │   ├── PedidoRepository.java
│   │   │   └── CompraRepository.java
│   │   ├── application/service/                # Servicios de negocio
│   │   │   ├── ProductoService.java
│   │   │   ├── PedidoService.java
│   │   │   ├── CompraService.java
│   │   │   ├── BackupService.java
│   │   │   └── DashboardService.java
│   │   └── ui/                                 # Interfaz JavaFX
│   │       ├── config/
│   │       │   ├── JavaFXApplication.java
│   │       │   └── SpringFXMLLoader.java
│   │       └── controller/
│   │           ├── MainController.java
│   │           ├── DashboardController.java
│   │           ├── ProductosController.java
│   │           ├── PedidosController.java
│   │           ├── ComprasController.java
│   │           └── BackupController.java
│   └── resources/
│       ├── application.properties              # Configuración
│       ├── db/migration/
│       │   └── V1__init.sql                   # Script inicial de BD
│       └── fxml/                               # Vistas JavaFX
│           ├── main.fxml
│           ├── dashboard.fxml
│           ├── productos.fxml
│           ├── pedidos.fxml
│           ├── compras.fxml
│           └── backup.fxml
```

## 🚀 Instalación y Ejecución

### Requisitos Previos

- **JDK 17** o superior instalado
- **Maven 3.6+** instalado
- **Variable de entorno JAVA_HOME** configurada

### Pasos para ejecutar

1. **Clonar o descargar el proyecto**

2. **Descargar dependencias y compilar**:
   ```bash
   mvn clean install
   ```

3. **Ejecutar la aplicación**:
   ```bash
   mvn spring-boot:run
   ```

   O alternativamente:
   ```bash
   mvn javafx:run
   ```

### Generar JAR ejecutable

```bash
mvn clean package
```

El JAR se generará en `target/Inventario-0.0.1-SNAPSHOT.jar`

Para ejecutar el JAR:
```bash
java -jar target/Inventario-0.0.1-SNAPSHOT.jar
```

### Empaquetado nativo (opcional con jpackage)

Si tienes JDK 17+ con jpackage, puedes crear un instalador nativo:

```bash
jpackage --input target --name "Sistema Inventario" --main-jar Inventario-0.0.1-SNAPSHOT.jar --main-class com.marcofidel_dev.inventario.InventarioApplication --type msi --win-console
```

## 🎯 Funcionalidades

### 1. Dashboard
- **Contadores rápidos**: Total productos, pedidos pendientes, productos con stock bajo
- **Tabla de productos con stock bajo** (stock actual ≤ stock mínimo)
- **Tabla de pedidos pendientes**
- Botón de actualización

### 2. Productos
- **CRUD completo** de productos
- **Tipos**: Maquillaje, Bolso, Bisutería
- **Campos obligatorios**: tipo, nombre, costo, precio de venta, stock actual, stock mínimo
- **Campos opcionales específicos**:
  - Maquillaje: tono/color, fecha de vencimiento
  - Todos: color, tamaño, material, talla
- **Búsqueda y filtros**:
  - Por nombre o SKU
  - Por tipo de producto
  - Solo productos con stock bajo
- **Alertas visuales**: Filas rojas para productos con stock bajo
- **Validaciones**: SKU único, números positivos, campos requeridos

### 3. Pedidos
- **Gestión de pedidos** (ventas a clientas)
- **Información de clienta**: nombre, WhatsApp, Instagram (opcional)
- **Estados**: PENDIENTE, ENTREGADO, CANCELADO
- **Regla clave**: El stock se descuenta SOLO al marcar como ENTREGADO
- **Validación**: Verifica stock suficiente antes de entregar
- **Filtros**: Por estado y rango de fechas
- **Acciones**: Marcar como entregado, cancelar

### 4. Compras
- **Registro de compras** (reposiciones de proveedores)
- **Información**: Proveedor (opcional), fecha, productos y cantidades
- **Regla clave**: Al guardar una compra, el stock se INCREMENTA automáticamente
- **Filtros**: Por rango de fechas
- **Vista de historial** ordenado por fecha

### 5. Backup
- **Crear backup**: Copia el archivo `inventario.db` con timestamp
  - Formato: `inventario_YYYY-MM-DD_HH-mm.db`
- **Restaurar backup**: Selecciona archivo .db para restaurar
  - ⚠️ **Advertencia**: Crea backup de seguridad antes de restaurar
  - ⚠️ **Requiere reiniciar** la aplicación después de restaurar

## 💾 Base de Datos

### Ubicación
- El archivo SQLite se crea en: `inventario.db` (raíz del proyecto)
- **Portabilidad**: Puedes mover el archivo .db junto con el JAR

### Migraciones
- **Flyway** gestiona las migraciones automáticamente
- Script inicial: `src/main/resources/db/migration/V1__init.sql`
- **Datos seed**: 5 productos de ejemplo incluidos

### Esquema

#### Tabla `producto`
- Información completa del producto
- Campos específicos por tipo
- Control de stock con alertas

#### Tabla `pedido` y `pedido_item`
- Relación 1:N
- Almacena precio al momento del pedido
- Tracking de estado

#### Tabla `compra` y `compra_item`
- Relación 1:N
- Historial de reposiciones

## 🔧 Decisiones Técnicas

### ¿Por qué JPA con SQLite?

**Opción elegida**: Spring Data JPA + Hibernate con `hibernate-community-dialects`

**Ventajas**:
- ✅ Mapeo objeto-relacional automático
- ✅ Repositorios sin código boilerplate
- ✅ Queries con métodos declarativos
- ✅ Validaciones de entidades con Bean Validation
- ✅ Gestión automática de transacciones

**Alternativa no elegida**: JdbcTemplate
- Más verboso, requiere mapeo manual
- Para un MVP, JPA ofrece mejor productividad

### Integración Spring + JavaFX

1. **JavaFXApplication** extiende `javafx.application.Application`
2. En el método `init()` se inicializa Spring Boot
3. **SpringFXMLLoader** inyecta controllers usando ApplicationContext
4. `FXMLLoader.setControllerFactory(context::getBean)` permite inyección de dependencias

### Arquitectura por Capas

```
UI (JavaFX Controllers) 
    ↓
Application (Services) ← LÓGICA DE NEGOCIO AQUÍ
    ↓
Infrastructure (Repositories)
    ↓
Domain (Entities)
```

**Principio clave**: Los controllers JavaFX son **delgados**, solo gestionan la UI. Toda la lógica está en los servicios.

## 📝 Reglas de Negocio Importantes

### Stock
1. **Productos**: Se alerta visualmente cuando `stock_actual ≤ stock_minimo`
2. **Pedidos**: Stock se decrementa SOLO al cambiar estado a ENTREGADO
3. **Compras**: Stock se incrementa INMEDIATAMENTE al guardar

### Validaciones
- Números no negativos (costo, precio, stock)
- SKU único (si se proporciona)
- Campos obligatorios marcados con `*`
- Validación de stock suficiente antes de entregar pedido

### Pedidos
- Estado inicial: PENDIENTE
- No se puede entregar un pedido CANCELADO
- No se puede cancelar un pedido ENTREGADO
- No se puede eliminar un pedido ENTREGADO (ya afectó el stock)

## 🧪 Datos de Prueba

La aplicación incluye 5 productos seed:
1. Labial Mate MAC (stock bajo: 2 ≤ 5)
2. Base Líquida Maybelline (stock bajo)
3. Bolso Tote Clásico
4. Aretes Largos Dorados
5. Collar Perlas (stock bajo)

## 🐛 Troubleshooting

### Error: "Cannot resolve javafx"
**Solución**: Ejecuta `mvn clean install` para descargar dependencias

### Error: "Table 'producto' does not exist"
**Solución**: Flyway crea las tablas automáticamente. Verifica `application.properties`

### La aplicación no inicia en Windows
**Solución**: Verifica que JAVA_HOME apunte a JDK 17, no JRE

### JavaFX no muestra la interfaz
**Solución**: Asegúrate de ejecutar con `mvn javafx:run` o que el JAR incluya las dependencias de JavaFX

## 📚 Próximas Mejoras (fuera del MVP)

- [ ] Añadir formularios completos para crear/editar Pedidos y Compras
- [ ] Reportes PDF de pedidos y compras
- [ ] Gráficos de ventas
- [ ] Gestión de múltiples usuarios
- [ ] Exportar datos a Excel
- [ ] Sincronización en la nube (opcional)
- [ ] Impresión de etiquetas con códigos de barras

## 📞 Soporte

Para problemas o dudas:
- Revisa los logs en consola (nivel DEBUG activado)
- Verifica el archivo `inventario.db` existe
- Consulta la documentación de Spring Boot y JavaFX

---

**Versión**: 0.0.1-SNAPSHOT  
**Última actualización**: 2026-02-05  
**Desarrollado con**: ❤️ + ☕

