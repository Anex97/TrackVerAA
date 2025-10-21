package com.trackver.model;

import java.time.LocalDate;

public class Auditoria {
    private int id;
    private LocalDate fecha;
    private String descripcion;
    private String estado;

    // Constructor vacío (necesario para el binding de formularios en Spring)
    public Auditoria() {}

    // Constructor con parámetros
    public Auditoria(int id, LocalDate fecha, String descripcion) {
        this.id = id;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.estado = "Pendiente"; // valor inicial por defecto
    }

    // Método para actualizar el estado de la auditoría
    public void actualizarEstado(String nuevoEstado) {
        this.estado = nuevoEstado;
    }

    // Método para generar un reporte preliminar
    public ReportePreliminar generarReporte() {
        return new ReportePreliminar(
            id,
            fecha,
            "Sistema", // aquí luego puedes pasar el nombre del Administrador
            "Resumen provisional: " + descripcion
        );
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}