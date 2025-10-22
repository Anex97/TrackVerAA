package com.trackver.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InitDB {
    public static void crearTablaUsuarios() {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "nombre TEXT NOT NULL," +
                     "correo TEXT UNIQUE NOT NULL," +
                     "contrasena TEXT NOT NULL," +
                     "nivelAcceso INTEGER NOT NULL)";

        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Tabla 'usuarios' lista.");
        } catch (Exception e) {
            System.out.println("Error creando tabla: " + e.getMessage());
        }
    }
    public static void crearTablaAuditorias() {
    String sql = "CREATE TABLE IF NOT EXISTS auditorias (" +
                 "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                 "titulo TEXT NOT NULL," +
                 "descripcion TEXT," +
                 "fecha TEXT NOT NULL," +
                 "estado TEXT NOT NULL," +
                 "usuario_id INTEGER," +
                 "FOREIGN KEY(usuario_id) REFERENCES usuarios(id)" +
                 ");";
        try (Connection conn = ConexionSQLite.conectar();
            Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
        System.out.println("Error creando tabla auditorias: " + e.getMessage());
        }
    }
    public static void crearTablaPosiciones(){
        String sql = "CREATE TABLE IF NOT EXISTS posiciones (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "latitud REAL NOT NULL," +
                     "longitud REAL NOT NULL," +
                     "fechaHora TEXT NOT NULL," +
                     "usuario_id INTEGER," +
                     "FOREIGN KEY(usuario_id) REFERENCES usuarios(id)" +
                     ");";
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error creando tabla posiciones: " + e.getMessage());
        }
    }
}