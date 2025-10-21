package com.trackver.model;

/**
 * Clase base que representa a un usuario del sistema.
 */
public class Usuario {
    private int id;
    private String nombre;
    private String correo;
    private String contrasena;
    private int nivelAcceso;

    public Usuario(int id, String nombre, String correo, String contrasena, int nivelAcceso) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.nivelAcceso = nivelAcceso;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getContrasena() { return contrasena; }
    public int getNivelAcceso() { return nivelAcceso; }

    public void setNivelAcceso(int nivelAcceso) { this.nivelAcceso = nivelAcceso; }

    /** Valida credenciales de inicio de sesión */
    public boolean iniciarSesion(String correo, String contrasena) {
        return this.correo.equals(correo) && this.contrasena.equals(contrasena);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", correo='" + correo + '\'' +
                ", nivelAcceso=" + nivelAcceso +
                '}';
    }
}