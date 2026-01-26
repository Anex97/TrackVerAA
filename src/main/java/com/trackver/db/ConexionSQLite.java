package com.trackver.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionSQLite {
    // Conexión a la base de vehículos
    private static final String DB_PATH = "50_Databases/";

    public static Connection conectarVehiculos() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH + "vehiculos.db");
    }

    public static Connection conectarAlertas() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH + "alertas.db");
    }

    public static Connection conectarUsuarios() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH + "usuarios.db");
    }

    public static Connection conectarAuditorias() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH + "auditorias.db");
    }

    public static Connection conectarPosiciones() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH + "posiciones.db");
    }
}