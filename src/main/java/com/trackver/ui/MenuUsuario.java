package com.trackver.ui;

import com.trackver.db.AuditoriaDAO;
import com.trackver.db.UsuarioDAO.UsuarioDTO;
import java.util.Scanner;

public class MenuUsuario {

    public static void mostrar(Scanner sc, UsuarioDTO usuario) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== MENÚ USUARIO ===");
            System.out.println("1. Consultar auditorías validadas");
            System.out.println("2. Ver reportes (en construcción)");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1":
                    consultarAuditoriasValidadas();
                    break;
                case "2":
                    System.out.println("Funcionalidad de reportes en construcción...");
                    break;
                case "0":
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private static void consultarAuditoriasValidadas() {
        var auditorias = AuditoriaDAO.listarValidadas();
        if (auditorias.isEmpty()) {
            System.out.println("No hay auditorías validadas disponibles.");
        } else {
            System.out.println("\n--- AUDITORÍAS VALIDADAS ---");
            for (var a : auditorias) {
                System.out.println("ID: " + a.id + " | " + a.titulo +
                                   " | Fecha: " + a.fecha +
                                   " | Estado: " + a.estado);
            }
        }
    }
}