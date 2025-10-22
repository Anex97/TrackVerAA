package com.trackver.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AuditoriaDAO {

    // DTO para transportar datos de auditoría
    public static class AuditoriaDTO {
        public final int id;
        public final String titulo;
        public final String descripcion;
        public final String fecha;
        public final String estado;
        public final int usuarioId;

        public AuditoriaDTO(int id, String titulo, String descripcion, String fecha, String estado, int usuarioId) {
            this.id = id;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.fecha = fecha;
            this.estado = estado;
            this.usuarioId = usuarioId;
        }
    }

    // Crear auditoría en la base de datos
    public static boolean crearAuditoria(String titulo, String descripcion, int usuarioId) {
        String sql = "INSERT INTO auditorias (titulo, descripcion, fecha, estado, usuario_id) VALUES (?, ?, date('now'), ?, ?)";
        try (Connection conn = ConexionSQLite.conectar();
             var pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, titulo);
            pstmt.setString(2, descripcion);
            pstmt.setString(3, "Pendiente");
            pstmt.setInt(4, usuarioId);

            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("❌ Error creando auditoría: " + e.getMessage());
            return false;
        }
    }

    // Listar todas las auditorías
    public static List<AuditoriaDTO> listarAuditorias() {
        List<AuditoriaDTO> lista = new ArrayList<>();
        String sql = "SELECT * FROM auditorias";
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new AuditoriaDTO(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descripcion"),
                        rs.getString("fecha"),
                        rs.getString("estado"),
                        rs.getInt("usuario_id")
                ));
            }
        } catch (Exception e) {
            System.out.println("❌ Error listando auditorías: " + e.getMessage());
        }
        return lista;
    }

    // Validar auditoría (cambiar estado a "Validada")
    public static boolean validarAuditoria(int id) {
        String sql = "UPDATE auditorias SET estado = 'Validada' WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectar();
             var pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int filas = pstmt.executeUpdate();
            return filas > 0;
        } catch (Exception e) {
            System.out.println("❌ Error validando auditoría: " + e.getMessage());
            return false;
        }
    }
    // Listar solo auditorías validadas
    public static List<AuditoriaDTO> listarValidadas() {
        List<AuditoriaDTO> lista = new ArrayList<>();
        String sql = "SELECT * FROM auditorias WHERE estado = 'Validada'";
        try (Connection conn = ConexionSQLite.conectar();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new AuditoriaDTO(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descripcion"),
                        rs.getString("fecha"),
                        rs.getString("estado"),
                        rs.getInt("usuario_id")
                ));
            }
        } catch (Exception e) {
            System.out.println("❌ Error listando auditorías validadas: " + e.getMessage());
        }
        return lista;
    }
}