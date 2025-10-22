package com.trackver.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class AuditoriaDAO {

    public static boolean crearAuditoria(String titulo, String descripcion, int usuarioId) {
        String sql = "INSERT INTO auditorias (titulo, descripcion, fecha, estado, usuario_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, titulo);
            pstmt.setString(2, descripcion);
            pstmt.setString(3, LocalDate.now().toString());
            pstmt.setString(4, "Pendiente"); // estado inicial
            pstmt.setInt(5, usuarioId);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Error creando auditoría: " + e.getMessage());
            return false;
        }
    }
}