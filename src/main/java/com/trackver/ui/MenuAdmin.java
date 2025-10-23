package com.trackver.ui;

import com.trackver.db.UsuarioDAO;
import com.trackver.db.AuditoriaDAO;
import com.trackver.db.PosicionDAO;
import com.trackver.db.UsuarioDAO.UsuarioDTO;
import com.trackver.db.AuditoriaDAO.AuditoriaDTO;
import com.trackver.db.PosicionDAO.PosicionDTO;

import java.util.List;
import java.util.Scanner;

public class MenuAdmin {

    public static void mostrar(Scanner sc, UsuarioDTO admin) {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n=== MENÚ ADMINISTRADOR ===");
            System.out.println("1. Listar usuarios");
            System.out.println("2. Crear usuario");
            System.out.println("3. Eliminar usuario");
            System.out.println("4. Listar auditorías");
            System.out.println("5. Validar auditoría");
            System.out.println("6. Listar todas las posiciones GPS");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");
            String opcion = sc.nextLine();

            switch (opcion) {
                case "1":
                    listarUsuarios();
                    break;
                case "2":
                    crearUsuario(sc);
                    break;
                case "3":
                    eliminarUsuario(sc);
                    break;
                case "4":
                    listarAuditorias();
                    break;
                case "5":
                    validarAuditoria(sc);
                    break;
                case "6":
                    listarTodasLasPosiciones();
                    break;
                case "0":
                    salir = true;
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    // =========================
    // USUARIOS
    // =========================
    private static void listarUsuarios() {
        List<UsuarioDTO> usuarios = UsuarioDAO.listarUsuarios();
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios registrados.");
        } else {
            System.out.println("\n--- LISTADO DE USUARIOS ---");
            usuarios.forEach(u -> System.out.println(
                "ID: " + u.id + " | Nombre: " + u.nombre + " | Correo: " + u.correo + " | Nivel: " + u.nivelAcceso
            ));
        }
    }

    private static void crearUsuario(Scanner sc) {
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("Correo: ");
        String correo = sc.nextLine();
        System.out.print("Contraseña: ");
        String contrasena = sc.nextLine();
        System.out.print("Nivel de acceso (0=Usuario, 1=Auditor, 2=Admin): ");
        int nivel = Integer.parseInt(sc.nextLine());

        if (UsuarioDAO.crearUsuario(nombre, correo, contrasena, nivel)) {
            System.out.println("Usuario creado con éxito.");
        } else {
            System.out.println("No se pudo crear el usuario.");
        }
    }

    private static void eliminarUsuario(Scanner sc) {
        listarUsuarios();
        System.out.print("Ingrese el ID del usuario a eliminar: ");
        int id = Integer.parseInt(sc.nextLine());
        if (UsuarioDAO.eliminarUsuario(id)) {
            System.out.println("Usuario eliminado.");
        } else {
            System.out.println("No se pudo eliminar el usuario.");
        }
    }

    // =========================
    // AUDITORÍAS
    // =========================
    private static void listarAuditorias() {
        List<AuditoriaDTO> auditorias = AuditoriaDAO.listarAuditorias();
        if (auditorias.isEmpty()) {
            System.out.println("No hay auditorías registradas.");
        } else {
            System.out.println("\n--- LISTADO DE AUDITORÍAS ---");
            auditorias.forEach(a -> System.out.println(
                "ID: " + a.id + " | Título: " + a.titulo + " | Estado: " + a.estado + " | Fecha: " + a.fecha
            ));
        }
    }

    private static void validarAuditoria(Scanner sc) {
        listarAuditorias();
        System.out.print("Ingrese el ID de la auditoría a validar: ");
        int id = Integer.parseInt(sc.nextLine());
        if (AuditoriaDAO.validarAuditoria(id)) {
            System.out.println("Auditoría validada con éxito.");
        } else {
            System.out.println("No se pudo validar la auditoría.");
        }
    }

    // =========================
    // POSICIONES GPS
    // =========================
    private static void listarTodasLasPosiciones() {
        List<PosicionDTO> posiciones = PosicionDAO.listarTodasLasPosiciones();
        if (posiciones.isEmpty()) {
            System.out.println("No hay posiciones registradas.");
        } else {
            System.out.println("\n--- LISTADO DE POSICIONES GPS ---");
            posiciones.forEach(p -> System.out.println(
                "ID: " + p.id + " | Usuario: " + p.usuarioId +
                " | Lat: " + p.latitud + " | Lon: " + p.longitud +
                " | Fecha: " + p.fechaHora
            ));
        }
    }
}