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
        public final String descripcion;
        public final String estado;

        public PosicionDTO(int id, double latitud, double longitud, String fechaHora, int usuarioId, int vehiculoId, String descripcion, String estado) {
            this.id = id;
            this.latitud = latitud;
            this.longitud = longitud;
            this.fechaHora = fechaHora;
            this.usuarioId = usuarioId;
            this.vehiculoId = vehiculoId;
            this.descripcion = descripcion;
            this.estado = estado;
        }
    }

    // Reverse geocode removed — no external calls from backend for now.

    // Migrar filas existentes: rellenar descripcion vacía y estado a partir de lat/lon cuando falte.
    public static void migrarEstadoYDescripcion() {
        String select = "SELECT id, latitud, longitud, descripcion, estado FROM posiciones WHERE descripcion IS NULL OR estado IS NULL";
        String update = "UPDATE posiciones SET descripcion = ?, estado = ? WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectarPosiciones();
             PreparedStatement ps = conn.prepareStatement(select);
             PreparedStatement psUpdate = conn.prepareStatement(update);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String descripcion = rs.getString("descripcion");
                String estado = rs.getString("estado");
                if (descripcion == null) descripcion = "";
                if (estado == null) estado = "";
                psUpdate.setString(1, descripcion);
                psUpdate.setString(2, estado);
                psUpdate.setInt(3, id);
                psUpdate.executeUpdate();
            }
            System.out.println("Migración de estado/descripcion completada.");
        } catch (Exception e) {
            System.out.println("Error migrando estado/descripcion: " + e.getMessage());
        }
    }

    // Registrar una nueva posición (busca vehículo asociado si no se especifica)
    public static boolean registrarPosicion(double latitud, double longitud, int usuarioId) {
        return registrarPosicion(latitud, longitud, usuarioId, null, null);
    }

    // Registrar posición con vehiculoId opcional y descripción opcional
    public static boolean registrarPosicion(double latitud, double longitud, int usuarioId, Integer vehiculoId, String descripcion) {
        String sqlWithVeh = "INSERT INTO posiciones (latitud, longitud, fechaHora, usuario_id, vehiculo_id, descripcion, estado) VALUES (?, ?, datetime('now'), ?, ?, ?, ?)";
        String sqlNoVeh = "INSERT INTO posiciones (latitud, longitud, fechaHora, usuario_id, descripcion, estado) VALUES (?, ?, datetime('now'), ?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectarPosiciones()) {
            if (vehiculoId != null && vehiculoId > 0) {
                String estado = "";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlWithVeh)) {
                    pstmt.setDouble(1, latitud);
                    pstmt.setDouble(2, longitud);
                    pstmt.setInt(3, usuarioId);
                    pstmt.setInt(4, vehiculoId);
                    pstmt.setString(5, descripcion);
                    pstmt.setString(6, estado);
                    pstmt.executeUpdate();
                    return true;
                }
            }
            // buscar un vehículo del usuario
            String findVeh = "SELECT id FROM vehiculos WHERE usuario_id = ? LIMIT 1";
            try (PreparedStatement psFind = conn.prepareStatement(findVeh)) {
                psFind.setInt(1, usuarioId);
                try (ResultSet rv = psFind.executeQuery()) {
                    if (rv.next()) {
                        int vehId = rv.getInt("id");
                        String estado = "";
                        try (PreparedStatement pstmt = conn.prepareStatement(sqlWithVeh)) {
                            pstmt.setDouble(1, latitud);
                            pstmt.setDouble(2, longitud);
                            pstmt.setInt(3, usuarioId);
                            pstmt.setInt(4, vehId);
                            pstmt.setString(5, descripcion);
                            pstmt.setString(6, estado);
                            pstmt.executeUpdate();
                            return true;
                        }
                    } else {
                        String estado = "";
                        try (PreparedStatement pstmt = conn.prepareStatement(sqlNoVeh)) {
                            pstmt.setDouble(1, latitud);
                            pstmt.setDouble(2, longitud);
                            pstmt.setInt(3, usuarioId);
                            pstmt.setString(4, descripcion);
                            pstmt.setString(5, estado);
                            pstmt.executeUpdate();
                            return true;
                        }
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
                    rs.getInt("vehiculo_id"),
                    rs.getString("descripcion"),
                    rs.getString("estado")
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
                    rs.getInt("vehiculo_id"),
                    rs.getString("descripcion"),
                    rs.getString("estado")
                ));
            }
        } catch (Exception e) {
            System.out.println("Error listando todas las posiciones: " + e.getMessage());
        }
        return lista;
    }
}