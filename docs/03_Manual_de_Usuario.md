# Manual de Usuario - TrackVerAA

## 1. Introducción

TrackVerAA es un sistema de gestión de auditorías que permite realizar seguimiento de actividades, generar reportes preliminares y registrar posiciones GPS. El sistema maneja tres tipos de usuarios:

- **Usuarios**: Pueden registrar su ubicación GPS
- **Auditores**: Pueden crear y gestionar auditorías
- **Administradores**: Tienen control total del sistema

## 2. Instalación

### 2.1 Requisitos Previos
- Java 17 o superior
- Maven 3.8 o superior

### 2.2 Pasos de Instalación
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Anex97/TrackVerAA.git
   cd TrackVerAA
   ```

2. Compilar el proyecto:
   ```bash
   mvn clean compile
   ```

3. Ejecutar la aplicación:
   ```bash
   mvn exec:java -Dexec.mainClass=com.trackver.app.Main
   ```

## 3. Guía de Uso

### 3.1 Inicio de Sesión
1. Ejecute la aplicación
2. Seleccione la opción "1. Iniciar sesión"
3. Ingrese su correo y contraseña

Credenciales de prueba:
- Admin: admin@trackver.com / 1234
- Auditor: auditor@trackver.com / 1234
- Usuario: usuario@trackver.com / 1234

### 3.2 Menú de Usuario
Como usuario regular, puede:
1. **Registrar posición GPS**
   - Seleccione opción 1
   - Ingrese latitud y longitud
2. **Listar mis posiciones**
   - Seleccione opción 2
   - Verá el historial de sus posiciones registradas

### 3.3 Menú de Auditor
Como auditor, puede:
1. **Crear auditoría**
   - Seleccione opción 1
   - Ingrese título y descripción
2. **Listar auditorías**
   - Seleccione opción 2 para todas las auditorías
   - Seleccione opción 3 para auditorías validadas

### 3.4 Menú de Administrador
Como administrador, puede:
1. **Gestionar usuarios**
   - Listar usuarios (opción 1)
   - Crear usuario (opción 2)
   - Eliminar usuario (opción 3)
2. **Gestionar auditorías**
   - Listar auditorías (opción 4)
   - Validar auditoría (opción 5)
3. **Ver posiciones GPS**
   - Listar todas las posiciones (opción 6)

## 4. Solución de Problemas

### 4.1 Problemas Comunes

1. **Error de conexión a base de datos**
   - Verifique que tiene permisos de escritura en el directorio
   - Las bases de datos se crean automáticamente en:
     - usuarios.db
     - auditorias.db
     - posiciones.db

2. **Error "Credenciales incorrectas"**
   - Verifique que está usando el correo exacto
   - La contraseña distingue mayúsculas y minúsculas

3. **No se pueden crear auditorías**
   - Verifique que tiene rol de Auditor
   - Asegúrese de que la base de datos tiene espacio

### 4.2 Mensajes de Error Comunes

| Mensaje | Causa Probable | Solución |
|---------|---------------|----------|
| "Error creando usuario" | Correo duplicado | Use otro correo |
| "Error en login" | Credenciales incorrectas | Verifique datos |
| "Error registrando posición" | Formato inválido | Use números decimales |

## 5. Mejores Prácticas

### 5.1 Seguridad
- Cambie su contraseña periódicamente
- No comparta sus credenciales
- Cierre sesión al terminar

### 5.2 Auditorías
- Use títulos descriptivos
- Incluya detalles relevantes en la descripción
- Valide auditorías oportunamente

### 5.3 Posiciones GPS
- Verifique coordenadas antes de registrar
- Use el formato decimal correcto
- Registre posiciones en momentos relevantes

## 6. Apéndice

### 6.1 Formato de Datos
- **Coordenadas GPS**: Decimales (ej: 25.6866, -100.3161)
- **Fechas**: YYYY-MM-DD
- **Estados de auditoría**: "Pendiente", "Validada"

### 6.2 Niveles de Acceso
- 0: Usuario regular
- 1: Auditor
- 2: Administrador

### 6.3 Limitaciones Conocidas
- Solo soporta un usuario activo a la vez
- No permite editar auditorías una vez creadas
- Las contraseñas se almacenan sin cifrar (demo)