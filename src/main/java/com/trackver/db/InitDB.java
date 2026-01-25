package com.trackver.db;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class InitDB {
    // =========================
    // TABLA VEHICULOS (vehiculos.db)
    // =========================
    public static void crearTablaVehiculos() {
        String sql = "CREATE TABLE IF NOT EXISTS vehiculos (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "marca TEXT NOT NULL," +
                     "modelo TEXT NOT NULL," +
                     "placas TEXT UNIQUE NOT NULL," +
                     "anio INTEGER NOT NULL," +
                     "usuario_id INTEGER)"; // referencia al usuario dueño (opcional)
        try (Connection conn = ConexionSQLite.conectarVehiculos();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabla 'vehiculos' lista en vehiculos.db");
            // Asegurar columna usuario_id en esquemas antiguos
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info('vehiculos')")) {
                boolean tieneUsuarioId = false;
                while (rs.next()) {
                    String colName = rs.getString("name");
                    if ("usuario_id".equalsIgnoreCase(colName)) {
                        tieneUsuarioId = true;
                        break;
                    }
                }
                if (!tieneUsuarioId) {
                    try (Statement s2 = conn.createStatement()) {
                        s2.execute("ALTER TABLE vehiculos ADD COLUMN usuario_id INTEGER");
                        System.out.println("Columna 'usuario_id' añadida a tabla vehiculos");
                    } catch (Exception ex) {
                        System.out.println("No se pudo añadir columna usuario_id: " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error creando tabla vehiculos: " + e.getMessage());
        }
    }

    // =========================
    // TABLA ALERTAS (alertas.db)
    // =========================
    public static void crearTablaAlertas() {
        String sql = "CREATE TABLE IF NOT EXISTS alertas (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "vehiculo_id INTEGER NOT NULL," +
                     "tipo TEXT NOT NULL," +
                     "descripcion TEXT," +
                     "fecha TEXT NOT NULL," +
                     "estado TEXT NOT NULL)";
        try (Connection conn = ConexionSQLite.conectarAlertas();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabla 'alertas' lista en alertas.db");
        } catch (Exception e) {
            System.out.println("Error creando tabla alertas: " + e.getMessage());
        }
    }

    // =========================
    // TABLA USUARIOS (usuarios.db)
    // =========================
    public static void crearTablaUsuarios() {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "nombre TEXT NOT NULL," +
                     "correo TEXT UNIQUE NOT NULL," +
                     "contrasena TEXT NOT NULL," +
                     "nivelAcceso INTEGER NOT NULL)"; // 0=Usuario, 1=Auditor, 2=Admin
        try (Connection conn = ConexionSQLite.conectarUsuarios();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabla 'usuarios' lista en usuarios.db");
        } catch (Exception e) {
            System.out.println("Error creando tabla usuarios: " + e.getMessage());
        }
    }

    // =========================
    // TABLA AUDITORIAS (auditorias.db)
    // =========================
    public static void crearTablaAuditorias() {
        String sql = "CREATE TABLE IF NOT EXISTS auditorias (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "titulo TEXT NOT NULL," +
                     "descripcion TEXT," +
                     "fecha TEXT NOT NULL," +
                     "estado TEXT NOT NULL," +
                     "usuario_id INTEGER)"; // referencia lógica al usuario que la creó
        try (Connection conn = ConexionSQLite.conectarAuditorias();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabla 'auditorias' lista en auditorias.db");
        } catch (Exception e) {
            System.out.println("Error creando tabla auditorias: " + e.getMessage());
        }
    }

    // =========================
    // TABLA POSICIONES (posiciones.db)
    // =========================
    public static void crearTablaPosiciones() {
        String sql = "CREATE TABLE IF NOT EXISTS posiciones (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "latitud REAL NOT NULL," +
                     "longitud REAL NOT NULL," +
                     "fechaHora TEXT NOT NULL," +
                     "usuario_id INTEGER," +
                     "vehiculo_id INTEGER)"; // ahora con referencia al vehículo dueño de la posición
        try (Connection conn = ConexionSQLite.conectarPosiciones();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabla 'posiciones' lista en posiciones.db");
            // Asegurar columna vehiculo_id en esquemas antiguos y migrar cuando sea posible
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info('posiciones')")) {
                boolean tieneVeh = false;
                while (rs.next()) {
                    String colName = rs.getString("name");
                    if ("vehiculo_id".equalsIgnoreCase(colName)) {
                        tieneVeh = true;
                        break;
                    }
                }
                if (!tieneVeh) {
                    try (Statement s2 = conn.createStatement()) {
                        s2.execute("ALTER TABLE posiciones ADD COLUMN vehiculo_id INTEGER");
                        System.out.println("Columna 'vehiculo_id' añadida a tabla posiciones");
                    } catch (Exception ex) {
                        System.out.println("No se pudo añadir columna vehiculo_id: " + ex.getMessage());
                    }
                }
            }

            // Migración simple: para posiciones existentes con usuario_id, intentar enlazar al primer vehículo del usuario
            try (PreparedStatement psSelect = conn.prepareStatement("SELECT id, usuario_id FROM posiciones WHERE vehiculo_id IS NULL");
                 Connection connVeh = ConexionSQLite.conectarVehiculos();
                 PreparedStatement psFindVeh = connVeh.prepareStatement("SELECT id FROM vehiculos WHERE usuario_id = ? LIMIT 1");
                 PreparedStatement psUpdate = conn.prepareStatement("UPDATE posiciones SET vehiculo_id = ? WHERE id = ?")) {
                try (ResultSet rpos = psSelect.executeQuery()) {
                    while (rpos.next()) {
                        int posId = rpos.getInt("id");
                        int usuarioId = rpos.getInt("usuario_id");
                        psFindVeh.setInt(1, usuarioId);
                        try (ResultSet rv = psFindVeh.executeQuery()) {
                            if (rv.next()) {
                                int vehId = rv.getInt("id");
                                psUpdate.setInt(1, vehId);
                                psUpdate.setInt(2, posId);
                                psUpdate.executeUpdate();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error en migración posiciones->vehiculo: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Error creando tabla posiciones: " + e.getMessage());
        }
    }
}