package com.trackver.auth;

import com.trackver.model.Usuario;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que gestiona la autenticación de usuarios.
 * Permite registrar, validar credenciales y bloquear usuarios tras múltiples intentos fallidos.
 */
public class SistemaAutenticacion {
    private Map<Integer, Integer> intentosFallidos = new HashMap<>();
    private Map<Integer, Usuario> usuarios = new HashMap<>();
    private final int MAX_INTENTOS = 3;

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param u Usuario a registrar
     */
    public void registrarUsuario(Usuario u) {
        usuarios.put(u.getId(), u);
        intentosFallidos.put(u.getId(), 0);
    }

    /**
     * Valida las credenciales de un usuario.
     *
     * @param idUsuario   Identificador del usuario
     * @param correo      Correo electrónico ingresado
     * @param contrasena  Contraseña ingresada
     * @return true si las credenciales son correctas, false en caso contrario
     */
    public boolean validarCredenciales(int idUsuario, String correo, String contrasena) {
        Usuario u = usuarios.get(idUsuario);
        if (u == null) return false;

        if (u.iniciarSesion(correo, contrasena)) {
            intentosFallidos.put(idUsuario, 0); // reinicia intentos fallidos
            return true;
        } else {
            intentosFallidos.put(idUsuario, intentosFallidos.getOrDefault(idUsuario, 0) + 1);
            if (intentosFallidos.get(idUsuario) >= MAX_INTENTOS) {
                bloquearUsuario(idUsuario);
            }
            return false;
        }
    }

    /**
     * Bloquea a un usuario tras superar el número máximo de intentos fallidos.
     *
     * @param idUsuario Identificador del usuario a bloquear
     */
    public void bloquearUsuario(int idUsuario) {
        usuarios.remove(idUsuario);
        intentosFallidos.remove(idUsuario);
    }
}