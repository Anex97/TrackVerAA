package com.trackver.app;

import com.trackver.auth.SistemaAutenticacion;
import com.trackver.model.*;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        SistemaAutenticacion auth = new SistemaAutenticacion();

        // Creamos usuarios de prueba
        Administrador admin = new Administrador(1, "Admin", "admin@t.com", "pass", "Zona A", 5);
        Auditor auditor = new Auditor(2, "Juan", "juan@t.com", "1234");

        auth.registrarUsuario(admin);
        auth.registrarUsuario(auditor);

        boolean salir = false;
        boolean loginExitoso = false;
        Usuario usuarioActivo = null;

        while (!salir) {
            System.out.println("\n=== MENÚ PRINCIPAL ===");
            System.out.println("1. Iniciar sesión");
            System.out.println("2. Crear auditoría (requiere login)");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opción: ");
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1":
                    System.out.print("Ingrese ID de usuario: ");
                    int id = Integer.parseInt(sc.nextLine());
                    System.out.print("Ingrese correo: ");
                    String correo = sc.nextLine();
                    System.out.print("Ingrese contraseña: ");
                    String contrasena = sc.nextLine();

                    if (auth.validarCredenciales(id, correo, contrasena)) {
                        loginExitoso = true;
                        usuarioActivo = (id == admin.getId()) ? admin : auditor;
                        System.out.println("Inicio de sesión exitoso. Bienvenido " + usuarioActivo.getNombre());
                    } else {
                        System.out.println("Credenciales incorrectas o usuario bloqueado.");
                    }
                    break;

                case "2":
                    if (!loginExitoso || usuarioActivo == null) {
                        System.out.println("Debe iniciar sesión primero.");
                        break;
                    }
                    System.out.print("Ingrese ID de auditoría: ");
                    int idAuditoria = Integer.parseInt(sc.nextLine());
                    System.out.print("Ingrese datos capturados: ");
                    String datos = sc.nextLine();

                    Auditoria a1 = new Auditoria(idAuditoria, LocalDate.now(), datos);
                    if (usuarioActivo instanceof Administrador) {
                        ((Administrador) usuarioActivo).agregarAuditoria(a1);
                    }
                    ReportePreliminar r = a1.generarReporte();
                    r.visualizar();
                    break;

                case "3":
                    salir = true;
                    System.out.println("Saliendo del sistema...");
                    break;

                default:
                    System.out.println("Opción no válida, intente de nuevo.");
            }
        }

        sc.close();
    }
}