package com.trackver.app;

import com.trackver.db.InitDB;
import com.trackver.db.SeedDB;
import com.trackver.db.UsuarioDAO;
import com.trackver.db.UsuarioDAO.UsuarioDTO;

import static spark.Spark.*;

public class Main {
    public static void main(String[] args) {
        // Inicializar DB y datos semilla
        InitDB.crearTablaUsuarios();
        InitDB.crearTablaAuditorias();
        // Crear vehiculos antes de posiciones para que migraciones que consulten vehiculos funcionen
        InitDB.crearTablaVehiculos();
        InitDB.crearTablaPosiciones();
        InitDB.crearTablaAlertas();

        SeedDB.insertarUsuarios();
        SeedDB.insertarAuditorias();
        SeedDB.insertarPosiciones();
        SeedDB.insertarVehiculos();
        SeedDB.insertarAlertas();
        // Insertar datos aleatorios adicionales para demo (vehículos y posiciones)
        SeedDB.insertarDatosAleatorios();

        // Configuración del servidor web embebido
        port(4567);

        // Servir archivos estáticos desde la carpeta del proyecto 20_FrontEnd
        staticFiles.externalLocation("c:/Repos/TrackVerAA/20_FrontEnd");

        // Endpoint POST /api/login - espera form-urlencoded con 'correo' y 'contrasena'
        post("/api/login", (req, res) -> {
            String correo = req.queryParams("correo");
            String contrasena = req.queryParams("contrasena");

            UsuarioDTO u = UsuarioDAO.buscarPorCorreoYPass(correo, contrasena);
            res.type("application/json; charset=UTF-8");
            if (u != null) {
                return String.format("{\"ok\":true,\"id\":%d,\"nombre\":\"%s\",\"nivelAcceso\":%d}",
                        u.id, u.nombre.replace("\"", "\\\""), u.nivelAcceso);
            } else {
                res.status(401);
                return "{\"ok\":false}";
            }
        });

        // API: conteo de vehiculos (global o por usuarioId)
        get("/api/vehiculos/count", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String uid = req.queryParams("usuarioId");
            int count;
            if (uid == null) {
                count = com.trackver.db.VehiculoDAO.listarVehiculos().size();
            } else {
                int usuarioId = Integer.parseInt(uid);
                count = com.trackver.db.VehiculoDAO.contarVehiculosPorUsuario(usuarioId);
            }
            return String.format("{\"count\":%d}", count);
        });

        // API: listar vehículos (usuarioId opcional)
        get("/api/vehiculos", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String uid = req.queryParams("usuarioId");
            java.util.List<com.trackver.db.VehiculoDAO.VehiculoDTO> lista = com.trackver.db.VehiculoDAO.listarVehiculos();
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            boolean first = true;
            for (com.trackver.db.VehiculoDAO.VehiculoDTO v : lista) {
                if (uid != null) {
                    try {
                        int uidInt = Integer.parseInt(uid);
                        if (v.usuarioId == null || v.usuarioId.intValue() != uidInt) continue;
                    } catch (NumberFormatException nfe) {
                        // ignore filter if invalid
                    }
                }
                if (!first) sb.append(',');
                first = false;
                String marca = v.marca == null ? "" : v.marca.replace("\"", "\\\"");
                String modelo = v.modelo == null ? "" : v.modelo.replace("\"", "\\\"");
                String placas = v.placas == null ? "" : v.placas.replace("\"", "\\\"");
                String usuarioIdJson = v.usuarioId == null ? "null" : String.valueOf(v.usuarioId);
                sb.append(String.format("{\"id\":%d,\"marca\":\"%s\",\"modelo\":\"%s\",\"placas\":\"%s\",\"anio\":%d,\"usuarioId\":%s}",
                    v.id, marca, modelo, placas, v.anio, usuarioIdJson));
            }
            sb.append(']');
            return sb.toString();
        });

        // API: eliminar vehículo (requiere id, usuarioId y contraseña para confirmar)
        post("/api/vehiculos/delete", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String idS = req.queryParams("id");
            String uidS = req.queryParams("usuarioId");
            String pass = req.queryParams("password");
            if (idS == null || uidS == null || pass == null) {
                res.status(400);
                return "{\"ok\":false,\"message\":\"Faltan parámetros\"}";
            }
            int id = Integer.parseInt(idS);
            int uid = Integer.parseInt(uidS);
            // verificar contraseña
            boolean ok = com.trackver.db.UsuarioDAO.verificarPasswordPorId(uid, pass);
            if (!ok) {
                res.status(401);
                return "{\"ok\":false,\"message\":\"Contraseña incorrecta\"}";
            }
            // verificar que el vehículo pertenece al usuario
            com.trackver.db.VehiculoDAO.VehiculoDTO v = com.trackver.db.VehiculoDAO.obtenerPorId(id);
            if (v == null) {
                res.status(404);
                return "{\"ok\":false,\"message\":\"Vehículo no encontrado\"}";
            }
            if (v.usuarioId == null || v.usuarioId.intValue() != uid) {
                res.status(403);
                return "{\"ok\":false,\"message\":\"No autorizado\"}";
            }
            boolean deleted = com.trackver.db.VehiculoDAO.eliminarVehiculo(id);
            if (deleted) return "{\"ok\":true}";
            res.status(500);
            return "{\"ok\":false,\"message\":\"No se pudo eliminar\"}";
        });

        // API: crear vehículo (asociado al usuario logueado)
        post("/api/vehiculos", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String marca = req.queryParams("marca");
            String modelo = req.queryParams("modelo");
            String placas = req.queryParams("placas");
            String anioS = req.queryParams("anio");
            String uid = req.queryParams("usuarioId");
            if (marca == null || placas == null || anioS == null || uid == null) {
                res.status(400);
                return "{\"ok\":false,\"error\":\"missing_fields\",\"message\":\"Faltan campos requeridos\"}";
            }
            int anio = Integer.parseInt(anioS);
            int usuarioId = Integer.parseInt(uid);
            try {
                int newId = com.trackver.db.VehiculoDAO.crearVehiculoConUsuario(marca, modelo == null ? "" : modelo, placas, anio, usuarioId);
                if (newId > 0) {
                    return String.format("{\"ok\":true,\"id\":%d}", newId);
                } else {
                    res.status(500);
                    return "{\"ok\":false,\"error\":\"insert_failed\",\"message\":\"No se pudo crear el vehículo\"}";
                }
            } catch (Exception e) {
                String msg = e.getMessage() == null ? "" : e.getMessage();
                if (msg.contains("UNIQUE constraint failed") || msg.contains("vehiculos.placas")) {
                    res.status(409);
                    return "{\"ok\":false,\"error\":\"duplicate_placas\",\"message\":\"Placas ya registradas\"}";
                }
                res.status(500);
                return String.format("{\"ok\":false,\"error\":\"%s\",\"message\":\"%s\"}",
                        e.getClass().getSimpleName(), msg.replace("\"", "\\\""));
            }
        });

        // API: ultima posicion (por usuarioId opcional). Si no se pasa usuarioId devuelve la última global
        get("/api/posiciones/ultima", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String uid = req.queryParams("usuarioId");
            java.util.List<com.trackver.db.PosicionDAO.PosicionDTO> lista;
            if (uid == null) {
                lista = com.trackver.db.PosicionDAO.listarTodasLasPosiciones();
            } else {
                int usuarioId = Integer.parseInt(uid);
                lista = com.trackver.db.PosicionDAO.listarPosicionesPorUsuario(usuarioId);
            }
            if (lista.isEmpty()) {
                return "{}";
            }
                com.trackver.db.PosicionDAO.PosicionDTO p = lista.get(0);
                // Intentar anexar información del vehículo si está disponible
                String placas = "";
                String marca = "";
                if (p.vehiculoId > 0) {
                    com.trackver.db.VehiculoDAO.VehiculoDTO v = com.trackver.db.VehiculoDAO.obtenerPorId(p.vehiculoId);
                    if (v != null) {
                        placas = v.placas == null ? "" : v.placas.replace("\"", "\\\"");
                        marca = v.marca == null ? "" : v.marca.replace("\"", "\\\"");
                    }
                }
                return String.format("{\"id\":%d,\"latitud\":%f,\"longitud\":%f,\"fechaHora\":\"%s\",\"usuarioId\":%d,\"vehiculoId\":%d,\"vehiculoPlacas\":\"%s\",\"vehiculoMarca\":\"%s\",\"descripcion\":\"%s\",\"estado\":\"%s\"}",
                    p.id, p.latitud, p.longitud, p.fechaHora.replace("\"", "\\\""), p.usuarioId, p.vehiculoId, placas, marca, (p.descripcion==null?"":p.descripcion.replace("\"","\\\"")), (p.estado==null?"":p.estado.replace("\"","\\\"")));
        });

        // API: listar posiciones (usuarioId opcional). Si no se pasa usuarioId devuelve todas las posiciones
        get("/api/posiciones", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String uid = req.queryParams("usuarioId");
            java.util.List<com.trackver.db.PosicionDAO.PosicionDTO> lista;
            if (uid == null) {
                lista = com.trackver.db.PosicionDAO.listarTodasLasPosiciones();
            } else {
                int usuarioId = Integer.parseInt(uid);
                lista = com.trackver.db.PosicionDAO.listarPosicionesPorUsuario(usuarioId);
            }
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            boolean first = true;
            for (com.trackver.db.PosicionDAO.PosicionDTO p : lista) {
                if (!first) sb.append(',');
                first = false;
                sb.append(String.format("{\"id\":%d,\"latitud\":%f,\"longitud\":%f,\"fechaHora\":\"%s\",\"usuarioId\":%d,\"vehiculoId\":%d,\"descripcion\":\"%s\",\"estado\":\"%s\"}",
                    p.id, p.latitud, p.longitud, p.fechaHora.replace("\"", "\\\""), p.usuarioId, p.vehiculoId, (p.descripcion==null?"":p.descripcion.replace("\"","\\\"")), (p.estado==null?"":p.estado.replace("\"","\\\""))));
            }
            sb.append(']');
            return sb.toString();
        });

        // API: registrar una nueva posición (POST). Espera form-urlencoded: lat, lon, usuarioId, descripcion (opcional)
        post("/api/posiciones", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String latS = req.queryParams("lat");
            String lonS = req.queryParams("lon");
            String uidS = req.queryParams("usuarioId");
            String vehIdS = req.queryParams("vehiculoId");
            String descripcion = req.queryParams("descripcion");
            String velS = req.queryParams("velocidad");
            Double velocidad = null;
            if (velS != null && !velS.isEmpty()) {
                try { velocidad = Double.parseDouble(velS); } catch (Exception e) { velocidad = null; }
            }
            if (latS == null || lonS == null || uidS == null) {
                res.status(400);
                return "{\"ok\":false,\"message\":\"Faltan parámetros\"}";
            }
            double lat = Double.parseDouble(latS);
            double lon = Double.parseDouble(lonS);
            int uid = Integer.parseInt(uidS);
            Integer vehId = null;
            if (vehIdS != null && !vehIdS.isEmpty()) {
                try { vehId = Integer.parseInt(vehIdS); } catch (NumberFormatException n) { vehId = null; }
            }
            boolean ok = com.trackver.db.PosicionDAO.registrarPosicion(lat, lon, uid, vehId, descripcion, velocidad);
            if (ok) return "{\"ok\":true}";
            res.status(500);
            return "{\"ok\":false,\"message\":\"No se pudo registrar posición\"}";
        });

        // API: crear geocerca (POST) - params: nombre, usuarioId (opcional), lat, lon, radio_m
        post("/api/geocercas", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String nombre = req.queryParams("nombre");
            String latS = req.queryParams("lat");
            String lonS = req.queryParams("lon");
            String radioS = req.queryParams("radio_m");
            String uidS = req.queryParams("usuarioId");
            if (latS == null || lonS == null || radioS == null) {
                res.status(400);
                return "{\"ok\":false,\"message\":\"Faltan parámetros\"}";
            }
            double lat = Double.parseDouble(latS);
            double lon = Double.parseDouble(lonS);
            double radio = Double.parseDouble(radioS);
            Integer usuarioId = null;
            if (uidS != null && !uidS.isEmpty()) {
                try { usuarioId = Integer.parseInt(uidS); } catch (Exception e) { usuarioId = null; }
            }
            try (java.sql.Connection conn = com.trackver.db.ConexionSQLite.conectarAlertas();
                 java.sql.PreparedStatement ps = conn.prepareStatement("INSERT INTO geocercas (nombre, usuario_id, latitud, longitud, radio_m, activo) VALUES (?, ?, ?, ?, ?, 1)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre == null ? "" : nombre);
                if (usuarioId == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, usuarioId);
                ps.setDouble(3, lat);
                ps.setDouble(4, lon);
                ps.setDouble(5, radio);
                ps.executeUpdate();
                try (java.sql.ResultSet rk = ps.getGeneratedKeys()) {
                    if (rk.next()) {
                        int id = rk.getInt(1);
                        return String.format("{\"ok\":true,\"id\":%d}", id);
                    }
                }
                return "{\"ok\":false}";
            } catch (Exception ex) {
                res.status(500);
                return String.format("{\"ok\":false,\"message\":\"%s\"}", ex.getMessage().replace("\"","\\\""));
            }
        });

        // API: asignar geocerca a vehículo (POST) - params: geocercaId, vehiculoId
        post("/api/geocercas/asignar", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String gidS = req.queryParams("geocercaId");
            String vidS = req.queryParams("vehiculoId");
            if (gidS == null || vidS == null) {
                res.status(400);
                return "{\"ok\":false,\"message\":\"Faltan parámetros\"}";
            }
            int gid = Integer.parseInt(gidS);
            int vid = Integer.parseInt(vidS);
            try (java.sql.Connection conn = com.trackver.db.ConexionSQLite.conectarAlertas();
                 java.sql.PreparedStatement ps = conn.prepareStatement("INSERT INTO geocerca_asignaciones (geocerca_id, vehiculo_id) VALUES (?, ?)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, gid);
                ps.setInt(2, vid);
                ps.executeUpdate();
                return "{\"ok\":true}";
            } catch (Exception ex) {
                res.status(500);
                return String.format("{\"ok\":false,\"message\":\"%s\"}", ex.getMessage().replace("\"","\\\""));
            }
        });

        // API: asignar límite de velocidad a vehículo (POST) - params: vehiculoId, vel_max_kmh
        post("/api/velocidades", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String vidS = req.queryParams("vehiculoId");
            String velS = req.queryParams("vel_max_kmh");
            if (vidS == null || velS == null) {
                res.status(400);
                return "{\"ok\":false,\"message\":\"Faltan parámetros\"}";
            }
            int vid = Integer.parseInt(vidS);
            double vel = Double.parseDouble(velS);
            try (java.sql.Connection conn = com.trackver.db.ConexionSQLite.conectarAlertas();
                 java.sql.PreparedStatement ps = conn.prepareStatement("INSERT INTO velocidades_asignadas (vehiculo_id, vel_max_kmh, activo) VALUES (?, ?, 1)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, vid);
                ps.setDouble(2, vel);
                ps.executeUpdate();
                try (java.sql.ResultSet rk = ps.getGeneratedKeys()) {
                    if (rk.next()) {
                        int id = rk.getInt(1);
                        return String.format("{\"ok\":true,\"id\":%d}", id);
                    }
                }
                return "{\"ok\":false}";
            } catch (Exception ex) {
                res.status(500);
                return String.format("{\"ok\":false,\"message\":\"%s\"}", ex.getMessage().replace("\"","\\\""));
            }
        });

        // Ruta simple para verificar servidor
        get("/api/ping", (req, res) -> "pong");

        // Redirigir raíz al HTML de login
        get("/", (req, res) -> {
            res.redirect("/10_HTML/Index.html");
            return null;
        });

        // Soporte para peticiones directas a /Panel.html (algunos enlaces usan ruta relativa)
        get("/Panel.html", (req, res) -> {
            res.redirect("/10_HTML/Panel.html");
            return null;
        });

        // API: listar alertas (opcional vehiculoId)
        get("/api/alertas", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String vidS = req.queryParams("vehiculoId");
            try (java.sql.Connection conn = com.trackver.db.ConexionSQLite.conectarAlertas();
                 java.sql.PreparedStatement ps = conn.prepareStatement(vidS == null ? "SELECT * FROM alertas ORDER BY fecha DESC" : "SELECT * FROM alertas WHERE vehiculo_id = ? ORDER BY fecha DESC")) {
                if (vidS != null) ps.setInt(1, Integer.parseInt(vidS));
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append('[');
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) sb.append(','); first = false;
                        int id = rs.getInt("id");
                        int vid = rs.getInt("vehiculo_id");
                        String tipo = rs.getString("tipo");
                        String desc = rs.getString("descripcion");
                        String fecha = rs.getString("fecha");
                        String estado = rs.getString("estado");
                        sb.append(String.format("{\"id\":%d,\"vehiculo_id\":%d,\"tipo\":\"%s\",\"descripcion\":\"%s\",\"fecha\":\"%s\",\"estado\":\"%s\"}", id, vid, tipo == null ? "" : tipo.replace("\"","\\\""), desc == null ? "" : desc.replace("\"","\\\""), fecha == null ? "" : fecha.replace("\"","\\\""), estado == null ? "" : estado.replace("\"","\\\"")));
                    }
                    sb.append(']');
                    return sb.toString();
                }
            } catch (Exception ex) {
                res.status(500);
                return String.format("{\"ok\":false,\"message\":\"%s\"}", ex.getMessage().replace("\"","\\\""));
            }
        });

        System.out.println("Servidor web iniciado en http://localhost:4567/ (archivos estáticos: 20_FrontEnd)");
    }
}