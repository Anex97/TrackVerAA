package com.trackver.app.controllers;

import com.trackver.model.Auditoria;
import com.trackver.model.ReportePreliminar;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuditoriaController {

    // Mostrar el formulario
    @GetMapping("/auditorias/nueva")
    public String mostrarFormulario(Model model) {
        model.addAttribute("auditoria", new Auditoria());
        return "form-auditoria"; // busca form-auditoria.html
    }

    // Procesar el formulario
    @PostMapping("/auditorias/nueva")
    public String procesarFormulario(@ModelAttribute Auditoria auditoria, Model model) {
        // Generar reporte preliminar con los datos ingresados
        ReportePreliminar reporte = auditoria.generarReporte();
        model.addAttribute("reporte", reporte);
        return "reporte"; // reutilizamos reporte.html
    }
}