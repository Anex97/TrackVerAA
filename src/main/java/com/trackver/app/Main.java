package com.trackver.app;

import com.trackver.db.InitDB;
import com.trackver.db.SeedDB;
import com.trackver.db.UsuarioDAO;
import com.trackver.db.UsuarioDAO.UsuarioDTO;

import static spark.Spark.*;
import spark.Request;
import spark.Response;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    // Helper that halts the request if the current session is not an admin
    private static void requireAdmin(Request req) {
        Object lvl = req.session().attribute("nivelAcceso");
        if (lvl == null) {
            halt(401, "{\"ok\":false,\"message\":\"No autenticado\"}");
        }
        int nivel = 0;
        try {
            if (lvl instanceof Integer) nivel = (Integer) lvl; else nivel = Integer.parseInt(lvl.toString());
        } catch (Exception e) { nivel = 0; }
        if (nivel != 2) {
            halt(403, "{\"ok\":false,\"message\":\"No autorizado\"}");
        }
    }

    public static void main(String[] args) {
        // Inicializar DB y datos semilla
        InitDB.crearTablaUsuarios();
        InitDB.crearTablaAuditorias();
        // Crear vehiculos antes de posiciones para que migraciones que consulten vehiculos funcionen
        InitDB.crearTablaVehiculos();
        InitDB.crearTablaPosiciones();
        InitDB.crearTablaAlertas();

        // Insertar seeds solo si las tablas están vacías (evita duplicados en reinicios)
        try (java.sql.Connection c = com.trackver.db.ConexionSQLite.conectarUsuarios();
             java.sql.Statement st = c.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(1) AS c FROM usuarios")) {
            if (!rs.next() || rs.getInt("c") == 0) {
                SeedDB.insertarUsuarios();
            }
        } catch (Exception e) { System.err.println("Error comprobando usuarios seed: " + e.getMessage()); }

        try (java.sql.Connection c = com.trackver.db.ConexionSQLite.conectarAuditorias();
             java.sql.Statement st = c.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(1) AS c FROM auditorias")) {
            if (!rs.next() || rs.getInt("c") == 0) {
                SeedDB.insertarAuditorias();
            }
        } catch (Exception e) { System.err.println("Error comprobando auditorias seed: " + e.getMessage()); }

        try (java.sql.Connection c = com.trackver.db.ConexionSQLite.conectarPosiciones();
             java.sql.Statement st = c.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(1) AS c FROM posiciones")) {
            if (!rs.next() || rs.getInt("c") == 0) {
                SeedDB.insertarPosiciones();
            }
        } catch (Exception e) { System.err.println("Error comprobando posiciones seed: " + e.getMessage()); }

        try (java.sql.Connection c = com.trackver.db.ConexionSQLite.conectarVehiculos();
             java.sql.Statement st = c.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(1) AS c FROM vehiculos")) {
            if (!rs.next() || rs.getInt("c") == 0) {
                SeedDB.insertarVehiculos();
            }
        } catch (Exception e) { System.err.println("Error comprobando vehiculos seed: " + e.getMessage()); }

        try (java.sql.Connection c = com.trackver.db.ConexionSQLite.conectarAlertas();
             java.sql.Statement st = c.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(1) AS c FROM alertas")) {
            if (!rs.next() || rs.getInt("c") == 0) {
                SeedDB.insertarAlertas();
            }
        } catch (Exception e) { System.err.println("Error comprobando alertas seed: " + e.getMessage()); }
        // Limpieza inicial: desactivar límites de velocidad duplicados dejando el más reciente activo por vehículo
        try (java.sql.Connection conn = com.trackver.db.ConexionSQLite.conectarAlertas();
             java.sql.Statement st = conn.createStatement()) {
            String cleanup = "UPDATE velocidades_asignadas SET activo=0 WHERE id NOT IN (SELECT MAX(id) FROM velocidades_asignadas WHERE activo=1 GROUP BY vehiculo_id)";
            st.executeUpdate(cleanup);
        } catch (Exception e) {
            System.err.println("Error limpiando duplicados de velocidades: " + e.getMessage());
        }
        // Insertar datos aleatorios adicionales para demo (vehículos y posiciones)
        // Desactivado: evitar que se inserten posiciones nuevas en cada reinicio.
        // Si necesitas regenerar datos de demo, descomenta la siguiente línea.
        // SeedDB.insertarDatosAleatorios();

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
                // Guardar información mínima en la sesión para verificaciones server-side
                req.session().attribute("usuarioId", u.id);
                req.session().attribute("usuarioNombre", u.nombre == null ? "" : u.nombre);
                req.session().attribute("nivelAcceso", u.nivelAcceso);
                return String.format("{\"ok\":true,\"id\":%d,\"nombre\":\"%s\",\"nivelAcceso\":%d}",
                        u.id, u.nombre.replace("\"", "\\\""), u.nivelAcceso);
            } else {
                res.status(401);
                return "{\"ok\":false}";
            }
        });

        // Protegemos rutas de administración: sólo administradores pueden acceder
        before("/api/usuarios", (req, res) -> requireAdmin(req));
        before("/api/usuarios/*", (req, res) -> requireAdmin(req));
        // Protegemos la página estática si se sirve desde la carpeta estática (ruta relativa)
        before("/10_HTML/admin_users.html", (req, res) -> requireAdmin(req));
        before("/admin_users.html", (req, res) -> requireAdmin(req));

        // API: listar usuarios
        get("/api/usuarios", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            java.util.List<UsuarioDTO> lista = UsuarioDAO.listarUsuarios();
            StringBuilder sb = new StringBuilder(); sb.append('[');
            boolean first = true;
            for (UsuarioDTO u : lista) {
                if (!first) sb.append(','); first = false;
                sb.append(String.format("{\"id\":%d,\"nombre\":\"%s\",\"correo\":\"%s\",\"nivelAcceso\":%d}", u.id, u.nombre == null ? "" : u.nombre.replace("\"","\\\""), u.correo == null ? "" : u.correo.replace("\"","\\\""), u.nivelAcceso));
            }
            sb.append(']');
            return sb.toString();
        });

        // API: crear usuario (POST)
        post("/api/usuarios", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String nombre = req.queryParams("nombre");
            String correo = req.queryParams("correo");
            String contrasena = req.queryParams("contrasena");
            String nivelS = req.queryParams("nivelAcceso");
            if (nombre == null || correo == null || contrasena == null || nivelS == null) {
                res.status(400);
                return "{\"ok\":false,\"message\":\"Faltan campos\"}";
            }
            int nivel = Integer.parseInt(nivelS);
            boolean ok = UsuarioDAO.crearUsuario(nombre, correo, contrasena, nivel);
            return String.format("{\"ok\":%b}", ok);
        });

        // API: actualizar usuario (POST)
        post("/api/usuarios/update", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String idS = req.queryParams("id");
            String nombre = req.queryParams("nombre");
            String correo = req.queryParams("correo");
            String nivelS = req.queryParams("nivelAcceso");
            String nuevaPass = req.queryParams("nuevaContrasena");
            if (idS == null || nombre == null || correo == null || nivelS == null) {
                res.status(400);
                return "{\"ok\":false,\"message\":\"Faltan campos\"}";
            }
            int id = Integer.parseInt(idS);
            int nivel = Integer.parseInt(nivelS);
            boolean ok = UsuarioDAO.actualizarUsuario(id, nombre, correo, nivel);
            if (ok && nuevaPass != null && !nuevaPass.isEmpty()) {
                UsuarioDAO.actualizarContrasena(id, nuevaPass);
            }
            return String.format("{\"ok\":%b}", ok);
        });

        // API: eliminar usuario (POST)
        post("/api/usuarios/delete", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String idS = req.queryParams("id");
            if (idS == null) { res.status(400); return "{\"ok\":false,\"message\":\"id requerido\"}"; }
            int id = Integer.parseInt(idS);
            boolean ok = UsuarioDAO.eliminarUsuario(id);
            return String.format("{\"ok\":%b}", ok);
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
            // Soportar también JSON en el body: {"vehiculoId":9, "vel_max_kmh":50}
            if ((vidS == null || velS == null) && req.body() != null && !req.body().trim().isEmpty()) {
                String body = req.body();
                try {
                    Pattern pVid = Pattern.compile("\\\"vehiculoId\\\"\\s*:\\s*\\\"?(\\d+)\\\"?");
                    Matcher mVid = pVid.matcher(body);
                    if (mVid.find()) vidS = mVid.group(1);
                    Pattern pVid2 = Pattern.compile("\\\"vehiculo_id\\\"\\s*:\\s*\\\"?(\\d+)\\\"?");
                    Matcher mVid2 = pVid2.matcher(body);
                    if (vidS == null && mVid2.find()) vidS = mVid2.group(1);
                    Pattern pVel = Pattern.compile("\\\"vel_max_kmh\\\"\\s*:\\s*\\\"?([0-9]+(?:\\.[0-9]+)?)\\\"?");
                    Matcher mVel = pVel.matcher(body);
                    if (mVel.find()) velS = mVel.group(1);
                    Pattern pVel2 = Pattern.compile("\\\"velMaxKmh\\\"\\s*:\\s*\\\"?([0-9]+(?:\\.[0-9]+)?)\\\"?");
                    Matcher mVel2 = pVel2.matcher(body);
                    if (velS == null && mVel2.find()) velS = mVel2.group(1);
                } catch (Exception ex) {
                    // ignore parsing errors; validation below will catch missing params
                }
            }
            if (vidS == null || velS == null) {
                res.status(400);
                return "{\"ok\":false,\"message\":\"Faltan parámetros\"}";
            }
            int vid = Integer.parseInt(vidS);
            double vel = Double.parseDouble(velS);
            try (java.sql.Connection conn = com.trackver.db.ConexionSQLite.conectarAlertas()) {
                // comprobar si ya existe un límite activo para este vehículo
                try (java.sql.PreparedStatement psCheck = conn.prepareStatement("SELECT COUNT(*) as c FROM velocidades_asignadas WHERE vehiculo_id = ? AND activo = 1")) {
                    psCheck.setInt(1, vid);
                    try (java.sql.ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next() && rs.getInt("c") > 0) {
                            res.status(409);
                            return "{\"ok\":false,\"message\":\"Ya existe un límite activo para ese vehículo\"}";
                        }
                    }
                }

                try (java.sql.PreparedStatement ps = conn.prepareStatement("INSERT INTO velocidades_asignadas (vehiculo_id, vel_max_kmh, activo) VALUES (?, ?, 1)", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, vid);
                    ps.setDouble(2, vel);
                    ps.executeUpdate();
                    try (java.sql.ResultSet rk = ps.getGeneratedKeys()) {
                        if (rk.next()) {
                            int id = rk.getInt(1);
                            return String.format("{\"ok\":true,\"id\":%d}", id);
                        }
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
                        int correoEnviado = 0;
                        int smsEnviado = 0;
                        try { correoEnviado = rs.getInt("correo_enviado"); } catch (Exception ex) { }
                        try { smsEnviado = rs.getInt("sms_enviado"); } catch (Exception ex) { }
                        sb.append(String.format("{\"id\":%d,\"vehiculo_id\":%d,\"tipo\":\"%s\",\"descripcion\":\"%s\",\"fecha\":\"%s\",\"estado\":\"%s\",\"correo_enviado\":%d,\"sms_enviado\":%d}", id, vid, tipo == null ? "" : tipo.replace("\"","\\\""), desc == null ? "" : desc.replace("\"","\\\""), fecha == null ? "" : fecha.replace("\"","\\\""), estado == null ? "" : estado.replace("\"","\\\""), correoEnviado, smsEnviado));
                    }
                    sb.append(']');
                    return sb.toString();
                }
            } catch (Exception ex) {
                res.status(500);
                return String.format("{\"ok\":false,\"message\":\"%s\"}", ex.getMessage().replace("\"","\\\""));
            }
        });

        // API: endpoint de prueba para crear una alerta (solo pruebas)
        // Uso: GET /api/alertas/test?vehiculoId=123&tipo=TEST&descripcion=Prueba
        get("/api/alertas/test", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String vidS = req.queryParams("vehiculoId");
            if (vidS == null || vidS.isEmpty()) { res.status(400); return "{\"ok\":false,\"message\":\"vehiculoId requerido\"}"; }
            String tipo = req.queryParams("tipo"); if (tipo == null) tipo = "TEST";
            String desc = req.queryParams("descripcion"); if (desc == null) desc = "Alerta de prueba";
            try {
                int vid = Integer.parseInt(vidS);
                boolean ok = com.trackver.db.AlertaDAO.crearAlerta(vid, tipo, desc, "TRIGGERED");
                if (ok) return "{\"ok\":true}";
                res.status(500); return "{\"ok\":false,\"message\":\"no se pudo crear alerta\"}";
            } catch (Exception ex) {
                res.status(500); return String.format("{\"ok\":false,\"message\":\"%s\"}", ex.getMessage().replace("\"","\\\""));
            }
        });

        // API: listar geocercas (opcional usuarioId)
        get("/api/geocercas", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String uidS = req.queryParams("usuarioId");
            try (java.sql.Connection conn = com.trackver.db.ConexionSQLite.conectarAlertas();
                 java.sql.PreparedStatement ps = conn.prepareStatement(uidS == null ? "SELECT * FROM geocercas WHERE activo=1" : "SELECT * FROM geocercas WHERE activo=1 AND (usuario_id = ?)") ) {
                if (uidS != null) ps.setInt(1, Integer.parseInt(uidS));
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    StringBuilder sb = new StringBuilder(); sb.append('[');
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) sb.append(','); first = false;
                        int id = rs.getInt("id");
                        String nombre = rs.getString("nombre");
                        double lat = rs.getDouble("latitud");
                        double lon = rs.getDouble("longitud");
                        double radio = rs.getDouble("radio_m");
                        int usuarioId = rs.getInt("usuario_id");
                        sb.append(String.format("{\"id\":%d,\"nombre\":\"%s\",\"latitud\":%f,\"longitud\":%f,\"radio_m\":%f,\"usuario_id\":%d}", id, nombre==null?"":nombre.replace("\"","\\\""), lat, lon, radio, usuarioId));
                    }
                    sb.append(']');
                    return sb.toString();
                }
            } catch (Exception ex) {
                res.status(500);
                return String.format("{\"ok\":false,\"message\":\"%s\"}", ex.getMessage().replace("\"","\\\""));
            }
        });

        // API: marcar correo/sms enviado o actualizar flags (POST)
        post("/api/alertas/mark", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String idS = req.queryParams("id");
            if (idS == null || idS.isEmpty()) { res.status(400); return "{\"ok\":false,\"message\":\"id requerido\"}"; }
            String correoS = req.queryParams("correo");
            String smsS = req.queryParams("sms");
            try (java.sql.Connection conn = com.trackver.db.ConexionSQLite.conectarAlertas()) {
                int totalUpdated = 0;
                int id = Integer.parseInt(idS);
                if (correoS != null) {
                    try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE alertas SET correo_enviado = ? WHERE id = ?")) {
                        ps.setInt(1, Integer.parseInt(correoS));
                        ps.setInt(2, id);
                        totalUpdated += ps.executeUpdate();
                    }
                }
                if (smsS != null) {
                    try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE alertas SET sms_enviado = ? WHERE id = ?")) {
                        ps.setInt(1, Integer.parseInt(smsS));
                        ps.setInt(2, id);
                        totalUpdated += ps.executeUpdate();
                    }
                }
                // If neither param provided, return bad request
                if (correoS == null && smsS == null) { res.status(400); return "{\"ok\":false,\"message\":\"correo o sms requerido\"}"; }
                return String.format("{\"ok\":%b}", totalUpdated>0);
            } catch (Exception ex) { res.status(500); return String.format("{\"ok\":false,\"message\":\"%s\"}", ex.getMessage().replace("\"","\\\"")); }
        });

        // API: descartar/eliminar alerta (POST)
        post("/api/alertas/dismiss", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String idS = req.queryParams("id");
            if (idS == null || idS.isEmpty()) { res.status(400); return "{\"ok\":false,\"message\":\"id requerido\"}"; }
            try {
                boolean ok = com.trackver.db.AlertaDAO.eliminarAlerta(Integer.parseInt(idS));
                return String.format("{\"ok\":%b}", ok);
            } catch (Exception ex) { res.status(500); return String.format("{\"ok\":false,\"message\":\"%s\"}", ex.getMessage().replace("\"","\\\"")); }
        });

        // API: listar asignaciones de geocercas a vehículos
        get("/api/geocercas/asignaciones", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            try (java.sql.Connection conn = com.trackver.db.ConexionSQLite.conectarAlertas();
                 java.sql.PreparedStatement ps = conn.prepareStatement("SELECT geocerca_id, vehiculo_id FROM geocerca_asignaciones")) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    StringBuilder sb = new StringBuilder(); sb.append('[');
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) sb.append(','); first = false;
                        int gid = rs.getInt("geocerca_id");
                        int vid = rs.getInt("vehiculo_id");
                        sb.append(String.format("{\"geocerca_id\":%d,\"vehiculo_id\":%d}", gid, vid));
                    }
                    sb.append(']');
                    return sb.toString();
                }
            } catch (Exception ex) {
                res.status(500);
                return String.format("{\"ok\":false,\"message\":\"%s\"}", ex.getMessage().replace("\"","\\\""));
            }
        });

        // API: listar límites de velocidad asignados (opcional vehiculoId)
        get("/api/velocidades", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String vidS = req.queryParams("vehiculoId");
            try (java.sql.Connection conn = com.trackver.db.ConexionSQLite.conectarAlertas();
                 java.sql.PreparedStatement ps = conn.prepareStatement(vidS == null ? "SELECT * FROM velocidades_asignadas WHERE activo=1" : "SELECT * FROM velocidades_asignadas WHERE activo=1 AND vehiculo_id = ?")) {
                if (vidS != null) ps.setInt(1, Integer.parseInt(vidS));
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    StringBuilder sb = new StringBuilder(); sb.append('[');
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) sb.append(','); first = false;
                        int id = rs.getInt("id");
                        int vid = rs.getInt("vehiculo_id");
                        double vel = rs.getDouble("vel_max_kmh");
                        sb.append(String.format("{\"id\":%d,\"vehiculo_id\":%d,\"vel_max_kmh\":%f}", id, vid, vel));
                    }
                    sb.append(']');
                    return sb.toString();
                }
            } catch (Exception ex) {
                res.status(500);
                return String.format("{\"ok\":false,\"message\":\"%s\"}", ex.getMessage().replace("\"","\\\""));
            }
        });

        // API: eliminar/desactivar límite de velocidad (POST) - params: id + usuarioId + password
        post("/api/velocidades/delete", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String idS = req.queryParams("id");
            String uidS = req.queryParams("usuarioId");
            String pass = req.queryParams("password");
            if ((idS == null || idS.isEmpty()) && req.body() != null && !req.body().trim().isEmpty()) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\\"id\\\"\\s*:\\s*\\\"?(\\d+)\\\"?").matcher(req.body());
                if (m.find()) idS = m.group(1);
            }
            if (idS == null || idS.isEmpty() || uidS == null || uidS.isEmpty() || pass == null) {
                res.status(400);
                return "{\"ok\":false,\"message\":\"Faltan parámetros\"}";
            }
            int id = Integer.parseInt(idS);
            int uid = Integer.parseInt(uidS);
            // verificar contraseña
            boolean okAuth = com.trackver.db.UsuarioDAO.verificarPasswordPorId(uid, pass);
            if (!okAuth) { res.status(401); return "{\"ok\":false,\"message\":\"Contraseña incorrecta\"}"; }
            try (java.sql.Connection conn = com.trackver.db.ConexionSQLite.conectarAlertas()) {
                // obtener vehiculo asociado a la asignación
                Integer vehId = null;
                try (java.sql.PreparedStatement psGet = conn.prepareStatement("SELECT vehiculo_id FROM velocidades_asignadas WHERE id = ?")) {
                    psGet.setInt(1, id);
                    try (java.sql.ResultSet rs = psGet.executeQuery()) {
                        if (rs.next()) vehId = rs.getInt("vehiculo_id");
                        else { res.status(404); return "{\"ok\":false,\"message\":\"No encontrado\"}"; }
                    }
                }
                // verificar que el vehículo pertenece al usuario
                com.trackver.db.VehiculoDAO.VehiculoDTO v = com.trackver.db.VehiculoDAO.obtenerPorId(vehId);
                if (v == null) { res.status(404); return "{\"ok\":false,\"message\":\"Vehículo no encontrado\"}"; }
                if (v.usuarioId == null || v.usuarioId.intValue() != uid) { res.status(403); return "{\"ok\":false,\"message\":\"No autorizado\"}"; }

                try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE velocidades_asignadas SET activo=0 WHERE id = ?")) {
                    ps.setInt(1, id);
                    int c = ps.executeUpdate();
                    if (c > 0) return "{\"ok\":true}";
                    res.status(404);
                    return "{\"ok\":false,\"message\":\"No encontrado\"}";
                }
            } catch (Exception ex) {
                res.status(500);
                return String.format("{\"ok\":false,\"message\":\"%s\"}", ex.getMessage().replace("\"","\\\""));
            }
        });

        // API: eliminar/desactivar geocerca (POST) - params: id + usuarioId + password
        post("/api/geocercas/delete", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            String idS = req.queryParams("id");
            String uidS = req.queryParams("usuarioId");
            String pass = req.queryParams("password");
            if ((idS == null || idS.isEmpty()) && req.body() != null && !req.body().trim().isEmpty()) {
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\\"id\\\"\\s*:\\s*\\\"?(\\d+)\\\"?").matcher(req.body());
                if (m.find()) idS = m.group(1);
            }
            if (idS == null || idS.isEmpty() || uidS == null || uidS.isEmpty() || pass == null) {
                res.status(400);
                return "{\"ok\":false,\"message\":\"Faltan parámetros\"}";
            }
            int id = Integer.parseInt(idS);
            int uid = Integer.parseInt(uidS);
            // verificar contraseña
            boolean okAuth = com.trackver.db.UsuarioDAO.verificarPasswordPorId(uid, pass);
            if (!okAuth) { res.status(401); return "{\"ok\":false,\"message\":\"Contraseña incorrecta\"}"; }
            try (java.sql.Connection conn = com.trackver.db.ConexionSQLite.conectarAlertas()) {
                // comprobar propietario si existe
                Integer ownerId = null;
                try (java.sql.PreparedStatement psGet = conn.prepareStatement("SELECT usuario_id FROM geocercas WHERE id = ?")) {
                    psGet.setInt(1, id);
                    try (java.sql.ResultSet rs = psGet.executeQuery()) {
                        if (rs.next()) {
                            ownerId = rs.getObject("usuario_id") == null ? null : rs.getInt("usuario_id");
                        } else { res.status(404); return "{\"ok\":false,\"message\":\"No encontrado\"}"; }
                    }
                }
                if (ownerId != null && ownerId.intValue() != uid) { res.status(403); return "{\"ok\":false,\"message\":\"No autorizado\"}"; }
                try (java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE geocercas SET activo=0 WHERE id = ?")) {
                    ps.setInt(1, id);
                    int c = ps.executeUpdate();
                    if (c > 0) return "{\"ok\":true}";
                    res.status(404);
                    return "{\"ok\":false,\"message\":\"No encontrado\"}";
                }
            } catch (Exception ex) {
                res.status(500);
                return String.format("{\"ok\":false,\"message\":\"%s\"}", ex.getMessage().replace("\"","\\\""));
            }
        });

        System.out.println("Servidor web iniciado en http://localhost:4567/ (archivos estáticos: 20_FrontEnd)");
    }
}