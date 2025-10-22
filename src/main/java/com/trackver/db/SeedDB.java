package com.trackver.db;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class SeedDB {
    public static void insertarUsuarios() {
        String sql = "INSERT OR IGNORE INTO usuarios (nombre, correo, contrasena, nivelAcceso) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionSQLite.conectar();
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
            pstmt.setString(3, "abcd");
            pstmt.setInt(4, 1);
            pstmt.executeUpdate();

            System.out.println("✅ Usuarios iniciales insertados (si no existían).");
        } catch (Exception e) {
            System.out.println("❌ Error insertando usuarios: " + e.getMessage());
        }
    }
}