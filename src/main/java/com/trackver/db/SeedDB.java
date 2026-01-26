package com.trackver.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;

public class SeedDB {
    // =========================
    // VEHICULOS
    // =========================
    public static void insertarVehiculos() {
        String sql = "INSERT OR IGNORE INTO vehiculos (marca, modelo, placas, anio, usuario_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectarVehiculos();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "Volvo");
            pstmt.setString(2, "TR 430");
            pstmt.setString(3, "ABC-123");
            pstmt.setInt(4, 2022);
            pstmt.setInt(5, 1); // asignar al usuario 1 por defecto
            pstmt.executeUpdate();

            pstmt.setString(1, "Volvo");
            pstmt.setString(2, "TR 500");
            pstmt.setString(3, "DEF-456");
            pstmt.setInt(4, 2023);
            pstmt.setInt(5, 1);
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
        String sql = "INSERT OR IGNORE INTO alertas (vehiculo_id, tipo, descripcion, fecha, estado) VALUES (?, ?, ?, date('now'), ?)";
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
        String sql = "INSERT OR IGNORE INTO usuarios (nombre, correo, contrasena, nivelAcceso) VALUES (?, ?, ?, ?)";
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
        try (Connection connP = ConexionSQLite.conectarPosiciones();
             Connection connV = ConexionSQLite.conectarVehiculos()) {

            // Helper to insert with optional vehiculo_id
            PreparedStatement pInsertWithVeh = connP.prepareStatement("INSERT OR IGNORE INTO posiciones (latitud, longitud, fechaHora, usuario_id, vehiculo_id) VALUES (?, ?, datetime('now'), ?, ?)");
            PreparedStatement pInsertNoVeh = connP.prepareStatement("INSERT OR IGNORE INTO posiciones (latitud, longitud, fechaHora, usuario_id) VALUES (?, ?, datetime('now'), ?)");

            // Usuario 3
            int usuario3 = 3;
            Integer veh = null;
            try (PreparedStatement pv = connV.prepareStatement("SELECT id FROM vehiculos WHERE usuario_id = ? LIMIT 1")) {
                pv.setInt(1, usuario3);
                try (ResultSet rv = pv.executeQuery()) { if (rv.next()) veh = rv.getInt("id"); }
            }
            if (veh != null) {
                pInsertWithVeh.setDouble(1, 25.6866);
                pInsertWithVeh.setDouble(2, -100.3161);
                pInsertWithVeh.setInt(3, usuario3);
                pInsertWithVeh.setInt(4, veh);
                pInsertWithVeh.executeUpdate();

                pInsertWithVeh.setDouble(1, 19.4326);
                pInsertWithVeh.setDouble(2, -99.1332);
                pInsertWithVeh.setInt(3, usuario3);
                pInsertWithVeh.setInt(4, veh);
                pInsertWithVeh.executeUpdate();
            } else {
                pInsertNoVeh.setDouble(1, 25.6866);
                pInsertNoVeh.setDouble(2, -100.3161);
                pInsertNoVeh.setInt(3, usuario3);
                pInsertNoVeh.executeUpdate();

                pInsertNoVeh.setDouble(1, 19.4326);
                pInsertNoVeh.setDouble(2, -99.1332);
                pInsertNoVeh.setInt(3, usuario3);
                pInsertNoVeh.executeUpdate();
            }

            // Posiciones de prueba para usuario 1 (demo)
            int usuario1 = 1;
            veh = null;
            try (PreparedStatement pv = connV.prepareStatement("SELECT id FROM vehiculos WHERE usuario_id = ? LIMIT 1")) {
                pv.setInt(1, usuario1);
                try (ResultSet rv = pv.executeQuery()) { if (rv.next()) veh = rv.getInt("id"); }
            }
            if (veh != null) {
                pInsertWithVeh.setDouble(1, 20.6597);
                pInsertWithVeh.setDouble(2, -103.3496);
                pInsertWithVeh.setInt(3, usuario1);
                pInsertWithVeh.setInt(4, veh);
                pInsertWithVeh.executeUpdate();

                pInsertWithVeh.setDouble(1, 19.4326);
                pInsertWithVeh.setDouble(2, -99.1332);
                pInsertWithVeh.setInt(3, usuario1);
                pInsertWithVeh.setInt(4, veh);
                pInsertWithVeh.executeUpdate();
            } else {
                pInsertNoVeh.setDouble(1, 20.6597);
                pInsertNoVeh.setDouble(2, -103.3496);
                pInsertNoVeh.setInt(3, usuario1);
                pInsertNoVeh.executeUpdate();

                pInsertNoVeh.setDouble(1, 19.4326);
                pInsertNoVeh.setDouble(2, -99.1332);
                pInsertNoVeh.setInt(3, usuario1);
                pInsertNoVeh.executeUpdate();
            }

            System.out.println("Posiciones iniciales insertadas en posiciones.db");

        } catch (Exception e) {
            System.out.println("Error insertando posiciones iniciales: " + e.getMessage());
        }
    }

    // Insertar datos aleatorios adicionales por usuario (vehículos y posiciones)
    public static void insertarDatosAleatorios() {
        Random rnd = new Random();
        try (Connection connV = ConexionSQLite.conectarVehiculos();
             Connection connP = ConexionSQLite.conectarPosiciones()) {

            // Consultar usuarios existentes
            try (Connection connU = ConexionSQLite.conectarUsuarios();
                 PreparedStatement pu = connU.prepareStatement("SELECT id FROM usuarios");
                 ResultSet rs = pu.executeQuery()) {
                while (rs.next()) {
                    int uid = rs.getInt("id");

                    // Contar vehículos del usuario y añadir hasta 3
                    int needVeh = 3;
                    try (PreparedStatement pc = connV.prepareStatement("SELECT COUNT(*) as c FROM vehiculos WHERE usuario_id = ?")) {
                        pc.setInt(1, uid);
                        ResultSet rc = pc.executeQuery();
                        if (rc.next()) {
                            int have = rc.getInt("c");
                            needVeh = Math.max(0, 3 - have);
                        }
                        rc.close();
                    }

                    if (needVeh > 0) {
                        String sqlV = "INSERT OR IGNORE INTO vehiculos (marca, modelo, placas, anio, usuario_id) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement pv = connV.prepareStatement(sqlV)) {
                            for (int i = 0; i < needVeh; i++) {
                                String marca = (rnd.nextBoolean() ? "Ford" : "Chevrolet");
                                String modelo = "Mdl" + (rnd.nextInt(900) + 100);
                                String placas = "U" + uid + "-" + (System.currentTimeMillis() % 100000) + "-" + i;
                                int anio = 2015 + rnd.nextInt(8);
                                pv.setString(1, marca);
                                pv.setString(2, modelo);
                                pv.setString(3, placas);
                                pv.setInt(4, anio);
                                pv.setInt(5, uid);
                                try { pv.executeUpdate(); } catch (Exception ex) { /* skip duplicates */ }
                            }
                        }
                    }

                    // Añadir posiciones de ejemplo (5) para el usuario
                    Integer firstVeh = null;
                    try (PreparedStatement pv = connV.prepareStatement("SELECT id FROM vehiculos WHERE usuario_id = ? LIMIT 1")) {
                        pv.setInt(1, uid);
                        try (ResultSet rv = pv.executeQuery()) { if (rv.next()) firstVeh = rv.getInt("id"); }
                    }
                    String sqlPwith = "INSERT OR IGNORE INTO posiciones (latitud, longitud, fechaHora, usuario_id, vehiculo_id) VALUES (?, ?, datetime('now','-' || ? || ' minutes'), ?, ?)";
                    String sqlPno = "INSERT OR IGNORE INTO posiciones (latitud, longitud, fechaHora, usuario_id) VALUES (?, ?, datetime('now','-' || ? || ' minutes'), ?)";
                    if (firstVeh != null) {
                        try (PreparedStatement pp = connP.prepareStatement(sqlPwith)) {
                            for (int j = 0; j < 5; j++) {
                                double lat = 19.0 + rnd.nextDouble() * 6.0; // México aproximado
                                double lon = -106.0 + rnd.nextDouble() * 10.0;
                                int minutesAgo = rnd.nextInt(1000);
                                pp.setDouble(1, lat);
                                pp.setDouble(2, lon);
                                pp.setInt(3, minutesAgo);
                                pp.setInt(4, uid);
                                pp.setInt(5, firstVeh);
                                try { pp.executeUpdate(); } catch (Exception ex) { /* ignore */ }
                            }
                        }
                    } else {
                        try (PreparedStatement pp = connP.prepareStatement(sqlPno)) {
                            for (int j = 0; j < 5; j++) {
                                double lat = 19.0 + rnd.nextDouble() * 6.0; // México aproximado
                                double lon = -106.0 + rnd.nextDouble() * 10.0;
                                int minutesAgo = rnd.nextInt(1000);
                                pp.setDouble(1, lat);
                                pp.setDouble(2, lon);
                                pp.setInt(3, minutesAgo);
                                pp.setInt(4, uid);
                                try { pp.executeUpdate(); } catch (Exception ex) { /* ignore */ }
                            }
                        }
                    }
                }
            }

            System.out.println("Datos aleatorios insertados para usuarios (vehículos y posiciones)");
        } catch (Exception e) {
            System.out.println("Error insertando datos aleatorios: " + e.getMessage());
        }
    }
}