package com.trackver.ui;

import com.trackver.db.UsuarioDAO.UsuarioDTO;
import java.util.Scanner;

public class MenuManager {
    public static void mostrarMenuPorRol(Scanner sc, UsuarioDTO usuario) {
        switch (usuario.nivelAcceso) {
            case 2:
                MenuAdmin.mostrar(sc, usuario);
                break;
            case 1:
                MenuAuditor.mostrar(sc, usuario);
                break;
            default:
                MenuUsuario.mostrar(sc, usuario);
        }
    }
}