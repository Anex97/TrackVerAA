package com.trackver.model;

import java.time.LocalDate;

/**
 * Clase que representa un reporte preliminar generado a partir de una auditoría.
 */
public class ReportePreliminar {
    private int id;
    private LocalDate fecha;
    private String autor;
    private String resumen;

    // Constructor con parámetros
    public ReportePreliminar(int id, LocalDate fecha, String autor, String resumen) {
        this.id = id;
        this.fecha = fecha;
        this.autor = autor;
        this.resumen = resumen;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    @Override
    public String toString() {
        return "ReportePreliminar{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", autor='" + autor + '\'' +
                ", resumen='" + resumen + '\'' +
                '}';
    }
}