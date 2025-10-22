package com.trackver.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    // =========================
    // LOGIN
    // =========================
    public static UsuarioDTO buscarPorCorreoYPass(String correo, String contrasena) {
        String sql = "SELECT id, nombre, correo, nivelAcceso FROM usuarios WHERE correo = ? AND contrasena = ?";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, correo);
            pstmt.setString(2, contrasena);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new UsuarioDTO(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("correo"),
                    rs.getInt("nivelAcceso")
                );
            }
            return null;
        } catch (Exception e) {
            System.out.println("❌ Error en login: " + e.getMessage());
            return null;
        }
    }

    // =========================
    // CREAR USUARIO
    // =========================
    public static boolean crearUsuario(String nombre, String correo, String contrasena, int nivelAcceso) {
        String sql = "INSERT INTO usuarios (nombre, correo, contrasena, nivelAcceso) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setString(2, correo);
            pstmt.setString(3, contrasena);
            pstmt.setInt(4, nivelAcceso);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Error creando usuario: " + e.getMessage());
            return false;
        }
    }

    // =========================
    // ELIMINAR USUARIO
    // =========================
    public static boolean eliminarUsuario(String correo) {
        String sql = "DELETE FROM usuarios WHERE correo = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, correo);
            int filas = pstmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.out.println("❌ Error eliminando usuario: " + e.getMessage());
            return false;
        }
    }

    // =========================
    // DTO (Data Transfer Object)
    // =========================
    public static class UsuarioDTO {
        public final int id;
        public final String nombre;
        public final String correo;
        public final int nivelAcceso;

        public UsuarioDTO(int id, String nombre, String correo, int nivelAcceso) {
            this.id = id;
            this.nombre = nombre;
            this.correo = correo;
            this.nivelAcceso = nivelAcceso;
        }
    }
}