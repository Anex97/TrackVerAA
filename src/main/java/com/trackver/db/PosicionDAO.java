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
        public final Double velocidad;
        public final String descripcion;
        public final String estado;
        public PosicionDTO(int id, double latitud, double longitud, String fechaHora, int usuarioId, int vehiculoId, Double velocidad, String descripcion, String estado) {
            this.id = id;
            this.latitud = latitud;
            this.longitud = longitud;
            this.fechaHora = fechaHora;
            this.usuarioId = usuarioId;
            this.vehiculoId = vehiculoId;
            this.velocidad = velocidad;
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
        return registrarPosicion(latitud, longitud, usuarioId, null, null, null);
    }

    // Registrar posición con vehiculoId opcional, descripción opcional y velocidad (km/h) opcional
    public static boolean registrarPosicion(double latitud, double longitud, int usuarioId, Integer vehiculoId, String descripcion, Double velocidad) {
        String sqlWithVeh = "INSERT INTO posiciones (latitud, longitud, fechaHora, usuario_id, vehiculo_id, descripcion, estado, velocidad) VALUES (?, ?, datetime('now'), ?, ?, ?, ?, ?)";
        String sqlNoVeh = "INSERT INTO posiciones (latitud, longitud, fechaHora, usuario_id, descripcion, estado, velocidad) VALUES (?, ?, datetime('now'), ?, ?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectarPosiciones()) {
            Integer usedVehId = vehiculoId;
            if (usedVehId == null || usedVehId <= 0) {
                // buscar un vehículo del usuario
                String findVeh = "SELECT id FROM vehiculos WHERE usuario_id = ? LIMIT 1";
                try (PreparedStatement psFind = conn.prepareStatement(findVeh)) {
                    psFind.setInt(1, usuarioId);
                    try (ResultSet rv = psFind.executeQuery()) {
                        if (rv.next()) {
                            usedVehId = rv.getInt("id");
                        }
                    }
                }
            }

            String estado = "";
            if (usedVehId != null && usedVehId > 0) {
                try (PreparedStatement pstmt = conn.prepareStatement(sqlWithVeh)) {
                    pstmt.setDouble(1, latitud);
                    pstmt.setDouble(2, longitud);
                    pstmt.setInt(3, usuarioId);
                    pstmt.setInt(4, usedVehId);
                    pstmt.setString(5, descripcion);
                    pstmt.setString(6, estado);
                    if (velocidad == null) pstmt.setNull(7, java.sql.Types.REAL); else pstmt.setDouble(7, velocidad);
                    pstmt.executeUpdate();
                }
            } else {
                try (PreparedStatement pstmt = conn.prepareStatement(sqlNoVeh)) {
                    pstmt.setDouble(1, latitud);
                    pstmt.setDouble(2, longitud);
                    pstmt.setInt(3, usuarioId);
                    pstmt.setString(4, descripcion);
                    pstmt.setString(5, estado);
                    if (velocidad == null) pstmt.setNull(6, java.sql.Types.REAL); else pstmt.setDouble(6, velocidad);
                    pstmt.executeUpdate();
                }
            }

            // Después de insertar, evaluar reglas de alerta (velocidad y geocercas)
            try {
                // Velocidad: verificar si hay un límite asignado al vehículo
                if (usedVehId != null && usedVehId > 0 && velocidad != null) {
                    try (Connection connA = ConexionSQLite.conectarAlertas();
                         PreparedStatement psVel = connA.prepareStatement("SELECT vel_max_kmh FROM velocidades_asignadas WHERE vehiculo_id = ? AND activo = 1 LIMIT 1")) {
                        psVel.setInt(1, usedVehId);
                        try (ResultSet rv = psVel.executeQuery()) {
                            if (rv.next()) {
                                double lim = rv.getDouble("vel_max_kmh");
                                if (velocidad > lim) {
                                    // comprobar throttle (10 minutos)
                                    boolean existe = alertaExisteReciente(connA, usedVehId, "Velocidad", "%Velocidad%", 10);
                                    if (!existe) {
                                        String desc = String.format("Exceso de velocidad: %.1f km/h (límite %.1f)", velocidad, lim);
                                        try (PreparedStatement pin = connA.prepareStatement("INSERT INTO alertas (vehiculo_id, tipo, descripcion, fecha, estado) VALUES (?, ?, ?, datetime('now'), 'Pendiente')")) {
                                            pin.setInt(1, usedVehId);
                                            pin.setString(2, "Velocidad");
                                            pin.setString(3, desc);
                                            pin.executeUpdate();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Geocercas: comprobar geocercas activas del usuario o asignadas al vehículo
                try (Connection connA = ConexionSQLite.conectarAlertas()) {
                    String sqlGeo = "SELECT g.id, g.nombre, g.latitud, g.longitud, g.radio_m FROM geocercas g WHERE g.activo = 1 AND (g.usuario_id = ? OR g.usuario_id IS NULL OR g.usuario_id = 0 OR EXISTS(SELECT 1 FROM geocerca_asignaciones ga WHERE ga.geocerca_id = g.id AND ga.vehiculo_id = ?))";
                    try (PreparedStatement psGeo = connA.prepareStatement(sqlGeo)) {
                        psGeo.setInt(1, usuarioId);
                        psGeo.setInt(2, usedVehId == null ? -1 : usedVehId);
                        try (ResultSet rg = psGeo.executeQuery()) {
                            while (rg.next()) {
                                int gid = rg.getInt("id");
                                String gname = rg.getString("nombre");
                                double glat = rg.getDouble("latitud");
                                double glon = rg.getDouble("longitud");
                                double gr = rg.getDouble("radio_m");
                                double dist = distanciaMetros(latitud, longitud, glat, glon);
                                if (dist > gr) {
                                    // fuera de la geocerca → generar alerta (throttle 10 min)
                                    String likeDesc = "%GeocercaId:" + gid + "%";
                                    boolean existe = alertaExisteReciente(connA, usedVehId == null ? 0 : usedVehId, "Geocerca", likeDesc, 10);
                                    if (!existe) {
                                        String desc = String.format("Salida de geocerca (id=%d, nombre=%s) distancia=%.0fm radio=%.0fm", gid, gname == null ? "" : gname, dist, gr);
                                        try (PreparedStatement pin = connA.prepareStatement("INSERT INTO alertas (vehiculo_id, tipo, descripcion, fecha, estado) VALUES (?, ?, ?, datetime('now'), 'Pendiente')")) {
                                            pin.setInt(1, usedVehId == null ? 0 : usedVehId);
                                            pin.setString(2, "Geocerca");
                                            // incluir id para poder filtrar
                                            pin.setString(3, "GeocercaId:" + gid + " - " + desc);
                                            pin.executeUpdate();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error evaluando reglas de alerta: " + ex.getMessage());
            }

            return true;
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
                // Leer velocidad si existe (puede ser NULL)
                Double vel = rs.getObject("velocidad") == null ? null : rs.getDouble("velocidad");
                lista.add(new PosicionDTO(
                    rs.getInt("id"),
                    rs.getDouble("latitud"),
                    rs.getDouble("longitud"),
                    rs.getString("fechaHora"),
                    rs.getInt("usuario_id"),
                    rs.getInt("vehiculo_id"),
                    vel,
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
                    rs.getObject("velocidad") == null ? null : rs.getDouble("velocidad"),
                    rs.getString("descripcion"),
                    rs.getString("estado")
                ));
            }
        } catch (Exception e) {
            System.out.println("Error listando todas las posiciones: " + e.getMessage());
        }
        return lista;
    }

    // Comprueba si hay una alerta reciente del mismo tipo/vehículo (throttle)
    private static boolean alertaExisteReciente(Connection connAlertas, int vehiculoId, String tipo, String descripcionLike, int minutos) {
        try (PreparedStatement ps = connAlertas.prepareStatement("SELECT COUNT(*) as c FROM alertas WHERE vehiculo_id = ? AND tipo = ? AND fecha >= datetime('now','-' || ? || ' minutes') AND descripcion LIKE ?")) {
            ps.setInt(1, vehiculoId);
            ps.setString(2, tipo);
            ps.setInt(3, minutos);
            ps.setString(4, descripcionLike);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("c") > 0;
            }
        } catch (Exception e) {
            System.out.println("Error comprobando alertas recientes: " + e.getMessage());
        }
        return false;
    }

    // Calcula distancia aproximada entre dos puntos en metros (Haversine)
    private static double distanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0; // radio Tierra en metros
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }
}