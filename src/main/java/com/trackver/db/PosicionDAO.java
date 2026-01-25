package com.trackver.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PosicionDAO {

    // DTO para transportar datos de posición
    public static class PosicionDTO {
        public final int id;
        public final double latitud;
        public final double longitud;
        public final String fechaHora;
        public final int usuarioId;
        public final int vehiculoId;

        public PosicionDTO(int id, double latitud, double longitud, String fechaHora, int usuarioId, int vehiculoId) {
            this.id = id;
            this.latitud = latitud;
            this.longitud = longitud;
            this.fechaHora = fechaHora;
            this.usuarioId = usuarioId;
            this.vehiculoId = vehiculoId;
        }
    }

    // Registrar una nueva posición
    public static boolean registrarPosicion(double latitud, double longitud, int usuarioId) {
        // Intentar asociar la posición a un vehículo del usuario si existe
        String findVeh = "SELECT id FROM vehiculos WHERE usuario_id = ? LIMIT 1";
        try (Connection conn = ConexionSQLite.conectarPosiciones();
             PreparedStatement psFind = conn.prepareStatement(findVeh)) {
            psFind.setInt(1, usuarioId);
            try (ResultSet rv = psFind.executeQuery()) {
                if (rv.next()) {
                    int vehId = rv.getInt("id");
                    String sql = "INSERT INTO posiciones (latitud, longitud, fechaHora, usuario_id, vehiculo_id) VALUES (?, ?, datetime('now'), ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setDouble(1, latitud);
                        pstmt.setDouble(2, longitud);
                        pstmt.setInt(3, usuarioId);
                        pstmt.setInt(4, vehId);
                        pstmt.executeUpdate();
                        return true;
                    }
                } else {
                    String sql = "INSERT INTO posiciones (latitud, longitud, fechaHora, usuario_id) VALUES (?, ?, datetime('now'), ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setDouble(1, latitud);
                        pstmt.setDouble(2, longitud);
                        pstmt.setInt(3, usuarioId);
                        pstmt.executeUpdate();
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error registrando posición: " + e.getMessage());
            return false;
        }
    }

    // Listar todas las posiciones de un usuario
    public static List<PosicionDTO> listarPosicionesPorUsuario(int usuarioId) {
        List<PosicionDTO> lista = new ArrayList<>();
        String sql = "SELECT * FROM posiciones WHERE usuario_id = ? ORDER BY fechaHora DESC";
        try (Connection conn = ConexionSQLite.conectarPosiciones();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lista.add(new PosicionDTO(
                    rs.getInt("id"),
                    rs.getDouble("latitud"),
                    rs.getDouble("longitud"),
                    rs.getString("fechaHora"),
                    rs.getInt("usuario_id"),
                    rs.getInt("vehiculo_id")
                ));
            }
        } catch (Exception e) {
            System.out.println("Error listando posiciones: " + e.getMessage());
        }
        return lista;
    }

    // Listar todas las posiciones (para Admin)
    public static List<PosicionDTO> listarTodasLasPosiciones() {
        List<PosicionDTO> lista = new ArrayList<>();
        String sql = "SELECT * FROM posiciones ORDER BY fechaHora DESC";
        try (Connection conn = ConexionSQLite.conectarPosiciones();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new PosicionDTO(
                    rs.getInt("id"),
                    rs.getDouble("latitud"),
                    rs.getDouble("longitud"),
                    rs.getString("fechaHora"),
                    rs.getInt("usuario_id"),
                    rs.getInt("vehiculo_id")
                ));
            }
        } catch (Exception e) {
            System.out.println("Error listando todas las posiciones: " + e.getMessage());
        }
        return lista;
    }
}