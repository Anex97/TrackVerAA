#Requires -Version 5.1

<#
.SYNOPSIS
    Script de PowerShell para ejecutar el servidor web de TrackVer A&A

.DESCRIPTION
    Este script facilita la ejecución del servidor web local para el frontend
    de TrackVer A&A. Verifica dependencias, configura el entorno y abre el navegador.

.PARAMETER Puerto
    Puerto en el que se ejecutará el servidor (por defecto: 8000)

.PARAMETER NoBrowser
    No abrir el navegador automáticamente

.EXAMPLE
    .\ejecutar_servidor.ps1

.EXAMPLE
    .\ejecutar_servidor.ps1 -Puerto 8080

.EXAMPLE
    .\ejecutar_servidor.ps1 -NoBrowser
#>

param(
    [int]$Puerto = 8000,
    [switch]$NoBrowser
)

# Configuración
$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
# La raíz del proyecto está 2 niveles arriba (30_Misc-Scripts/10_ServerScripts -> raíz)
$projectRoot = Split-Path -Parent (Split-Path -Parent $scriptDir)
$frontendDir = Join-Path $projectRoot "20_FrontEnd"

# Función para escribir mensajes coloreados
function Write-ColorMessage {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

# Banner
Write-ColorMessage "========================================" "Cyan"
Write-ColorMessage "   TrackVer A&A - Servidor Web Local" "Cyan"
Write-ColorMessage "========================================" "Cyan"
Write-Host

# Verificar Python
try {
    $pythonVersion = python --version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-ColorMessage "✅ Python encontrado: $pythonVersion" "Green"
    } else {
        throw "Python no encontrado"
    }
} catch {
    Write-ColorMessage "❌ Error: Python no está disponible en el sistema" "Red"
    Write-Host
    Write-ColorMessage "📋 Solución:" "Yellow"
    Write-Host "   1. Instala Python desde: https://python.org"
    Write-Host "   2. Asegúrate de marcar 'Add Python to PATH' durante la instalación"
    Write-Host
    Read-Host "Presiona Enter para salir"
    exit 1
}

# Verificar directorio del frontend
if (-not (Test-Path $frontendDir)) {
    Write-ColorMessage "❌ Error: No se encuentra el directorio '20_FrontEnd'" "Red"
    Write-Host
    Write-ColorMessage "📋 Solución:" "Yellow"
    Write-Host "   Asegúrate de que este script esté en la raíz del proyecto TrackVerAA"
    Write-Host "   Directorio actual: $scriptDir"
    Write-Host
    Read-Host "Presiona Enter para salir"
    exit 1
}

Write-ColorMessage "✅ Directorio del proyecto verificado" "Green"
Write-Host

# Mostrar configuración
Write-ColorMessage "🔧 Configuración:" "Magenta"
Write-Host "   Puerto: $Puerto"
Write-Host "   Directorio: $frontendDir"
if ($NoBrowser) {
    Write-Host "   Navegador automático: Deshabilitado"
} else {
    Write-Host "   Navegador automático: Habilitado"
}
Write-Host

# Función para abrir navegador
function Open-Browser {
    param([int]$Delay = 2)

    Start-Sleep -Seconds $Delay
    try {
        Start-Process "http://localhost:$Puerto/10_HTML/"
        Write-ColorMessage "🌐 Navegador abierto en: http://localhost:$Puerto/10_HTML/" "Green"
    } catch {
        Write-ColorMessage "⚠️  No se pudo abrir el navegador automáticamente" "Yellow"
    }
}

# Abrir navegador en background si no se deshabilitó
if (-not $NoBrowser) {
    $browserJob = Start-Job -ScriptBlock ${function:Open-Browser} -ArgumentList 2
}

# Ejecutar servidor
try {
    Write-ColorMessage "🚀 Iniciando servidor..." "Green"
    Write-ColorMessage "📋 Presiona Ctrl+C para detener el servidor" "Yellow"
    Write-Host ("-" * 50)

    # Cambiar al directorio del frontend y ejecutar servidor
    Push-Location $frontendDir
    python -m http.server $Puerto

} catch {
    Write-ColorMessage "`n❌ Error al ejecutar el servidor: $($_.Exception.Message)" "Red"
    exit 1

} finally {
    Pop-Location

    # Limpiar job del navegador si existe
    if ($browserJob) {
        Stop-Job $browserJob -ErrorAction SilentlyContinue
        Remove-Job $browserJob -ErrorAction SilentlyContinue
    }
}

Write-Host
Write-ColorMessage "👋 Servidor detenido" "Cyan"