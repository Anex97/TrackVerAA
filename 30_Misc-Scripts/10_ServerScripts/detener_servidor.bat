@echo off
REM Detenedor automatico del servidor web de TrackVer A&A
REM Uso: doble clic en este archivo

echo ========================================
echo  TrackVer A&A - Detenedor de Servidor
echo ========================================
echo.

echo Buscando procesos usando el puerto 8000...

REM Buscar procesos usando el puerto 8000
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8000 ^| findstr LISTENING') do (
    echo Deteniendo proceso PID: %%a
    taskkill /PID %%a /F >nul 2>&1
    if errorlevel 1 (
        echo Error al detener PID %%a
    ) else (
        echo Proceso %%a detenido correctamente
    )
)

echo.
echo Verificando puerto...
timeout /t 2 /nobreak >nul

REM Verificar si el puerto sigue ocupado
netstat -ano | findstr :8000 | findstr LISTENING >nul 2>&1
if errorlevel 1 (
    echo Puerto 8000 liberado correctamente
) else (
    echo El puerto 8000 podria seguir ocupado
    echo Ejecuta manualmente: taskkill /IM python.exe /F
)

echo.
echo Servidor detenido
echo.
pause