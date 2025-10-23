package com.trackver.ui;

import com.trackver.db.AuditoriaDAO;
import com.trackver.db.AuditoriaDAO.AuditoriaDTO;
import com.trackver.db.UsuarioDAO.UsuarioDTO;

import java.util.List;
import java.util.Scanner;

public class MenuAuditor {

    public static void mostrar(Scanner sc, UsuarioDTO auditor) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== MENÚ AUDITOR ===");
            System.out.println("1. Crear auditoría");
            System.out.println("2. Listar todas las auditorías");
            System.out.println("3. Listar auditorías validadas");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1":
                    crearAuditoria(sc, auditor.id);
                    break;
                case "2":
                    listarAuditorias();
                    break;
                case "3":
                    listarValidadas();
                    break;
                case "0":
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private static void crearAuditoria(Scanner sc, int usuarioId) {
        System.out.print("Título: ");
        String titulo = sc.nextLine();
        System.out.print("Descripción: ");
        String descripcion = sc.nextLine();
        if (AuditoriaDAO.crearAuditoria(titulo, descripcion, usuarioId)) {
            System.out.println("Auditoría creada.");
        } else {
            System.out.println("No se pudo crear la auditoría.");
        }
    }

    private static void listarAuditorias() {
        List<AuditoriaDTO> auditorias = AuditoriaDAO.listarAuditorias();
        if (auditorias.isEmpty()) {
            System.out.println("No hay auditorías registradas.");
        } else {
            auditorias.forEach(a -> System.out.println(
                "ID: " + a.id + " | Título: " + a.titulo + " | Estado: " + a.estado + " | Fecha: " + a.fecha
            ));
        }
    }

    private static void listarValidadas() {
        List<AuditoriaDTO> auditorias = AuditoriaDAO.listarValidadas();
        if (auditorias.isEmpty()) {
            System.out.println("No hay auditorías validadas.");
        } else {
            auditorias.forEach(a -> System.out.println(
                "ID: " + a.id + " | Título: " + a.titulo + " | Estado: " + a.estado + " | Fecha: " + a.fecha
            ));
        }
    }
}