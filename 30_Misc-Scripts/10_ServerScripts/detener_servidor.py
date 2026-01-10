#!/usr/bin/env python3
"""
Script para detener el servidor web de TrackVer A&A
Busca y termina procesos de Python que estén usando el puerto 8000
"""

import subprocess
import sys
import platform
import time

def ejecutar_comando(comando):
    """Ejecuta un comando y retorna la salida"""
    try:
        result = subprocess.run(comando, shell=True, capture_output=True, text=True)
        return result.returncode == 0, result.stdout.strip(), result.stderr.strip()
    except Exception as e:
        return False, "", str(e)

def detectar_procesos_puerto(puerto=8000):
    """Detecta procesos usando un puerto específico"""
    sistema = platform.system().lower()

    if sistema == "windows":
        # En Windows usamos netstat
        exito, salida, error = ejecutar_comando(f'netstat -ano | findstr :{puerto}')
        if not exito:
            return []

        procesos = []
        for linea in salida.split('\n'):
            if f':{puerto}' in linea and 'LISTENING' in linea:
                partes = linea.split()
                if len(partes) >= 5:
                    pid = partes[-1]
                    try:
                        pid_int = int(pid)
                        procesos.append(pid_int)
                    except ValueError:
                        continue
        return procesos

    else:
        # En Linux/Mac usamos lsof o netstat
        exito, salida, error = ejecutar_comando(f'lsof -ti:{puerto}')
        if exito and salida:
            return [int(pid) for pid in salida.split('\n') if pid.strip()]
        return []

def obtener_info_proceso(pid):
    """Obtiene información de un proceso"""
    sistema = platform.system().lower()

    if sistema == "windows":
        exito, salida, error = ejecutar_comando(f'tasklist /FI "PID eq {pid}" /FO CSV')
        if exito and len(salida.split('\n')) > 1:
            linea = salida.split('\n')[1]
            partes = linea.split('","')
            if len(partes) >= 1:
                nombre = partes[0].strip('"')
                return nombre
    else:
        exito, salida, error = ejecutar_comando(f'ps -p {pid} -o comm=')
        if exito:
            return salida.strip()

    return "Desconocido"

def detener_procesos(pids):
    """Detiene una lista de procesos por PID"""
    sistema = platform.system().lower()
    procesos_detenidos = []

    for pid in pids:
        nombre = obtener_info_proceso(pid)

        if sistema == "windows":
            exito, salida, error = ejecutar_comando(f'taskkill /PID {pid} /F')
            if exito:
                procesos_detenidos.append(f"{nombre} (PID: {pid})")
        else:
            exito, salida, error = ejecutar_comando(f'kill -9 {pid}')
            if exito:
                procesos_detenidos.append(f"{nombre} (PID: {pid})")

    return procesos_detenidos

def verificar_puerto_libre(puerto=8000, max_intentos=10):
    """Verifica que el puerto esté libre esperando un poco"""
    for i in range(max_intentos):
        procesos = detectar_procesos_puerto(puerto)
        if not procesos:
            return True
        time.sleep(0.5)
    return False

def main():
    """Función principal"""
    print("🛑 TrackVer A&A - Detenedor de Servidor")
    print("=" * 40)

    puerto = 8000  # Puerto por defecto

    # Detectar procesos usando el puerto
    print(f"🔍 Buscando procesos usando el puerto {puerto}...")
    procesos = detectar_procesos_puerto(puerto)

    if not procesos:
        print(f"✅ No hay procesos usando el puerto {puerto}")
        print("ℹ️  El servidor ya está detenido o no se está ejecutando.")
        return 0

    print(f"📋 Procesos encontrados: {len(procesos)}")
    for pid in procesos:
        nombre = obtener_info_proceso(pid)
        print(f"   • {nombre} (PID: {pid})")

    # Confirmar detención
    try:
        respuesta = input("\n❓ ¿Deseas detener estos procesos? (s/N): ").lower().strip()
        if respuesta not in ['s', 'si', 'y', 'yes']:
            print("❌ Operación cancelada por el usuario.")
            return 0
    except KeyboardInterrupt:
        print("\n❌ Operación cancelada por el usuario.")
        return 0

    # Detener procesos
    print("\n🛑 Deteniendo procesos...")
    procesos_detenidos = detener_procesos(procesos)

    if procesos_detenidos:
        print("✅ Procesos detenidos exitosamente:")
        for proceso in procesos_detenidos:
            print(f"   • {proceso}")

        # Verificar que el puerto esté libre
        print(f"\n⏳ Verificando que el puerto {puerto} esté libre...")
        if verificar_puerto_libre(puerto):
            print(f"✅ Puerto {puerto} liberado correctamente.")
        else:
            print(f"⚠️  El puerto {puerto} podría seguir ocupado. Verifica manualmente.")
    else:
        print("❌ No se pudieron detener los procesos.")

    return 0

if __name__ == "__main__":
    sys.exit(main())