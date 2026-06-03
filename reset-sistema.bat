@echo off
echo ========================================
echo RESET Y PRUEBA DEL SISTEMA  
echo ========================================
echo.
echo [1] Cerrando aplicaciones Java...
taskkill /F /IM java.exe /T 2>nul
timeout /t 2 >nul
echo [2] Eliminando base de datos...
if exist inventario.db del inventario.db
echo [OK] Sistema listo
