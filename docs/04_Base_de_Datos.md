# Documentación de la Base de Datos

## 1. Esquema General

El sistema utiliza tres bases de datos SQLite independientes para mantener la separación de concerns y facilitar el mantenimiento:

### 1.1 usuarios.db
Almacena información de usuarios y credenciales

### 1.2 auditorias.db
Gestiona auditorías y sus estados

### 1.3 posiciones.db
Registra el tracking de posiciones GPS

## 2. Estructura Detallada

### 2.1 usuarios.db

#### Tabla: usuarios
| Columna | Tipo | Restricciones | Descripción |
|---------|------|---------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Identificador único |
| nombre | TEXT | NOT NULL | Nombre completo del usuario |
| correo | TEXT | UNIQUE NOT NULL | Email para login |
| contrasena | TEXT | NOT NULL | Contraseña (sin cifrar) |
| nivelAcceso | INTEGER | NOT NULL | Nivel de privilegios (0=Usuario, 1=Auditor, 2=Admin) |

```sql
CREATE TABLE usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    correo TEXT UNIQUE NOT NULL,
    contrasena TEXT NOT NULL,
    nivelAcceso INTEGER NOT NULL
);
```

### 2.2 auditorias.db

#### Tabla: auditorias
| Columna | Tipo | Restricciones | Descripción |
|---------|------|---------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Identificador único |
| titulo | TEXT | NOT NULL | Título de la auditoría |
| descripcion | TEXT | | Detalles de la auditoría |
| fecha | TEXT | NOT NULL | Fecha de creación (YYYY-MM-DD) |
| estado | TEXT | NOT NULL | Estado actual ("Pendiente"/"Validada") |
| usuario_id | INTEGER | | ID del auditor que la creó |

```sql
CREATE TABLE auditorias (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo TEXT NOT NULL,
    descripcion TEXT,
    fecha TEXT NOT NULL,
    estado TEXT NOT NULL,
    usuario_id INTEGER
);
```

### 2.3 posiciones.db

#### Tabla: posiciones
| Columna | Tipo | Restricciones | Descripción |
|---------|------|---------------|-------------|
| id | INTEGER | PRIMARY KEY AUTOINCREMENT | Identificador único |
| latitud | REAL | NOT NULL | Coordenada latitud |
| longitud | REAL | NOT NULL | Coordenada longitud |
| fechaHora | TEXT | NOT NULL | Timestamp del registro |
| usuario_id | INTEGER | | ID del usuario asociado |

```sql
CREATE TABLE posiciones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    latitud REAL NOT NULL,
    longitud REAL NOT NULL,
    fechaHora TEXT NOT NULL,
    usuario_id INTEGER
);
```

## 3. Relaciones y Referencias

### 3.1 Referencias Lógicas
- `auditorias.usuario_id` → `usuarios.id`
- `posiciones.usuario_id` → `usuarios.id`

Nota: Las referencias son lógicas (no hay FOREIGN KEYs físicas) debido al uso de bases de datos separadas.

## 4. Operaciones Comunes

### 4.1 Consultas Frecuentes

#### Autenticación de Usuario
```sql
SELECT id, nombre, correo, nivelAcceso 
FROM usuarios 
WHERE correo = ? AND contrasena = ?
```

#### Listar Auditorías Validadas
```sql
SELECT * 
FROM auditorias 
WHERE estado = 'Validada'
```

#### Historial de Posiciones
```sql
SELECT * 
FROM posiciones 
WHERE usuario_id = ? 
ORDER BY fechaHora DESC
```

### 4.2 Inserciones

#### Nuevo Usuario
```sql
INSERT INTO usuarios (nombre, correo, contrasena, nivelAcceso) 
VALUES (?, ?, ?, ?)
```

#### Nueva Auditoría
```sql
INSERT INTO auditorias (titulo, descripcion, fecha, estado, usuario_id) 
VALUES (?, ?, date('now'), 'Pendiente', ?)
```

#### Nueva Posición
```sql
INSERT INTO posiciones (latitud, longitud, fechaHora, usuario_id) 
VALUES (?, ?, datetime('now'), ?)
```

## 5. Consideraciones de Diseño

### 5.1 Ventajas del Diseño Actual
- Separación de concerns por base de datos
- Esquema simple y directo
- Fácil de mantener y respaldar

### 5.2 Limitaciones
- No hay integridad referencial entre bases
- Datos no normalizados en algunas tablas
- Sin soporte para soft delete

### 5.3 Posibles Mejoras
- Implementar foreign keys usando una sola base
- Agregar índices para búsquedas frecuentes
- Implementar versionado de auditorías
- Agregar timestamps de creación/modificación