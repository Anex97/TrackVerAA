# TrackVerAA

Descripción
- **Propósito:** Aplicación Java para la gestión y trazabilidad de auditorías, usuarios y posiciones GPS. Permite crear y gestionar auditorías, almacenar posiciones geográficas asociadas a usuarios y manejar roles (Administrador, Auditor, Usuario).
- **Tecnologías:** Java 17, Maven, SQLite (mediante `sqlite-jdbc`).

Características principales
- **Inicio de sesión y roles:** Soporta roles diferenciados y menús por rol (`Administrador`, `Auditor`, `Usuario`).
- **Persistencia local:** Usa bases de datos SQLite independientes: `usuarios.db`, `auditorias.db`, `posiciones.db`.
- **Datos semilla:** El proyecto inicializa la base de datos y carga datos de ejemplo con `InitDB` y `SeedDB`.

Requisitos
- **Java:** JDK 17 instalado.
- **Maven:** Apache Maven instalado en PATH.
- **Sistema operativo:** Probado como aplicación local en Windows; debería ejecutarse en cualquier plataforma con Java 17.

Ejecución Simple de la aplicación
- Ejecutar el archivo TrackverA&A.exe

Ejecucion desde Visual Basic usando Java
- Compilar y Empaquetar
- Ejecutar la applicacion con el comando que se encuentra debajo o darle Run al Main.java

Instalación y ejecución (Windows PowerShell)
- Clonar el repositorio:
```
git clone https://github.com/Anex97/TrackVerAA.git
cd TrackVerAA
```

- Compilar y empaquetar:
```
mvn clean package
```

- Ejecutar la aplicación con el plugin `exec` configurado en `pom.xml`:
```
mvn exec:java
```

Estructura principal del proyecto
- **`pom.xml`**: configuración Maven; usa Java 17 y dependencias `sqlite-jdbc` y `junit-jupiter`.
- **`src/main/java/com/trackver/app/Main.java`**: punto de entrada de la aplicación. Inicializa las tablas y los datos semilla, y muestra el menú principal.
- **`src/main/java/com/trackver/db/`**: capa de acceso a datos
	- `ConexionSQLite.java`: métodos de conexión a las bases SQLite (`usuarios.db`, `auditorias.db`, `posiciones.db`).
	- `InitDB.java`: contiene métodos para crear tablas (se ejecuta al iniciar la app).
	- `SeedDB.java`: inserta datos iniciales (usuarios, auditorías, posiciones).
	- `UsuarioDAO.java`, `AuditoriaDAO.java`, `PosicionDAO.java`, `AuditoriaDAO.java`: DAOs para operaciones sobre las tablas.
- **`src/main/java/com/trackver/model/`**: modelos de dominio (ej. `Usuario`, `Administrador`, `Auditor`, `Auditoria`, `ReportePreliminar`).
- **`src/main/java/com/trackver/ui/`**: menús por rol (`MenuAdmin`, `MenuAuditor`, `MenuManager`, `MenuUsuario`).
- **`SQLScript/`**: carpeta con scripts SQL (si se desea usar o revisar los scripts manuales).
- **`10_Documentacion/`**: documentación adicional y recursos del proyecto.

Base de datos y archivos generados
- Al ejecutarse, la aplicación crea/usa los siguientes ficheros SQLite en el directorio de trabajo:
	- `usuarios.db`
	- `auditorias.db`
	- `posiciones.db`
- La inicialización de tablas y la inserción de datos de ejemplo la hacen `InitDB` y `SeedDB` en el arranque de `Main`.

Resumen de clases clave
- `com.trackver.app.Main`: punto de entrada; menú principal e inicialización.
- `com.trackver.auth.SistemaAutenticacion`: (gestiona la autenticación, revisar para más detalles).
- `com.trackver.db.UsuarioDAO`: búsqueda y manejo de usuarios.
- `com.trackver.db.ConexionSQLite`: conexiones JDBC a archivos SQLite.
- `com.trackver.db.SeedDB`: inserta datos de ejemplo al inicio.
- `com.trackver.ui.MenuManager`: delega menús según el rol del usuario.

## 🌐 Frontend Web (Interfaz de Usuario)

El proyecto incluye una interfaz web completa desarrollada con HTML5, CSS3 y JavaScript vanilla para proporcionar una experiencia de usuario moderna y responsiva.

### 📁 Estructura del Frontend
- **`20_FrontEnd/`**: Directorio principal del frontend web
  - **`Index.html`**: Página de inicio de sesión
  - **`Panel.html`**: Dashboard principal con estadísticas
  - **`Registro.html`**: Formulario para registrar posiciones GPS
  - **`Consulta.html`**: Página para consultar registros existentes
  - **`Vehiculo.html`**: Gestión de vehículos
  - **`Alertas.html`**: Sistema de alertas y notificaciones
  - **`Recuperar.html`**: Recuperación de contraseña
  - **`styles.css`**: Estilos CSS para toda la aplicación
  - **`20_JavaScript/`**: Scripts JavaScript organizados por funcionalidad
    - `index.js`: Lógica de autenticación
    - `panel.js`: Dashboard y estadísticas
    - `registro.js`: Registro GPS con mapa interactivo
    - `consulta.js`: Consultas y filtros con visualización en mapa
    - `vehiculo.js`: Gestión de vehículos
    - `alertas.js`: Sistema de alertas

### 🚀 Ejecución del Servidor Web

#### Opción 1: Scripts Automáticos (Recomendado - Windows)
```bash
# Desde la carpeta de scripts
cd 30_Misc-Scripts/10_ServerScripts

# Doble clic en el archivo
ejecutar_servidor.bat

# O desde línea de comandos
python servidor.py
```

#### Opción 2: Ejecución Directa desde la Raíz
```bash
# Script Python inteligente
python 30_Misc-Scripts/10_ServerScripts/servidor.py

# Script batch simple (Windows)
30_Misc-Scripts\10_ServerScripts\ejecutar_servidor.bat

# Script PowerShell avanzado
.\30_Misc-Scripts\10_ServerScripts\ejecutar_servidor.ps1
```

#### Opción 3: PowerShell Avanzado
```powershell
# Ejecución básica
.\30_Misc-Scripts\10_ServerScripts\ejecutar_servidor.ps1

# Con puerto personalizado
.\30_Misc-Scripts\10_ServerScripts\ejecutar_servidor.ps1 -Puerto 8080

# Sin abrir navegador automáticamente
.\30_Misc-Scripts\10_ServerScripts\ejecutar_servidor.ps1 -NoBrowser
```

#### Opción 4: Manual con Python
```bash
cd 20_FrontEnd
python -m http.server 8000
```

### 🛑 Detención del Servidor Web

#### Opción 1: Scripts Automáticos (Recomendado)
```bash
# Desde la carpeta de scripts
cd 30_Misc-Scripts/10_ServerScripts

# Script Python inteligente
python detener_servidor.py

# Script batch simple (Windows)
detener_servidor.bat

# Script PowerShell avanzado
.\detener_servidor.ps1
```

#### Opción 2: Ejecución Directa desde la Raíz
```bash
# Script Python inteligente
python 30_Misc-Scripts/10_ServerScripts/detener_servidor.py

# Script batch simple (Windows)
30_Misc-Scripts\10_ServerScripts\detener_servidor.bat

# Script PowerShell avanzado
.\30_Misc-Scripts\10_ServerScripts\detener_servidor.ps1
```

#### Opción 3: Interrupción Manual
```bash
# En el terminal donde corre el servidor:
Ctrl + C
```

#### Opción 4: Comandos Directos
```bash
# Ver procesos usando el puerto
netstat -ano | findstr :8000

# Detener por PID
taskkill /PID <PID> /F

# Detener todos los procesos Python
taskkill /IM python.exe /F
```

### ⚙️ Configuración Personalizada

El archivo `config.ini` permite personalizar el comportamiento del servidor:

```ini
[servidor]
puerto = 8000
delay_navegador = 2
abrir_navegador = true
pagina_inicial = /10_HTML/
```

**Opciones de `pagina_inicial`:**
- `/10_HTML/` - Página principal del frontend (por defecto)
- `/10_HTML/Index.html` - Página de login
- `/10_HTML/Panel.html` - Dashboard
- `/10_HTML/Registro.html` - Registro GPS
- `/10_HTML/Consulta.html` - Consulta de registros
- `/10_HTML/Vehiculo.html` - Gestión de vehículos
- `/10_HTML/Alertas.html` - Sistema de alertas

### 📋 Scripts Disponibles

**Ubicación**: `30_Misc-Scripts/10_ServerScripts/`

- **`servidor.py`**: Script principal de Python con verificación automática
- **`ejecutar_servidor.bat`**: Script batch simple para Windows
- **`ejecutar_servidor.ps1`**: Script avanzado de PowerShell con parámetros
- **`detener_servidor.py`**: Script para detener el servidor automáticamente
- **`detener_servidor.bat`**: Script batch simple para detener el servidor
- **`detener_servidor.ps1`**: Script avanzado de PowerShell para detener el servidor
- **`config.ini`**: Archivo de configuración opcional (en la raíz del proyecto)

### 🌟 Características del Frontend

- **📱 Diseño Responsivo**: Adaptable a móviles y tablets
- **🗺️ Mapas Interactivos**: Integración con Leaflet.js para visualización GPS
- **⚡ JavaScript Moderno**: ES6+ con funcionalidades avanzadas
- **🎨 Interfaz Intuitiva**: Navegación clara y experiencia de usuario fluida
- **🔍 Sistema de Filtros**: Búsqueda avanzada en consultas
- **📊 Dashboard en Tiempo Real**: Estadísticas actualizadas automáticamente
- **🔔 Sistema de Alertas**: Notificaciones y estados del sistema

### 🔧 Tecnologías del Frontend

- **HTML5**: Estructura semántica y moderna
- **CSS3**: Estilos responsivos con Flexbox/Grid
- **JavaScript (Vanilla)**: Sin frameworks externos excepto Leaflet.js
- **Leaflet.js**: Librería de mapas open-source
- **LocalStorage**: Persistencia de sesión del usuario

### 🌐 Acceso a la Aplicación Web

Una vez ejecutado el servidor, accede a:
- **URL Principal** (automática): `http://localhost:8000/10_HTML/`
- **Página de Login**: `http://localhost:8000/10_HTML/Index.html`
- **Dashboard**: `http://localhost:8000/10_HTML/Panel.html`
- **Registro GPS**: `http://localhost:8000/10_HTML/Registro.html`
- **Consulta**: `http://localhost:8000/10_HTML/Consulta.html`
- **Vehículos**: `http://localhost:8000/10_HTML/Vehiculo.html`
- **Alertas**: `http://localhost:8000/10_HTML/Alertas.html`
- **Credenciales de Prueba**: `admin` / `123`

### 📋 Notas Importantes

- El frontend incluye simulaciones de datos para desarrollo
- Listo para integración con el backend Java vía APIs REST
- Compatible con todos los navegadores modernos
- Optimizado para rendimiento y experiencia de usuario
