package com.trackver.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class PosicionDAO {

        public static boolean registrarPosicion(double latitud, double longitud, int usuarioId) {
        String sql = "INSERT INTO posiciones (latitud, longitud, fecha, usuario_id) VALUES (?, ?, datetime('now'), ?)";
        try (Connection conn = ConexionSQLite.conectar();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, latitud);
            pstmt.setDouble(2, longitud);
            pstmt.setInt(3, usuarioId);
            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Error registrando posición: " + e.getMessage());
            return false;
        }
    }

    public static List<String> listarPosicionesPorUsuario(int usuarioId) {
        List<String> posiciones = new ArrayList<>();
        String sql = "SELECT latitud, longitud, fecha FROM posiciones WHERE usuario_id = ? ORDER BY fecha DESC";
        try (Connection conn = ConexionSQLite.conectar();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                posiciones.add("Latitud: " + rs.getDouble("latitud") +
                            ", Longitud: " + rs.getDouble("longitud") +
                            ", Fecha: " + rs.getString("fecha"));
            }
        } catch (Exception e) {
            System.out.println("Error listando posiciones: " + e.getMessage());
        }
        return posiciones;
    }

}