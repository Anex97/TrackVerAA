package com.trackver.db;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SeedDB {
    // =========================
    // VEHICULOS
    // =========================
    public static void insertarVehiculos() {
        String sql = "INSERT INTO vehiculos (marca, modelo, placas, anio) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectarVehiculos();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "Volvo");
            pstmt.setString(2, "TR 430");
            pstmt.setString(3, "ABC-123");
            pstmt.setInt(4, 2022);
            pstmt.executeUpdate();

            pstmt.setString(1, "Volvo");
            pstmt.setString(2, "TR 500");
            pstmt.setString(3, "DEF-456");
            pstmt.setInt(4, 2023);
            pstmt.executeUpdate();

            System.out.println("Vehículos iniciales insertados en vehiculos.db");
        } catch (Exception e) {
            System.out.println("Error insertando vehículos iniciales: " + e.getMessage());
        }
    }

    // =========================
    // ALERTAS
    // =========================
    public static void insertarAlertas() {
        String sql = "INSERT INTO alertas (vehiculo_id, tipo, descripcion, fecha, estado) VALUES (?, ?, ?, date('now'), ?)";
        try (Connection conn = ConexionSQLite.conectarAlertas();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 1); // Volvo TR 430
            pstmt.setString(2, "Velocidad");
            pstmt.setString(3, "Exceso de velocidad detectado");
            pstmt.setString(4, "Pendiente");
            pstmt.executeUpdate();

            pstmt.setInt(1, 2); // Volvo TR 500
            pstmt.setString(2, "Zona restringida");
            pstmt.setString(3, "Entrada a zona no autorizada");
            pstmt.setString(4, "Atendida");
            pstmt.executeUpdate();

            System.out.println("Alertas iniciales insertadas en alertas.db");
        } catch (Exception e) {
            System.out.println("Error insertando alertas iniciales: " + e.getMessage());
        }
    }

    // =========================
    // USUARIOS
    // =========================
    public static void insertarUsuarios() {
        String sql = "INSERT INTO usuarios (nombre, correo, contrasena, nivelAcceso) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectarUsuarios();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Admin
            pstmt.setString(1, "Admin");
            pstmt.setString(2, "admin@trackver.com");
            pstmt.setString(3, "1234");
            pstmt.setInt(4, 2);
            pstmt.executeUpdate();

            // Auditor
            pstmt.setString(1, "Auditor");
            pstmt.setString(2, "auditor@trackver.com");
            pstmt.setString(3, "1234");
            pstmt.setInt(4, 1);
            pstmt.executeUpdate();

            // Usuario
            pstmt.setString(1, "Usuario");
            pstmt.setString(2, "usuario@trackver.com");
            pstmt.setString(3, "1234");
            pstmt.setInt(4, 0);
            pstmt.executeUpdate();

            System.out.println("Usuarios iniciales insertados en usuarios.db");

        } catch (Exception e) {
            System.out.println("Error insertando usuarios iniciales: " + e.getMessage());
        }
    }

    // =========================
    // AUDITORÍAS
    // =========================
    public static void insertarAuditorias() {
        String sql = "INSERT INTO auditorias (titulo, descripcion, fecha, estado, usuario_id) VALUES (?, ?, date('now'), ?, ?)";
        try (Connection conn = ConexionSQLite.conectarAuditorias();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "Auditoría inicial");
            pstmt.setString(2, "Revisión de sistema base");
            pstmt.setString(3, "Pendiente");
            pstmt.setInt(4, 2); // creada por el Auditor
            pstmt.executeUpdate();

            pstmt.setString(1, "Auditoría de seguridad");
            pstmt.setString(2, "Validación de accesos y roles");
            pstmt.setString(3, "Pendiente");
            pstmt.setInt(4, 2);
            pstmt.executeUpdate();

            System.out.println("Auditorías iniciales insertadas en auditorias.db");

        } catch (Exception e) {
            System.out.println("Error insertando auditorías iniciales: " + e.getMessage());
        }
    }

    // =========================
    // POSICIONES GPS
    // =========================
    public static void insertarPosiciones() {
        String sql = "INSERT INTO posiciones (latitud, longitud, fechaHora, usuario_id) VALUES (?, ?, datetime('now'), ?)";
        try (Connection conn = ConexionSQLite.conectarPosiciones();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, 25.6866);
            pstmt.setDouble(2, -100.3161);
            pstmt.setInt(3, 3); // Usuario normal
            pstmt.executeUpdate();

            pstmt.setDouble(1, 19.4326);
            pstmt.setDouble(2, -99.1332);
            pstmt.setInt(3, 3);
            pstmt.executeUpdate();

            System.out.println("Posiciones iniciales insertadas en posiciones.db");

        } catch (Exception e) {
            System.out.println("Error insertando posiciones iniciales: " + e.getMessage());
        }
    }
}