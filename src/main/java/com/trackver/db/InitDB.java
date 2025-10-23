package com.trackver.db;

import java.sql.Connection;
import java.sql.Statement;

public class InitDB {

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
                     "usuario_id INTEGER)"; // referencia lógica al usuario dueño de la posición
        try (Connection conn = ConexionSQLite.conectarPosiciones();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabla 'posiciones' lista en posiciones.db");
        } catch (Exception e) {
            System.out.println("Error creando tabla posiciones: " + e.getMessage());
        }
    }
}