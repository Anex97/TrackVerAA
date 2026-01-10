#Requires -Version 5.1

<#
.SYNOPSIS
    Detiene el servidor web de TrackVer A&A

.DESCRIPTION
    Este script busca y detiene automáticamente todos los procesos
    que estén usando el puerto 8000 (servidor web).

.PARAMETER Puerto
    Puerto a liberar (por defecto: 8000)

.PARAMETER Force
    No pedir confirmación antes de detener procesos

.EXAMPLE
    .\detener_servidor.ps1

.EXAMPLE
    .\detener_servidor.ps1 -Puerto 8080

.EXAMPLE
    .\detener_servidor.ps1 -Force
#>

param(
    [int]$Puerto = 8000,
    [switch]$Force
)

# Configuración
$ErrorActionPreference = "Stop"

# Función para escribir mensajes coloreados
function Write-ColorMessage {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

# Banner
Write-ColorMessage "========================================" "Red"
Write-ColorMessage "   TrackVer A&A - Detenedor de Servidor" "Red"
Write-ColorMessage "========================================" "Red"
Write-Host

# Función para obtener procesos usando un puerto
function Get-ProcessesUsingPort {
    param([int]$Port)

    try {
        $netstat = netstat -ano | Select-String ":$Port" | Select-String "LISTENING"
        $pids = @()

        foreach ($line in $netstat) {
            $parts = $line -split '\s+'
            if ($parts.Length -ge 5) {
                $pid = $parts[-1]
                if ($pid -match '^\d+$') {
                    $pids += [int]$pid
                }
            }
        }

        return $pids | Select-Object -Unique
    } catch {
        Write-ColorMessage "❌ Error al buscar procesos: $($_.Exception.Message)" "Red"
        return @()
    }
}

# Función para obtener información de proceso
function Get-ProcessInfo {
    param([int]$Pid)

    try {
        $process = Get-Process -Id $Pid -ErrorAction SilentlyContinue
        if ($process) {
            return "$($process.ProcessName).exe"
        }
    } catch {
        # Proceso no encontrado
    }
    return "Proceso desconocido"
}

# Buscar procesos
Write-ColorMessage "🔍 Buscando procesos usando el puerto $Puerto..." "Yellow"
$pids = Get-ProcessesUsingPort -Port $Puerto

if ($pids.Count -eq 0) {
    Write-ColorMessage "✅ No hay procesos usando el puerto $Puerto" "Green"
    Write-ColorMessage "ℹ️  El servidor ya está detenido o no se está ejecutando." "Cyan"
    exit 0
}

Write-ColorMessage "📋 Procesos encontrados: $($pids.Count)" "Magenta"
foreach ($pid in $pids) {
    $processName = Get-ProcessInfo -Pid $pid
    Write-Host "   • $processName (PID: $pid)"
}

# Confirmar detención
if (-not $Force) {
    Write-Host
    $respuesta = Read-Host "❓ ¿Deseas detener estos procesos? (s/N)"
    if ($respuesta -notmatch "^(s|si|y|yes)$") {
        Write-ColorMessage "❌ Operación cancelada por el usuario." "Yellow"
        exit 0
    }
}

# Detener procesos
Write-Host
Write-ColorMessage "🛑 Deteniendo procesos..." "Red"
$procesosDetenidos = @()

foreach ($pid in $pids) {
    $processName = Get-ProcessInfo -Pid $pid

    try {
        Stop-Process -Id $pid -Force -ErrorAction Stop
        $procesosDetenidos += "$processName (PID: $pid)"
        Write-ColorMessage "✅ $processName (PID: $pid) detenido" "Green"
    } catch {
        Write-ColorMessage "❌ Error al detener $processName (PID: $pid): $($_.Exception.Message)" "Red"
    }
}

# Verificar puerto liberado
Write-Host
Write-ColorMessage "⏳ Verificando puerto..." "Yellow"
Start-Sleep -Seconds 2

$pidsRestantes = Get-ProcessesUsingPort -Port $Puerto
if ($pidsRestantes.Count -eq 0) {
    Write-ColorMessage "✅ Puerto $Puerto liberado correctamente." "Green"
} else {
    Write-ColorMessage "⚠️  Algunos procesos podrían seguir activos:" "Yellow"
    foreach ($pid in $pidsRestantes) {
        $processName = Get-ProcessInfo -Pid $pid
        Write-Host "   • $processName (PID: $pid)"
    }
}

Write-Host
Write-ColorMessage "👋 Servidor detenido" "Cyan"