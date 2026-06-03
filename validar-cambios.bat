@echo off
REM Script de Validación - Módulo de Compras
REM Este script verifica que los cambios se compilaron correctamente

setlocal enabledelayedexpansion

echo.
echo ========================================
echo VALIDACION DE CAMBIOS - MODULO COMPRAS
echo ========================================
echo.

REM Verificar que Maven está disponible
echo [INFO] Verificando Maven...
mvnw -v >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven no está disponible
    exit /b 1
)
echo [OK] Maven está disponible
echo.

REM Compilar el proyecto
echo [INFO] Compilando proyecto...
call mvnw clean compile -q
if errorlevel 1 (
    echo [ERROR] Compilación falló
    exit /b 1
)
echo [OK] Compilación exitosa
echo.

REM Empaquetar el proyecto
echo [INFO] Empaquetando proyecto...
call mvnw clean package -q -DskipTests
if errorlevel 1 (
    echo [ERROR] Empaque falló
    exit /b 1
)
echo [OK] Empaque exitoso
echo.

REM Verificar archivos modificados
echo [INFO] Verificando archivos modificados...
if not exist "src\main\resources\fxml\nueva-compra.fxml" (
    echo [ERROR] Archivo nueva-compra.fxml no existe
    exit /b 1
)
echo [OK] nueva-compra.fxml existe

if not exist "src\main\resources\fxml\compras.fxml" (
    echo [ERROR] Archivo compras.fxml no existe
    exit /b 1
)
echo [OK] compras.fxml existe

if not exist "src\main\java\com\marcofidel_dev\inventario\ui\controller\ComprasController.java" (
    echo [ERROR] Archivo ComprasController.java no existe
    exit /b 1
)
echo [OK] ComprasController.java existe
echo.

REM Verificar documentos creados
echo [INFO] Verificando documentos de referencia...
if not exist "CORRECCION_TAMAÑO_VENTANA_COMPRAS.md" (
    echo [WARN] Documento CORRECCION_TAMAÑO_VENTANA_COMPRAS.md no existe
) else (
    echo [OK] CORRECCION_TAMAÑO_VENTANA_COMPRAS.md existe
)

if not exist "PRUEBAS_MODULO_COMPRAS_COMPLETO.md" (
    echo [WARN] Documento PRUEBAS_MODULO_COMPRAS_COMPLETO.md no existe
) else (
    echo [OK] PRUEBAS_MODULO_COMPRAS_COMPLETO.md existe
)

if not exist "RESUMEN_CORRECCION_VENTANA_COMPRAS.md" (
    echo [WARN] Documento RESUMEN_CORRECCION_VENTANA_COMPRAS.md no existe
) else (
    echo [OK] RESUMEN_CORRECCION_VENTANA_COMPRAS.md existe
)
echo.

echo ========================================
echo VALIDACION COMPLETADA EXITOSAMENTE
echo ========================================
echo.
echo Cambios aplicados:
echo - nueva-compra.fxml: ScrollPane + tamaño preferido
echo - compras.fxml: Min-height para tabla
echo - ComprasController.java: Scene con tamaño 950x750
echo.
echo Próximos pasos:
echo 1. Ejecutar: run.bat
echo 2. Abrir módulo Compras
echo 3. Hacer clic en "Nueva"
echo 4. Verificar tamaño de ventana
echo.
pause

