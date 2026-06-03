@echo off
REM ========================================
REM Scripts de Utilidad para Base de Datos
REM ========================================

:menu
cls
echo ========================================
echo   GESTION DE BASE DE DATOS - INVENTARIO
echo ========================================
echo.
echo 1. Inicializar/Recrear Base de Datos
echo 2. Respaldar Base de Datos
echo 3. Restaurar Base de Datos
echo 4. Ver Informacion de la Base de Datos
echo 5. Limpiar Archivos Temporales WAL
echo 6. Ejecutar Aplicacion
echo 7. Compilar Proyecto
echo 8. Salir
echo.
set /p opcion="Seleccione una opcion (1-8): "

if "%opcion%"=="1" goto inicializar
if "%opcion%"=="2" goto respaldar
if "%opcion%"=="3" goto restaurar
if "%opcion%"=="4" goto info
if "%opcion%"=="5" goto limpiar
if "%opcion%"=="6" goto ejecutar
if "%opcion%"=="7" goto compilar
if "%opcion%"=="8" goto salir
goto menu

:inicializar
echo.
echo Inicializando base de datos...
if exist inventario.db (
    echo Creando respaldo antes de eliminar...
    copy inventario.db inventario_backup_%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%%time:~6,2%.db 2>nul
    del /f /q inventario.db
)
if exist inventario.db-wal del /f /q inventario.db-wal
if exist inventario.db-shm del /f /q inventario.db-shm
echo Base de datos lista para ser recreada.
echo Compile y ejecute el proyecto para crear una nueva base de datos.
pause
goto menu

:respaldar
echo.
echo Creando respaldo de la base de datos...
if not exist inventario.db (
    echo ERROR: No existe la base de datos inventario.db
    pause
    goto menu
)
set fecha=%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set fecha=%fecha: =0%
copy inventario.db "backups\inventario_%fecha%.db"
if not exist backups mkdir backups
copy inventario.db "backups\inventario_%fecha%.db"
echo Respaldo creado: backups\inventario_%fecha%.db
pause
goto menu

:restaurar
echo.
echo Restaurar base de datos desde respaldo...
echo.
if not exist backups (
    echo ERROR: No existe la carpeta de respaldos
    pause
    goto menu
)
echo Archivos de respaldo disponibles:
dir /b backups\*.db
echo.
set /p archivo="Ingrese el nombre del archivo a restaurar: "
if not exist "backups\%archivo%" (
    echo ERROR: El archivo no existe
    pause
    goto menu
)
if exist inventario.db (
    echo Creando respaldo de seguridad...
    copy inventario.db inventario_pre_restore.db
)
copy "backups\%archivo%" inventario.db
echo Base de datos restaurada exitosamente
pause
goto menu

:info
echo.
echo Informacion de la Base de Datos:
echo ================================
if exist inventario.db (
    echo Estado: Base de datos encontrada
    for %%A in (inventario.db) do echo Tamano: %%~zA bytes
    echo Ubicacion: %CD%\inventario.db
    echo.
    if exist inventario.db-wal (
        echo Modo WAL: Activo
        for %%A in (inventario.db-wal) do echo WAL size: %%~zA bytes
    ) else (
        echo Modo WAL: No detectado
    )
) else (
    echo Estado: Base de datos NO encontrada
    echo La base de datos se creara al ejecutar la aplicacion
)
echo.
pause
goto menu

:limpiar
echo.
echo Limpiando archivos temporales...
if exist inventario.db-wal (
    del /f /q inventario.db-wal
    echo Archivo WAL eliminado
)
if exist inventario.db-shm (
    del /f /q inventario.db-shm
    echo Archivo SHM eliminado
)
echo Limpieza completada
pause
goto menu

:ejecutar
echo.
echo Ejecutando aplicacion...
if not exist target\Inventario-0.0.1-SNAPSHOT.jar (
    echo ERROR: El JAR no existe. Compile primero el proyecto.
    pause
    goto menu
)
java -jar target\Inventario-0.0.1-SNAPSHOT.jar
pause
goto menu

:compilar
echo.
echo Compilando proyecto...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo ERROR: La compilacion fallo
) else (
    echo Compilacion exitosa
)
pause
goto menu

:salir
echo.
echo Saliendo...
exit


