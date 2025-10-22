package com.trackver.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionSQLite {
    // Ruta de la base de datos. Si no existe, SQLite la crea automáticamente.
    private static final String URL = "jdbc:sqlite:usuarios.db";

    /**
     * Método para obtener una conexión a la base de datos SQLite.
     * @return Connection activa o null si ocurre un error.
     */
    public static Connection conectar() {
        try {
            Connection conn = DriverManager.getConnection(URL);
            // Si quieres, puedes imprimir un mensaje de depuración:
            // System.out.println("✅ Conexión establecida con SQLite.");
            return conn;
        } catch (SQLException e) {
            System.out.println("❌ Error al conectar con SQLite: " + e.getMessage());
            return null;
        }
    }
}