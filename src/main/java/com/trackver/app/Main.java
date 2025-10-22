package com.trackver.app;

import com.trackver.db.InitDB;
import com.trackver.db.SeedDB;
import com.trackver.db.UsuarioDAO;
import com.trackver.db.UsuarioDAO.UsuarioDTO;
import com.trackver.ui.MenuManager;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 1) Inicializar BD y usuarios iniciales
        InitDB.crearTablaUsuarios();
        InitDB.crearTablaAuditorias();
        InitDB.crearTablaPosiciones();

        // 2) Insertar datos semilla
        SeedDB.insertarUsuarios();
        SeedDB.insertarAuditorias();
        SeedDB.insertarPosiciones();

        // 3) Menú principal
        boolean salir = false;

        while (!salir) {
            System.out.println("\n=== MENÚ PRINCIPAL ===");
            System.out.println("1. Iniciar sesión");
            System.out.println("2. Salir");
            System.out.print("Seleccione una opción: ");
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1": {
                    // Login con SQLite
                    System.out.print("Ingrese correo: ");
                    String correo = sc.nextLine();
                    System.out.print("Ingrese contraseña: ");
                    String contrasena = sc.nextLine();

                    UsuarioDTO usuarioActivo = UsuarioDAO.buscarPorCorreoYPass(correo, contrasena);

                    if (usuarioActivo != null) {
                        System.out.println("Bienvenido " + usuarioActivo.nombre +
                                " | Rol: " + rolTexto(usuarioActivo.nivelAcceso));

                        // Delegar al MenuManager
                        MenuManager.mostrarMenuPorRol(sc, usuarioActivo);
                    } else {
                        System.out.println("Credenciales incorrectas.");
                    }
                    break;
                }
                case "2":
                    salir = true;
                    System.out.println("Saliendo del sistema...");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        }

        sc.close();
    }

    // Utilidad para mostrar el rol en texto
    private static String rolTexto(int nivelAcceso) {
        switch (nivelAcceso) {
            case 2: return "Administrador";
            case 1: return "Auditor";
            default: return "Usuario";
        }
    }
}