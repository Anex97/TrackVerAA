package com.trackver.ui;

import com.trackver.db.UsuarioDAO.UsuarioDTO;
import java.util.Scanner;

public class MenuUsuario {

    public static void mostrar(Scanner sc, UsuarioDTO usuario) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== MENÚ USUARIO ===");
            System.out.println("1. Consultar auditorías");
            System.out.println("2. Ver reportes");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1":
                    System.out.println("Consultando auditorías...");
                    // Aquí conectarías con AuditoriaDAO.consultarAuditorias()
                    break;
                case "2":
                    System.out.println("Mostrando reportes...");
                    // Aquí conectarías con AuditoriaDAO.verReportes()
                    break;
                case "0":
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }
}