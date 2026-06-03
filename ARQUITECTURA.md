# 📋 Documentación Técnica - Decisiones de Arquitectura

## 🎯 Resumen Ejecutivo

Este documento explica las decisiones técnicas tomadas para el MVP del Sistema de Inventario.

---

## 1. Stack Tecnológico

### Java 17 (en lugar de Java 21)

**Decisión**: Usar Java 17

**Razones**:
- ✅ LTS (Long Term Support) más estable
- ✅ Compatibilidad perfecta con Spring Boot 4.0.2
- ✅ Amplio soporte de librerías
- ✅ Menor riesgo de incompatibilidades

**Alternativa descartada**: Java 21 (más reciente pero menos probado con el ecosistema actual)

---

## 2. Persistencia: JPA + Hibernate vs JdbcTemplate

### Opción Elegida: Spring Data JPA + Hibernate

**Dependencias**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-community-dialects</artifactId>
</dependency>
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.45.0.0</version>
</dependency>
```

**Ventajas**:
1. **Productividad**: Repositories sin código boilerplate
   ```java
   public interface ProductoRepository extends JpaRepository<Producto, Long> {
       List<Producto> findByActivoTrue();
       // Hibernate genera la query automáticamente
   }
   ```

2. **Validaciones integradas**: Bean Validation en entidades
   ```java
   @NotNull
   @DecimalMin("0.0")
   private BigDecimal costo;
   ```

3. **Relaciones ORM**: Mapeo automático de `@OneToMany`, `@ManyToOne`
   ```java
   @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
   private List<PedidoItem> items;
   ```

4. **Transacciones declarativas**: `@Transactional` sin código manual

**Desventajas conocidas**:
- ⚠️ SQLite no soporta todas las features de Hibernate (mitigado con hibernate-community-dialects)
- ⚠️ Overhead mínimo de rendimiento (irrelevante para una app de escritorio)

**Alternativa descartada: JdbcTemplate**
- ❌ Código más verboso (RowMapper manual)
- ❌ Queries SQL escritas a mano
- ❌ Sin validaciones automáticas
- ✅ Más control fino (no necesario para este MVP)

---

## 3. Base de Datos: SQLite

### ¿Por qué SQLite?

**Ventajas para este caso de uso**:
1. **Sin instalación**: No requiere servidor de BD
2. **Archivo único**: `inventario.db` es portátil
3. **Cero configuración**: Funciona out-of-the-box
4. **Perfecto para offline**: App de escritorio sin red
5. **Backups triviales**: Copiar el archivo .db

**Configuración**:
```properties
spring.datasource.url=jdbc:sqlite:inventario.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
```

**Ubicación del archivo**: Raíz del proyecto (mismo directorio que el JAR)

**Limitaciones conocidas**:
- No soporta múltiples escritores concurrentes (no es problema: app monousuario)
- Sin types complejos (JSON, arrays) - no los necesitamos

---

## 4. Migraciones: Flyway

### ¿Por qué Flyway?

**Ventajas**:
1. **Versionado de BD**: Cambios controlados en el esquema
2. **Idempotente**: Ejecuta migraciones solo una vez
3. **Historial**: Tabla `flyway_schema_history` con registro de cambios
4. **Seed data**: Datos iniciales en la migración V1

**Configuración**:
```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

**Estructura**:
```
src/main/resources/db/migration/
└── V1__init.sql   # Crea tablas + seed data
```

**Flujo de ejecución**:
1. App arranca
2. Flyway verifica si `V1__init.sql` ya se ejecutó
3. Si no, ejecuta el script
4. Registra la versión en `flyway_schema_history`

---

## 5. JavaFX + Spring Boot Integration

### Desafío: JavaFX inicia antes que Spring

**Problema**: JavaFX usa `Application.launch()` y tiene su propio ciclo de vida

**Solución implementada**:

1. **JavaFXApplication** extiende `javafx.application.Application`
   ```java
   public class JavaFXApplication extends Application {
       private ConfigurableApplicationContext context;
       
       @Override
       public void init() {
           // Inicializar Spring ANTES de mostrar UI
           this.context = new SpringApplicationBuilder()
               .sources(InventarioApplication.class)
               .run(args);
       }
   }
   ```

2. **SpringFXMLLoader** inyecta controllers
   ```java
   FXMLLoader loader = new FXMLLoader(url);
   loader.setControllerFactory(context::getBean);  // ← Magia aquí
   ```

3. **Main** lanza JavaFX
   ```java
   public static void main(String[] args) {
       Application.launch(JavaFXApplication.class, args);
   }
   ```

**Resultado**: Controllers JavaFX con inyección de dependencias
```java
@Component
@RequiredArgsConstructor  // Constructor con @Autowired automático
public class ProductosController {
    private final ProductoService productoService;  // ← Inyectado por Spring
}
```

---

## 6. Arquitectura por Capas

### Estructura

```
┌─────────────────────────────────────┐
│  UI Layer (JavaFX Controllers)      │  ← Solo maneja vistas
├─────────────────────────────────────┤
│  Application Layer (Services)       │  ← LÓGICA DE NEGOCIO
├─────────────────────────────────────┤
│  Infrastructure (Repositories)      │  ← Acceso a datos
├─────────────────────────────────────┤
│  Domain (Entities)                  │  ← Modelos de dominio
└─────────────────────────────────────┘
```

### Regla de Oro

**❌ NUNCA hacer esto**:
```java
@FXML
private void guardar() {
    // Lógica de negocio en el controller
    if (producto.getStockActual() < 0) { ... }
    productoRepository.save(producto);  // Llamar directamente al repo
}
```

**✅ SIEMPRE hacer esto**:
```java
@FXML
private void guardar() {
    try {
        productoService.guardar(producto);  // Servicio maneja todo
        mostrarInfo("Guardado OK");
    } catch (Exception e) {
        mostrarError(e.getMessage());
    }
}
```

### Beneficios

1. **Testeable**: Servicios se pueden probar sin UI
2. **Reutilizable**: Lógica de negocio independiente de JavaFX
3. **Mantenible**: Cambios en lógica no afectan controllers
4. **Escalable**: Fácil añadir API REST más tarde

---

## 7. Reglas de Negocio Implementadas

### Stock Management

**Regla 1: Pedidos no afectan stock hasta ENTREGADO**
```java
@Transactional
public Pedido marcarComoEntregado(Long pedidoId) {
    // Validar stock disponible
    for (PedidoItem item : pedido.getItems()) {
        if (!producto.tieneStockSuficiente(item.getCantidad())) {
            throw new IllegalStateException("Stock insuficiente");
        }
    }
    // Solo aquí se descuenta
    productoService.decrementarStock(...);
    pedido.setEstado(ENTREGADO);
}
```

**Regla 2: Compras incrementan stock inmediatamente**
```java
@Transactional
public Compra guardar(Compra compra) {
    Compra saved = compraRepository.save(compra);
    // Incrementar stock automáticamente
    for (CompraItem item : saved.getItems()) {
        productoService.incrementarStock(...);
    }
    return saved;
}
```

**Regla 3: Alertas de stock bajo**
```java
public boolean isStockBajo() {
    return stockActual <= stockMinimo;
}
```

### Validaciones

**A nivel de entidad**:
```java
@NotNull
@Min(0)
private Integer stockActual;
```

**A nivel de servicio**:
```java
if (codigoProducto != null && existente.isPresent()) {
    throw new IllegalArgumentException("Código de producto duplicado");
}
```

**A nivel de UI**:
```java
if (txtNombre.getText().trim().isEmpty()) {
    mostrarAdvertencia("Nombre obligatorio");
    return false;
}
```

---

## 8. Manejo de Errores

### Estrategia

1. **Servicios**: Lanzan excepciones descriptivas
   ```java
   throw new IllegalStateException("Stock insuficiente: " + mensaje);
   ```

2. **Controllers**: Capturan y muestran al usuario
   ```java
   try {
       service.operacion();
   } catch (Exception e) {
       mostrarError(e.getMessage());
       log.error("Error", e);
   }
   ```

3. **Logging**: SLF4J con niveles DEBUG/INFO/ERROR
   ```java
   @Slf4j
   public class ProductoService {
       log.info("Guardando producto: {}", producto.getNombre());
       log.error("Error al guardar", e);
   }
   ```

---

## 9. UI/UX Decisions

### Feedback Visual

1. **Productos con stock bajo**: Fila roja en tabla
   ```java
   tblProductos.setRowFactory(tv -> new TableRow<>() {
       if (item.isStockBajo()) {
           setStyle("-fx-background-color: #ffcccc;");
       }
   });
   ```

2. **Contadores en Dashboard**: Cards con colores semánticos
   - Azul: Info general
   - Rojo: Alertas (pedidos pendientes)
   - Naranja: Warnings (stock bajo)

3. **Confirmaciones**: Siempre para acciones destructivas
   ```java
   if (confirmar("¿Seguro eliminar?")) {
       service.eliminar(id);
   }
   ```

---

## 10. Backup Strategy

### Implementación Simple

```java
public String crearBackup(String directorioDestino) {
    String timestamp = LocalDateTime.now().format("yyyy-MM-dd_HH-mm");
    String nombreBackup = "inventario_" + timestamp + ".db";
    Files.copy(Path.of("inventario.db"), Path.of(directorioDestino, nombreBackup));
}
```

### ¿Por qué no base de datos en memoria?

**Descartado**: H2 en memoria
- ❌ Se pierde al cerrar la app
- ❌ Requiere backup/restore manual

**Elegido**: SQLite con archivo
- ✅ Persistencia automática
- ✅ Backup = copiar archivo
- ✅ Portabilidad total

---

## 11. Build y Deployment

### Maven

**Por qué Maven y no Gradle**:
- ✅ Ya estaba en el proyecto inicial
- ✅ Más maduro con Spring Boot
- ✅ Menor curva de aprendizaje

### Fat JAR

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

**Resultado**: `Inventario-0.0.1-SNAPSHOT.jar` con todas las dependencias

**Ejecución**:
```bash
java -jar Inventario-0.0.1-SNAPSHOT.jar
```

### jpackage (Opcional)

Para crear instalador .exe/.msi:
```bash
jpackage --input target \
         --name "Sistema Inventario" \
         --main-jar Inventario-0.0.1-SNAPSHOT.jar \
         --type msi
```

---

## 12. Escalabilidad Futura

### Diseño preparado para

1. **API REST**: Servicios ya están separados de UI
   ```java
   @RestController
   public class ProductoRestController {
       @Autowired ProductoService service;  // Reutilizar
   }
   ```

2. **Múltiples bases de datos**: Cambiar en `application.properties`
   ```properties
   spring.datasource.url=jdbc:postgresql://...
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   ```

3. **Seguridad**: Añadir Spring Security
   ```java
   @EnableWebSecurity
   public class SecurityConfig { ... }
   ```

4. **Multi-tenant**: Filtros en repositorios
   ```java
   @Query("... WHERE tenantId = :tenantId")
   ```

---

## 📊 Comparativa de Alternativas

| Aspecto | Opción Elegida | Alternativa | Razón |
|---------|----------------|-------------|-------|
| Java | 17 LTS | 21 | Estabilidad |
| Persistencia | JPA | JdbcTemplate | Productividad |
| BD | SQLite | H2 | Archivo portable |
| Migraciones | Flyway | Liquibase | Simplicidad |
| UI | JavaFX | Swing | Moderno |
| Build | Maven | Gradle | Ya presente |

---

## 🎓 Lecciones Aprendidas

1. **JavaFX + Spring**: Integración no trivial pero funcional con `controllerFactory`
2. **SQLite + JPA**: Funciona bien con hibernate-community-dialects
3. **Arquitectura por capas**: Crucial para mantener lógica separada de UI
4. **Flyway**: Esencial para gestionar cambios de BD en futuro

---

**Autor**: Arquitecto Senior Java  
**Fecha**: 2026-02-05  
**Versión**: 1.0

