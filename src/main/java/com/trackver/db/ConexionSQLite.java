package com.trackver.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionSQLite {

    // Conexión a la base de usuarios
    public static Connection conectarUsuarios() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:usuarios.db");
    }

    // Conexión a la base de auditorías
    public static Connection conectarAuditorias() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:auditorias.db");
    }

    // Conexión a la base de posiciones GPS
    public static Connection conectarPosiciones() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:posiciones.db");
    }
}