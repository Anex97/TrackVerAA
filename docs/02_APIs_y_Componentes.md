# Documentación de APIs y Componentes

## 1. Paquete `com.trackver.db`

### 1.1 ConexionSQLite
Gestiona las conexiones a las bases de datos SQLite del sistema.

```java
public class ConexionSQLite {
    /**
     * Establece conexión con la base de datos de usuarios
     * @return Connection objeto de conexión a usuarios.db
     * @throws SQLException si hay error de conexión
     */
    public static Connection conectarUsuarios()

    /**
     * Establece conexión con la base de datos de auditorías
     * @return Connection objeto de conexión a auditorias.db
     * @throws SQLException si hay error de conexión
     */
    public static Connection conectarAuditorias()

    /**
     * Establece conexión con la base de datos de posiciones
     * @return Connection objeto de conexión a posiciones.db
     * @throws SQLException si hay error de conexión
     */
    public static Connection conectarPosiciones()
}
```

### 1.2 UsuarioDAO
Operaciones CRUD para la entidad Usuario.

```java
public class UsuarioDAO {
    /**
     * DTO para transferencia de datos de usuario
     */
    public static class UsuarioDTO {
        public final int id;
        public final String nombre;
        public final String correo;
        public final int nivelAcceso;
    }

    /**
     * Crea un nuevo usuario en el sistema
     * @param nombre Nombre del usuario
     * @param correo Email único del usuario
     * @param contrasena Contraseña del usuario
     * @param nivelAcceso Nivel de acceso (0=Usuario, 1=Auditor, 2=Admin)
     * @return boolean éxito de la operación
     */
    public static boolean crearUsuario(String nombre, String correo, 
                                     String contrasena, int nivelAcceso)

    /**
     * Autentica un usuario por correo y contraseña
     * @param correo Email del usuario
     * @param contrasena Contraseña del usuario
     * @return UsuarioDTO datos del usuario o null si no existe/credenciales incorrectas
     */
    public static UsuarioDTO buscarPorCorreoYPass(String correo, String contrasena)

    // ... otros métodos
}
```

## 2. Paquete `com.trackver.model`

### 2.1 Auditoria
Representa una auditoría en el sistema.

```java
public class Auditoria {
    /**
     * Constructor de Auditoría
     * @param id Identificador único
     * @param fecha Fecha de creación
     * @param datos Datos capturados
     */
    public Auditoria(int id, LocalDate fecha, String datos)

    /**
     * Genera un reporte preliminar de la auditoría
     * @return ReportePreliminar nuevo o existente
     */
    public ReportePreliminar generarReporte()

    /**
     * Actualiza el estado de la auditoría
     * @param nuevoEstado Estado a asignar
     */
    public void actualizarEstado(String nuevoEstado)
}
```

## 3. Paquete `com.trackver.ui`

### 3.1 MenuManager
Gestiona la navegación entre menús según el rol del usuario.

```java
public class MenuManager {
    /**
     * Muestra el menú correspondiente al rol del usuario
     * @param sc Scanner para entrada de usuario
     * @param usuario UsuarioDTO con datos del usuario activo
     */
    public static void mostrarMenuPorRol(Scanner sc, UsuarioDTO usuario)
}
```

## 4. Paquete `com.trackver.auth`

### 4.1 SistemaAutenticacion
Maneja la autenticación y autorización de usuarios.

```java
public class SistemaAutenticacion {
    /**
     * Valida credenciales de usuario
     * @param id ID del usuario
     * @param correo Email del usuario
     * @param contrasena Contraseña a validar
     * @return boolean true si las credenciales son correctas
     */
    public boolean validarCredenciales(int id, String correo, String contrasena)
}
```

## 5. Interacciones entre Componentes

### 5.1 Flujo de Autenticación y Menús
1. `Main` recibe credenciales
2. `UsuarioDAO` valida credenciales contra BD
3. `MenuManager` determina menú según rol
4. Menú específico (`MenuAdmin`, `MenuAuditor`, `MenuUsuario`) toma control

### 5.2 Flujo de Auditorías
1. `MenuAuditor` recibe datos de nueva auditoría
2. `AuditoriaDAO` persiste datos
3. `Auditoria` puede generar `ReportePreliminar`
4. `MenuAdmin` puede validar auditorías

## 6. Ejemplos de Uso

### 6.1 Crear y Validar Auditoría
```java
// Como Auditor
String titulo = "Nueva Auditoría";
String descripcion = "Descripción detallada";
int auditorId = usuarioActivo.id;
AuditoriaDAO.crearAuditoria(titulo, descripcion, auditorId);

// Como Admin
int auditoriaId = 1;
AuditoriaDAO.validarAuditoria(auditoriaId);
```

### 6.2 Registrar Posición GPS
```java
// Como Usuario
double latitud = 25.6866;
double longitud = -100.3161;
int usuarioId = usuarioActivo.id;
PosicionDAO.registrarPosicion(latitud, longitud, usuarioId);
```