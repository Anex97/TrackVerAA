package com.trackver.ui;

import com.trackver.db.UsuarioDAO;
import com.trackver.db.UsuarioDAO.UsuarioDTO;
import java.util.Scanner;

public class MenuAdmin {

    public static void mostrar(Scanner sc, UsuarioDTO usuario) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== MENÚ ADMINISTRADOR ===");
            System.out.println("1. Validar auditoría");
            System.out.println("2. Listar auditorías");
            System.out.println("3. Gestión de usuarios");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1":
                    System.out.println("Validando auditoría...");
                    validarAuditoria(sc);
                    break;
                case "2":
                    System.out.println("Listando auditorías...");
                    listarAuditorias(sc);
                    break;
                case "3":
                    gestionUsuarios(sc);
                    break;
                case "0":
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private static void gestionUsuarios(Scanner sc) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n--- GESTIÓN DE USUARIOS ---");
            System.out.println("1. Crear usuario");
            System.out.println("2. Eliminar usuario");
            System.out.println("0. Volver al menú anterior");
            System.out.print("Seleccione una opción: ");
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1":
                    System.out.print("Nombre: ");
                    String nombre = sc.nextLine();
                    System.out.print("Correo: ");
                    String correo = sc.nextLine();
                    System.out.print("Contraseña: ");
                    String contrasena = sc.nextLine();
                    System.out.print("Nivel de acceso (0=Usuario, 1=Auditor, 2=Admin): ");
                    int nivel = Integer.parseInt(sc.nextLine());

                    if (UsuarioDAO.crearUsuario(nombre, correo, contrasena, nivel)) {
                        System.out.println("✅ Usuario creado con éxito.");
                    } else {
                        System.out.println("❌ No se pudo crear el usuario.");
                    }
                    break;

                case "2":
                    System.out.print("Correo del usuario a eliminar: ");
                    String correoEliminar = sc.nextLine();

                    if (UsuarioDAO.eliminarUsuario(correoEliminar)) {
                        System.out.println("✅ Usuario eliminado.");
                    } else {
                        System.out.println("❌ No se encontró el usuario.");
                    }
                    break;

                case "0":
                    salir = true;
                    break;

                default:
                    System.out.println("Opción inválida.");
            }
        }
    }
    private static void listarAuditorias(Scanner sc) {
        var auditorias = com.trackver.db.AuditoriaDAO.listarAuditorias();
        if (auditorias.isEmpty()) {
            System.out.println("No hay auditorías registradas.");
        } else {
            System.out.println("\n--- LISTADO DE AUDITORÍAS ---");
            for (var a : auditorias) {
                System.out.println("ID: " + a.id + " | " + a.titulo + " | Estado: " + a.estado + " | Fecha: " + a.fecha);
            }
        }
    }

    private static void validarAuditoria(Scanner sc) {
        listarAuditorias(sc);
        System.out.print("Ingrese el ID de la auditoría a validar: ");
        int id = Integer.parseInt(sc.nextLine());
        if (com.trackver.db.AuditoriaDAO.validarAuditoria(id)) {
            System.out.println("✅ Auditoría validada con éxito.");
        } else {
            System.out.println("❌ No se pudo validar la auditoría.");
        }
    }
}