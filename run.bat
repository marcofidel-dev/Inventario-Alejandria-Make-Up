@echo off
echo ========================================
echo  Sistema de Inventario - Iniciando...
echo ========================================
echo.

REM Verificar si Maven está instalado
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven no está instalado o no está en el PATH
    echo.
    echo Por favor instala Maven desde: https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

REM Verificar Java
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java no está instalado o no está en el PATH
    echo.
    echo Por favor instala JDK 17 desde: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

echo Verificando Java version...
java -version
echo.

echo Descargando dependencias (solo la primera vez)...
call mvn dependency:resolve

echo.
echo Iniciando aplicacion...
call mvn spring-boot:run

pause

