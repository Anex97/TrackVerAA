package com.trackver.app.controllers;

import com.trackver.model.Auditoria;
import com.trackver.model.ReportePreliminar;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class ReporteController {

    @GetMapping("/reporte")
    public String mostrarReporte(Model model) {
        // Crear una auditoría de ejemplo
        Auditoria auditoria = new Auditoria(101, LocalDate.now(), "Inspección de seguridad en planta");
        ReportePreliminar reporte = auditoria.generarReporte();

        // Pasar el reporte a la vista
        model.addAttribute("reporte", reporte);
        return "reporte"; // busca reporte.html en templates
    }
}