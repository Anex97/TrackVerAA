package com.trackver.model;

import java.time.LocalDate;

/**
 * Clase que representa una Auditoría dentro del sistema.
 * Contiene información básica, estado y la capacidad de generar un reporte preliminar.
 */
public class Auditoria {
    private int idAuditoria;
    private LocalDate fecha;
    private String datosCapturados;
    private String estado;
    private ReportePreliminar reporte;

    /**
     * Constructor de Auditoria.
     *
     * @param id     Identificador único de la auditoría
     * @param fecha  Fecha de creación de la auditoría
     * @param datos  Datos capturados durante la auditoría
     */
    public Auditoria(int id, LocalDate fecha, String datos) {
        this.idAuditoria = id;
        this.fecha = fecha;
        this.datosCapturados = datos;
        this.estado = "CREADA";
    }

    /**
     * Genera un reporte preliminar asociado a esta auditoría.
     * Si ya existe, devuelve el mismo reporte.
     *
     * @return ReportePreliminar generado
     */
    public ReportePreliminar generarReporte() {
        if (this.reporte == null) {
            String descripcion = "Resumen provisional: " + this.datosCapturados;
            String autor = "Sistema"; // o el nombre del usuario que la generó
            this.reporte = new ReportePreliminar(
                this.idAuditoria,
                LocalDate.now(),
                descripcion,
                autor
            );
        }
        return this.reporte;
    }

    /**
     * Actualiza el estado de la auditoría.
     *
     * @param nuevoEstado Nuevo estado a asignar
     */
    public void actualizarEstado(String nuevoEstado) {
        this.estado = nuevoEstado;
    }

    // Getters
    public int getIdAuditoria() { return idAuditoria; }
    public LocalDate getFecha() { return fecha; }
    public String getDatosCapturados() { return datosCapturados; }
    public String getEstado() { return estado; }
    public ReportePreliminar getReporte() { return reporte; }

    @Override
    public String toString() {
        return "Auditoria{" +
                "idAuditoria=" + idAuditoria +
                ", fecha=" + fecha +
                ", datosCapturados='" + datosCapturados + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}