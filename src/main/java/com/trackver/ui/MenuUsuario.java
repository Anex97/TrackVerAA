package com.trackver.ui;

import com.trackver.db.PosicionDAO;
import com.trackver.db.PosicionDAO.PosicionDTO;
import com.trackver.db.UsuarioDAO.UsuarioDTO;

import java.util.List;
import java.util.Scanner;

public class MenuUsuario {

    public static void mostrar(Scanner sc, UsuarioDTO usuario) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== MENÚ USUARIO ===");
            System.out.println("1. Registrar posición GPS");
            System.out.println("2. Listar mis posiciones");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1":
                    registrarPosicion(sc, usuario.id);
                    break;
                case "2":
                    listarMisPosiciones(usuario.id);
                    break;
                case "0":
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private static void registrarPosicion(Scanner sc, int usuarioId) {
        try {
            System.out.print("Latitud: ");
            double lat = Double.parseDouble(sc.nextLine());
            System.out.print("Longitud: ");
            double lon = Double.parseDouble(sc.nextLine());
            if (PosicionDAO.registrarPosicion(lat, lon, usuarioId)) {
                System.out.println("Posición registrada.");
            } else {
                System.out.println("No se pudo registrar la posición.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Coordenadas inválidas.");
        }
    }

    private static void listarMisPosiciones(int usuarioId) {
        List<PosicionDTO> posiciones = PosicionDAO.listarPosicionesPorUsuario(usuarioId);
        if (posiciones.isEmpty()) {
            System.out.println("No tienes posiciones registradas.");
        } else {
            posiciones.forEach(p -> System.out.println(
                "ID: " + p.id + " | Lat: " + p.latitud + " | Lon: " + p.longitud + " | Fecha: " + p.fechaHora
            ));
        }
    }
}