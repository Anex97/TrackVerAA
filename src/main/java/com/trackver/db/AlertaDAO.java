package com.trackver.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertaDAO {
    public static class AlertaDTO {
        public final int id;
        public final int vehiculoId;
        public final String tipo;
        public final String descripcion;
        public final String fecha;
        public final String estado;

        public AlertaDTO(int id, int vehiculoId, String tipo, String descripcion, String fecha, String estado) {
            this.id = id;
            this.vehiculoId = vehiculoId;
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.fecha = fecha;
            this.estado = estado;
        }
    }

    public static boolean crearAlerta(int vehiculoId, String tipo, String descripcion, String estado) {
        String sql = "INSERT INTO alertas (vehiculo_id, tipo, descripcion, fecha, estado) VALUES (?, ?, ?, date('now'), ?)";
        try (Connection conn = ConexionSQLite.conectarAlertas();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehiculoId);
            pstmt.setString(2, tipo);
            pstmt.setString(3, descripcion);
            pstmt.setString(4, estado);
            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Error creando alerta: " + e.getMessage());
            return false;
        }
    }

    public static List<AlertaDTO> listarAlertas() {
        List<AlertaDTO> lista = new ArrayList<>();
        String sql = "SELECT id, vehiculo_id, tipo, descripcion, fecha, estado FROM alertas";
        try (Connection conn = ConexionSQLite.conectarAlertas();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new AlertaDTO(
                    rs.getInt("id"),
                    rs.getInt("vehiculo_id"),
                    rs.getString("tipo"),
                    rs.getString("descripcion"),
                    rs.getString("fecha"),
                    rs.getString("estado")
                ));
            }
        } catch (Exception e) {
            System.out.println("Error listando alertas: " + e.getMessage());
        }
        return lista;
    }

    public static boolean eliminarAlerta(int id) {
        String sql = "DELETE FROM alertas WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectarAlertas();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error eliminando alerta: " + e.getMessage());
            return false;
        }
    }
}
