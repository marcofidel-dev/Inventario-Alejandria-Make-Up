@echo off
echo ========================================
echo Diagnostico del Sistema de Inventario
echo ========================================
echo.

echo Verificando archivos...
if exist "inventario.db" (
    echo [OK] Base de datos encontrada: inventario.db
) else (
    echo [ERROR] No se encuentra la base de datos
)

if exist "target\Inventario-0.0.1-SNAPSHOT.jar" (
    echo [OK] JAR encontrado
) else (
    echo [ERROR] JAR no encontrado
)

echo.
echo ========================================
echo Verificando estructura de BD...
echo ========================================

sqlite3 inventario.db ".schema producto" > schema_producto.txt
sqlite3 inventario.db ".schema pedido" > schema_pedido.txt
sqlite3 inventario.db ".schema pedido_item" > schema_pedido_item.txt

echo [INFO] Estructura de tabla 'producto':
type schema_producto.txt
echo.

echo [INFO] Estructura de tabla 'pedido':
type schema_pedido.txt
echo.

echo [INFO] Estructura de tabla 'pedido_item':
type schema_pedido_item.txt
echo.

echo ========================================
echo Verificando datos...
echo ========================================

sqlite3 inventario.db "SELECT COUNT(*) as total_productos FROM producto;" > count_productos.txt
sqlite3 inventario.db "SELECT COUNT(*) as total_pedidos FROM pedido;" > count_pedidos.txt

echo [INFO] Total de productos:
type count_productos.txt

echo [INFO] Total de pedidos:
type count_pedidos.txt

echo.
echo ========================================
echo Verificando productos con stock...
echo ========================================
sqlite3 inventario.db "SELECT id, nombre, codigo_producto, stock_actual FROM producto WHERE activo = 1 LIMIT 5;" -header -column

echo.
echo ========================================
echo Diagnostico completado
echo ========================================
pause

