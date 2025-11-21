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

Cómo contribuir
- Abrir un issue para discutir cambios o mejoras.
- Hacer un fork, crear una rama con la tarea (`feature/descripcion` o `fix/descripcion`), y enviar PR contra `main`.

Notas y recomendaciones
- Las contraseñas en los datos semilla son de ejemplo (`1234`) y NO deben usarse en producción.
- Para migrar a un entorno web o servidor centralizado, considerar consolidar la persistencia en un único RDBMS en servidor y exponer una API.

Contacto
- Repositorio: `https://github.com/Anex97/TrackVerAA`
----