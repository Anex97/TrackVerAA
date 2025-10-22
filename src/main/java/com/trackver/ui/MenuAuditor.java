package com.trackver.ui;

import com.trackver.db.AuditoriaDAO;
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
                    crearAuditoria(sc, usuario);
                    break;
                case "2":
                    System.out.println("Generando reporte preliminar...");
                    generarReportePreliminar();
                    break;
                case "0":
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    // Método para crear una auditoría
    private static void crearAuditoria(Scanner sc, UsuarioDTO usuario) {
        System.out.print("Título de la auditoría: ");
        String titulo = sc.nextLine();
        System.out.print("Descripción: ");
        String descripcion = sc.nextLine();

        if (AuditoriaDAO.crearAuditoria(titulo, descripcion, usuario.id)) {
            System.out.println("✅ Auditoría creada con éxito.");
        } else {
            System.out.println("❌ No se pudo crear la auditoría.");
        }
    }

    //Metodo para generar reporte preliminar
    private static void generarReportePreliminar() {
        var auditorias = com.trackver.db.AuditoriaDAO.listarAuditorias();
        if (auditorias.isEmpty()) {
         System.out.println("No hay auditorías registradas.");
        }  
        else {
            System.out.println("\n--- REPORTE PRELIMINAR ---");
            for (var a : auditorias) {
            System.out.println("ID: " + a.id + " | " + a.titulo + " | Estado: " + a.estado + " | Fecha: " + a.fecha);
            }
        }
    }  
}