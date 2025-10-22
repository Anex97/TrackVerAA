package com.trackver.ui;

import com.trackver.db.UsuarioDAO.UsuarioDTO;
import java.util.Scanner;

public class MenuAuditor {

    public static void mostrar(Scanner sc, UsuarioDTO usuario) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== MENÚ AUDITOR ===");
            System.out.println("1. Crear auditoría");
            System.out.println("2. Generar reporte preliminar");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1":
                    System.out.println("Creando auditoría...");
                    // Aquí conectarías con AuditoriaDAO.crearAuditoria()
                    break;
                case "2":
                    System.out.println("Generando reporte preliminar...");
                    // Aquí conectarías con AuditoriaDAO.generarReporte()
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