package com.trackver.model;

public class Auditor extends Usuario {
    public Auditor(int id, String nombre, String correo, String contrasena) {
        super(id, nombre, correo, contrasena, 1); // nivelAcceso fijo
    }
}