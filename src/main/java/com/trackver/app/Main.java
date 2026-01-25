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
                return String.format("{\"id\":%d,\"latitud\":%f,\"longitud\":%f,\"fechaHora\":\"%s\",\"usuarioId\":%d,\"vehiculoId\":%d,\"vehiculoPlacas\":\"%s\",\"vehiculoMarca\":\"%s\"}",
                        p.id, p.latitud, p.longitud, p.fechaHora.replace("\"", "\\\""), p.usuarioId, p.vehiculoId, placas, marca);
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
                sb.append(String.format("{\"id\":%d,\"latitud\":%f,\"longitud\":%f,\"fechaHora\":\"%s\",\"usuarioId\":%d,\"vehiculoId\":%d}",
                    p.id, p.latitud, p.longitud, p.fechaHora.replace("\"", "\\\""), p.usuarioId, p.vehiculoId));
            }
            sb.append(']');
            return sb.toString();
        });

        // Ruta simple para verificar servidor
        get("/api/ping", (req, res) -> "pong");

        // Redirigir raíz al HTML de login
        get("/", (req, res) -> {
            res.redirect("/10_HTML/Index.html");
            return null;
        });

        System.out.println("Servidor web iniciado en http://localhost:4567/ (archivos estáticos: 20_FrontEnd)");
    }
}