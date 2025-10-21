package com.trackver.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa a un Administrador dentro del sistema.
 * Un administrador puede gestionar auditorías y validar su estado.
 */
public class Administrador extends Usuario {
    private String zonaAsignada;
    private List<Auditoria> auditorias;

    /**
     * Constructor de Administrador.
     *
     * @param id           Identificador único del administrador
     * @param nombre       Nombre del administrador
     * @param correo       Correo electrónico
     * @param contrasena   Contraseña de acceso
     * @param zonaAsignada Zona asignada al administrador
     * @param nivelAcceso  Nivel de acceso del administrador
     */
    public Administrador(int id, String nombre, String correo, String contrasena, String zonaAsignada, int nivelAcceso) {
        super(id, nombre, correo, contrasena, nivelAcceso);
        this.zonaAsignada = zonaAsignada;
        this.auditorias = new ArrayList<>();
    }

    /**
     * Valida una auditoría cambiando su estado.
     *
     * @param a            Auditoría a validar
     * @param nuevoEstado  Nuevo estado de la auditoría
     */
    public void validaAuditoria(Auditoria a, String nuevoEstado) {
        a.actualizarEstado(nuevoEstado);
    }

    /**
     * Consulta todas las auditorías asignadas al administrador.
     *
     * @return Lista de auditorías
     */
    public List<Auditoria> consultarAuditorias() {
        return auditorias;
    }

    /**
     * Agrega una nueva auditoría a la lista.
     *
     * @param a Auditoría a agregar
     */
    public void agregarAuditoria(Auditoria a) {
        auditorias.add(a);
    }

    // Getter opcional para zonaAsignada
    public String getZonaAsignada() {
        return zonaAsignada;
    }
}