package com.trackver.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {
    public static class VehiculoDTO {
        public final int id;
        public final String marca;
        public final String modelo;
        public final String placas;
        public final int anio;
        public final Integer usuarioId;
        public VehiculoDTO(int id, String marca, String modelo, String placas, int anio, Integer usuarioId) {
            this.id = id;
            this.marca = marca;
            this.modelo = modelo;
            this.placas = placas;
            this.anio = anio;
            this.usuarioId = usuarioId;
        }
    }

    public static boolean crearVehiculo(String marca, String modelo, String placas, int anio) {
        String sql = "INSERT INTO vehiculos (marca, modelo, placas, anio) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectarVehiculos();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, marca);
            pstmt.setString(2, modelo);
            pstmt.setString(3, placas);
            pstmt.setInt(4, anio);
            pstmt.executeUpdate();
            return true;
        } catch (Exception e) {
            System.out.println("Error creando vehículo: " + e.getMessage());
            return false;
        }
    }

    public static List<VehiculoDTO> listarVehiculos() {
        List<VehiculoDTO> lista = new ArrayList<>();
        String sql = "SELECT id, marca, modelo, placas, anio, usuario_id FROM vehiculos";
        try (Connection conn = ConexionSQLite.conectarVehiculos();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new VehiculoDTO(
                    rs.getInt("id"),
                    rs.getString("marca"),
                    rs.getString("modelo"),
                    rs.getString("placas"),
                    rs.getInt("anio"),
                    // usuario_id puede ser NULL
                    rs.getObject("usuario_id") == null ? null : rs.getInt("usuario_id")
                ));
            }
        } catch (Exception e) {
            System.out.println("Error listando vehículos: " + e.getMessage());
        }
        return lista;
    }

    public static int contarVehiculosPorUsuario(int usuarioId) {
        String sql = "SELECT COUNT(*) as cnt FROM vehiculos WHERE usuario_id = ?";
        try (Connection conn = ConexionSQLite.conectarVehiculos();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("cnt");
        } catch (Exception e) {
            System.out.println("Error contando vehículos por usuario: " + e.getMessage());
        }
        return 0;
    }

    public static VehiculoDTO obtenerPorId(int id) {
        String sql = "SELECT id, marca, modelo, placas, anio, usuario_id FROM vehiculos WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectarVehiculos();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new VehiculoDTO(
                        rs.getInt("id"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getString("placas"),
                        rs.getInt("anio"),
                        rs.getObject("usuario_id") == null ? null : rs.getInt("usuario_id")
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("Error obteniendo vehículo por id: " + e.getMessage());
        }
        return null;
    }

    public static boolean eliminarVehiculo(int id) {
        String sql = "DELETE FROM vehiculos WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectarVehiculos();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("Error eliminando vehículo: " + e.getMessage());
            return false;
        }
    }
}
