@echo off
echo Inicializando base de datos SQLite...
echo.

REM Eliminar base de datos corrupta si existe
if exist inventario.db del /f /q inventario.db
if exist inventario.db-wal del /f /q inventario.db-wal
if exist inventario.db-shm del /f /q inventario.db-shm

echo Base de datos antigua eliminada.
echo.
echo Ejecutando la aplicación para crear la nueva base de datos...
echo.

REM Ejecutar la aplicación para crear la base de datos
java -jar target\Inventario-0.0.1-SNAPSHOT.jar

echo.
echo Base de datos creada correctamente!
pause

