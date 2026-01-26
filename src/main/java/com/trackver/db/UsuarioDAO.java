package com.trackver.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // DTO para transportar datos de usuario
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

    // Crear un nuevo usuario
    public static boolean crearUsuario(String nombre, String correo, String contrasena, int nivelAcceso) {
        String sql = "INSERT INTO usuarios (nombre, correo, contrasena, nivelAcceso) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectarUsuarios();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, correo);
            pstmt.setString(3, contrasena);
            pstmt.setInt(4, nivelAcceso);
            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Error creando usuario: " + e.getMessage());
            return false;
        }
    }

    // Buscar usuario por correo y contraseña (login)
    public static UsuarioDTO buscarPorCorreoYPass(String correo, String contrasena) {
        String sql = "SELECT id, nombre, correo, nivelAcceso FROM usuarios WHERE correo = ? AND contrasena = ?";
        try (Connection conn = ConexionSQLite.conectarUsuarios();
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
        } catch (Exception e) {
            System.out.println("Error en login: " + e.getMessage());
        }
        return null;
    }

    // Listar todos los usuarios
    public static List<UsuarioDTO> listarUsuarios() {
        List<UsuarioDTO> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, correo, nivelAcceso FROM usuarios";
        try (Connection conn = ConexionSQLite.conectarUsuarios();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new UsuarioDTO(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getInt("nivelAcceso")
                ));
            }
        } catch (Exception e) {
            System.out.println("Error listando usuarios: " + e.getMessage());
        }
        return lista;
    }

    // Eliminar usuario por ID
    public static boolean eliminarUsuario(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectarUsuarios();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int filas = pstmt.executeUpdate();
            return filas > 0;
        } catch (Exception e) {
            System.out.println("Error eliminando usuario: " + e.getMessage());
            return false;
        }
    }

    // Actualizar datos de usuario (nombre, correo, nivelAcceso)
    public static boolean actualizarUsuario(int id, String nombre, String correo, int nivelAcceso) {
        String sql = "UPDATE usuarios SET nombre = ?, correo = ?, nivelAcceso = ? WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectarUsuarios();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, correo);
            pstmt.setInt(3, nivelAcceso);
            pstmt.setInt(4, id);
            int filas = pstmt.executeUpdate();
            return filas > 0;
        } catch (Exception e) {
            System.out.println("Error actualizando usuario: " + e.getMessage());
            return false;
        }
    }

    // Actualizar contraseña de usuario
    public static boolean actualizarContrasena(int id, String nuevaContrasena) {
        String sql = "UPDATE usuarios SET contrasena = ? WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectarUsuarios();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nuevaContrasena);
            pstmt.setInt(2, id);
            int filas = pstmt.executeUpdate();
            return filas > 0;
        } catch (Exception e) {
            System.out.println("Error actualizando contraseña: " + e.getMessage());
            return false;
        }
    }

    // Verificar contraseña por id de usuario
    public static boolean verificarPasswordPorId(int id, String contrasena) {
        String sql = "SELECT id FROM usuarios WHERE id = ? AND contrasena = ?";
        try (Connection conn = ConexionSQLite.conectarUsuarios();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, contrasena);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.out.println("Error verificando password: " + e.getMessage());
            return false;
        }
    }
}