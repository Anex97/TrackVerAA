#!/usr/bin/env python3
"""
Servidor HTTP para TrackVer A&A Frontend
Script para ejecutar fácilmente el servidor web local
"""

import os
import sys
import subprocess
import webbrowser
import time
import configparser
from pathlib import Path

def cargar_configuracion():
    """Carga la configuración desde config.ini si existe"""
    config = configparser.ConfigParser()
    config_path = Path(__file__).parent.parent.parent / "config.ini"  # Subir dos niveles para llegar a la raíz

    # Valores por defecto
    configuracion = {
        'puerto': 8000,
        'delay_navegador': 2,
        'abrir_navegador': True,
        'pagina_inicial': '/10_HTML/'  # Nueva opción para página inicial
    }

    if config_path.exists():
        try:
            config.read(config_path)
            if 'servidor' in config:
                configuracion['puerto'] = config.getint('servidor', 'puerto', fallback=8000)
                configuracion['delay_navegador'] = config.getint('servidor', 'delay_navegador', fallback=2)
                configuracion['abrir_navegador'] = config.getboolean('servidor', 'abrir_navegador', fallback=True)
                configuracion['pagina_inicial'] = config.get('servidor', 'pagina_inicial', fallback='/10_HTML/')
        except Exception as e:
            print(f"⚠️  No se pudo leer la configuración: {e}")
            print("Usando configuración por defecto...")

    return configuracion

def verificar_directorio():
    """Verifica que estamos en el directorio correcto y encuentra la raíz del proyecto"""
    # Obtener el directorio donde está ubicado este script
    script_dir = Path(__file__).parent
    # La raíz del proyecto está 2 niveles arriba (30_Misc-Scripts/10_ServerScripts -> raíz)
    raiz_proyecto = script_dir.parent.parent

    directorio_frontend = raiz_proyecto / "20_FrontEnd"

    if not directorio_frontend.exists():
        print("❌ Error: No se encuentra el directorio '20_FrontEnd'")
        print(f"Raíz del proyecto detectada: {raiz_proyecto}")
        print(f"Directorio frontend esperado: {directorio_frontend}")
        print("Asegúrate de que la estructura del proyecto esté completa")
        return False

    print(f"✅ Raíz del proyecto encontrada: {raiz_proyecto}")
    return raiz_proyecto

def verificar_python():
    """Verifica que Python esté disponible"""
    try:
        result = subprocess.run([sys.executable, "--version"],
                              capture_output=True, text=True, check=True)
        version = result.stdout.strip()
        print(f"✅ Python encontrado: {version}")
        return True
    except (subprocess.CalledProcessError, FileNotFoundError):
        print("❌ Error: Python no está disponible en el sistema")
        return False

def ejecutar_servidor(directorio_frontend, puerto=8000):
    """Ejecuta el servidor HTTP"""
    print(f"🚀 Iniciando servidor en: http://localhost:{puerto}")
    print(f"📁 Sirviendo archivos desde: {directorio_frontend}")
    print("📋 Presiona Ctrl+C para detener el servidor")
    print("-" * 50)

    try:
        # Cambiar al directorio del frontend
        os.chdir(directorio_frontend)

        # Ejecutar el servidor
        subprocess.run([sys.executable, "-m", "http.server", str(puerto)],
                      check=True)

    except KeyboardInterrupt:
        print("\n\n👋 Servidor detenido por el usuario")
    except subprocess.CalledProcessError as e:
        print(f"\n❌ Error al ejecutar el servidor: {e}")
        return False
    except Exception as e:
        print(f"\n❌ Error inesperado: {e}")
        return False

    return True

def abrir_navegador(puerto=8000, delay=2, pagina_inicial='/10_HTML/'):
    """Abre el navegador automáticamente después de un delay"""
    try:
        time.sleep(delay)
        url = f"http://localhost:{puerto}{pagina_inicial}"
        webbrowser.open(url)
        print(f"🌐 Abriendo navegador en: {url}")
    except Exception as e:
        print(f"⚠️  No se pudo abrir el navegador automáticamente: {e}")

def main():
    """Función principal"""
    print("🌟 TrackVer A&A - Servidor Web Local")
    print("=" * 40)

    # Cargar configuración
    config = cargar_configuracion()

    # Verificar Python
    if not verificar_python():
        return 1

    # Verificar directorio
    raiz_proyecto = verificar_directorio()
    if not raiz_proyecto:
        return 1

    directorio_frontend = raiz_proyecto / "20_FrontEnd"

    # Configuración del servidor
    puerto = config['puerto']
    delay_navegador = config['delay_navegador']
    abrir_navegador_auto = config['abrir_navegador']
    pagina_inicial = config['pagina_inicial']

    print(f"🔧 Configuración: Puerto {puerto}, Delay navegador {delay_navegador}s, Página inicial: {pagina_inicial}")

    # Abrir navegador en background si está habilitado
    if abrir_navegador_auto:
        import threading
        navegador_thread = threading.Thread(target=abrir_navegador, args=(puerto, delay_navegador, pagina_inicial))
        navegador_thread.daemon = True
        navegador_thread.start()
    else:
        print("ℹ️  Apertura automática del navegador deshabilitada")

    # Ejecutar servidor
    exito = ejecutar_servidor(directorio_frontend, puerto)

    return 0 if exito else 1

if __name__ == "__main__":
    sys.exit(main())