@echo off
REM Script para ejecutar el servidor web de TrackVer A&A
REM Uso: doble clic en este archivo o ejecutar desde línea de comandos

echo ========================================
echo    TrackVer A&A - Servidor Web Local
echo ========================================
echo.

REM Verificar si Python está instalado
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Error: Python no está instalado o no está en el PATH
    echo.
    echo 📋 Solución:
    echo    1. Instala Python desde: https://python.org
    echo    2. Asegúrate de marcar "Add Python to PATH" durante la instalación
    echo.
    pause
    exit /b 1
)

REM Verificar que estamos en el directorio correcto
REM Obtener la raíz del proyecto (2 niveles arriba desde la ubicación del script)
for %%i in ("%~dp0..\..\") do set "project_root=%%~fi"
set "frontend_dir=%project_root%20_FrontEnd"

if not exist "%frontend_dir%" (
    echo ❌ Error: No se encuentra el directorio '20_FrontEnd'
    echo.
    echo 📋 Información de depuración:
    echo    Ubicación del script: %~dp0
    echo    Raíz del proyecto: %project_root%
    echo    Directorio frontend: %frontend_dir%
    echo.
    echo    Asegúrate de que la estructura del proyecto esté completa
    echo.
    pause
    exit /b 1
)

echo ✅ Python encontrado
echo ✅ Directorio del proyecto verificado
echo.

REM Ejecutar el script de Python
echo 🚀 Iniciando servidor...
python "%~dp0servidor.py"

REM Si el script falla, mostrar mensaje de error
if errorlevel 1 (
    echo.
    echo ❌ El servidor se detuvo con errores
    pause
)