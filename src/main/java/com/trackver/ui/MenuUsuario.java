package com.trackver.ui;

import com.trackver.db.PosicionDAO;
import com.trackver.db.UsuarioDAO.UsuarioDTO;
import java.util.Scanner;

public class MenuUsuario {

    public static void mostrar(Scanner sc, UsuarioDTO usuario) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== MENÚ USUARIO ===");
            System.out.println("1. Registrar nueva posición GPS");
            System.out.println("2. Consultar mis posiciones");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1":
                    registrarPosicion(sc, usuario);
                    break;
                case "2":
                    consultarPosiciones(usuario);
                    break;
                case "0":
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private static void registrarPosicion(Scanner sc, UsuarioDTO usuario) {
        try {
            System.out.print("Ingrese latitud: ");
            double latitud = Double.parseDouble(sc.nextLine());
            System.out.print("Ingrese longitud: ");
            double longitud = Double.parseDouble(sc.nextLine());

            if (PosicionDAO.registrarPosicion(latitud, longitud, usuario.id)) {
                System.out.println("Posición registrada con éxito.");
            } else {
                System.out.println("No se pudo registrar la posición.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Coordenadas inválidas.");
        }
    }

    private static void consultarPosiciones(UsuarioDTO usuario) {
        var posiciones = PosicionDAO.listarPosicionesPorUsuario(usuario.id);
        if (posiciones.isEmpty()) {
            System.out.println("No tienes posiciones registradas.");
        } else {
            System.out.println("\n--- HISTORIAL DE POSICIONES ---");
            posiciones.forEach(System.out::println);
        }
    }
}