package com.trackver.model;

import java.time.LocalDate;

public class ReportePreliminar {
    private int id;
    private LocalDate fecha;
    private String descripcion;
    private String autor;

    public ReportePreliminar(int id, LocalDate fecha, String descripcion, String autor) {
        this.id = id;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.autor = autor;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getAutor() {
        return autor;
    }

    // Método para mostrar el reporte en consola
    public void visualizar() {
        System.out.println("===== REPORTE PRELIMINAR =====");
        System.out.println("ID: " + id);
        System.out.println("Fecha: " + fecha);
        System.out.println("Autor: " + autor);
        System.out.println("Descripción: " + descripcion);
        System.out.println("==============================");
    }

    @Override
    public String toString() {
        return "ReportePreliminar{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", descripcion='" + descripcion + '\'' +
                ", autor='" + autor + '\'' +
                '}';
    }
}