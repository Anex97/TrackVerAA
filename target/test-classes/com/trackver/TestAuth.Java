package com.trackver;

import com.trackver.auth.SistemaAutenticacion;
import com.trackver.model.Auditor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestAuth {
    @Test
    public void inicioSesionBloqueo() {
        SistemaAutenticacion s = new SistemaAutenticacion();
        Auditor a = new Auditor(10, "A", "a@x", "pw");
        s.registrarUsuario(a);
        assertFalse(s.validarCredenciales(10, "a@x", "wrong"));
        assertFalse(s.validarCredenciales(10, "a@x", "wrong"));
        assertFalse(s.validarCredenciales(10, "a@x", "wrong"));
        assertFalse(s.validarCredenciales(10, "a@x", "pw"));
    }
}