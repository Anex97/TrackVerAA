# Análisis del Repositorio TrackVerAA

## 1. Descripción General del Proyecto
- **Nombre**: TrackVer-AA (`TrackVerAA`)
- **Tipo**: Aplicación de consola Java (Maven)
- **Propósito**: Proyecto académico para gestionar auditorías y generar reportes preliminares
- **Punto de entrada**: `com.trackver.app.Main`

### Flow Principal
1. Inicializa la base de datos (tablas `usuarios`, `auditorias`, `posiciones`) usando `InitDB`
2. Inserta datos semilla con `SeedDB`
3. Muestra un menú de consola que permite iniciar sesión
4. Según el rol del usuario, delega en `MenuManager` para mostrar menús específicos

## 2. Tecnologías y Dependencias

### 2.1 Dependencias Runtime/Test (pom.xml)
```xml
<!-- SQLite JDBC Driver -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.45.1.0</version>
</dependency>

<!-- JUnit 5 para pruebas -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>
```

### 2.2 Plugins de Build (Maven)
- org.apache.maven.plugins:maven-compiler-plugin:3.11.0
- org.codehaus.mojo:exec-maven-plugin:3.1.0
- org.apache.maven.plugins:maven-surefire-plugin:3.1.2

## 3. APIs y Librerías Utilizadas

### 3.1 JDBC / SQLite
Ejemplo de conexión (`ConexionSQLite.java`):
```java
public static Connection conectarUsuarios() throws SQLException {
    return DriverManager.getConnection("jdbc:sqlite:usuarios.db");
}
```

Ejemplo de operaciones CRUD (`UsuarioDAO.java`):
```java
try (Connection conn = ConexionSQLite.conectarUsuarios();
     PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setString(1, correo);
    pstmt.setString(2, contrasena);
    ResultSet rs = pstmt.executeQuery();
    if (rs.next()) {
        return new UsuarioDTO(
            rs.getInt("id"),
            rs.getString("nombre"),
            rs.getString("correo"),
            rs.getInt("nivelAcceso")
        );
    }
}
```

### 3.2 java.time (LocalDate)
Uso en modelos (`Auditoria.java`):
```java
import java.time.LocalDate;

public class Auditoria {
    private LocalDate fecha;
    
    public Auditoria(int id, LocalDate fecha, String datos) {
        this.fecha = fecha;
        // ...
    }

    public ReportePreliminar generarReporte() {
        return new ReportePreliminar(
            this.idAuditoria,
            LocalDate.now(),
            descripcion,
            autor
        );
    }
}
```

### 3.3 java.util (Colecciones y Scanner)
Ejemplo de colecciones en DAOs:
```java
List<AuditoriaDTO> lista = new ArrayList<>();
while (rs.next()) {
    lista.add(new AuditoriaDTO(...));
}
```

Ejemplo de Scanner para I/O (`Main.java`):
```java
Scanner sc = new Scanner(System.in);
System.out.print("Ingrese correo: ");
String correo = sc.nextLine();
MenuManager.mostrarMenuPorRol(sc, usuarioActivo);
```

### 3.4 JUnit 5
Ejemplo de test (`TestAuth.Java`):
```java
@Test
public void inicioSesionBloqueo() {
    SistemaAutenticacion s = new SistemaAutenticacion();
    Auditor a = new Auditor(10, "A", "a@x", "pw");
    s.registrarUsuario(a);
    assertFalse(s.validarCredenciales(10, "a@x", "wrong"));
}
```

## 4. Estructura del Proyecto

### 4.1 Paquetes Principales
- `com.trackver.db`: DAOs y utilidades de base de datos
- `com.trackver.model`: Entidades del dominio
- `com.trackver.ui`: Interfaces de usuario (menús CLI)
- `com.trackver.auth`: Sistema de autenticación
- `com.trackver.app`: Punto de entrada

### 4.2 Bases de Datos
El proyecto utiliza tres bases SQLite:
- `usuarios.db`: Gestión de usuarios y autenticación
- `auditorias.db`: Almacenamiento de auditorías
- `posiciones.db`: Registro de posiciones GPS

## 5. Cómo Ejecutar el Proyecto

### 5.1 Requisitos
- Java 17 (actual)
- Maven

### 5.2 Comandos
```powershell
# Compilar
mvn compile

# Ejecutar
mvn exec:java -Dexec.mainClass=com.trackver.app.Main

# Ejecutar tests
mvn test

# Ver árbol de dependencias
mvn dependency:tree
```

## 6. Notas Técnicas
- El proyecto está configurado para Java 17 (propiedades `maven.compiler.source` y `maven.compiler.target` en `pom.xml`).
- Usa try-with-resources para manejo seguro de recursos de base de datos.
- Implementa un sistema de roles (Usuario, Auditor, Admin) con menús específicos.
- Los DAOs implementan DTOs internos para transferencia de datos.