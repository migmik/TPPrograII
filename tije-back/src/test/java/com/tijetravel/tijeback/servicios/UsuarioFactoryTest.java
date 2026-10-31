package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tijetravel.tijeback.DnisPrueba;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.Administrador;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.servicios.usuarios.CreadorAdministrador;
import com.tijetravel.tijeback.servicios.usuarios.CreadorCliente;
import com.tijetravel.tijeback.servicios.usuarios.CreadorUsuario;
import com.tijetravel.tijeback.servicios.usuarios.CreadorVendedor;
import com.tijetravel.tijeback.servicios.usuarios.UsuarioFactory;

class UsuarioFactoryTest {

    /** Creador falso: sirve para comprobar que la fabrica solo depende de la interfaz. */
    private static class CreadorDePrueba implements CreadorUsuario {
        private final RolUsuario rol;

        CreadorDePrueba(RolUsuario rol) {
            this.rol = rol;
        }

        @Override
        public RolUsuario rol() {
            return rol;
        }

        @Override
        public Usuario crear(String nombre, String hash, Turista turista, String dni) {
            return new Administrador(nombre + "-delegado", hash, DnisPrueba.siguiente());
        }
    }

    @Test
    void aceptaImplementacionesPorContratoSinConocerSusClases() {
        List<CreadorUsuario> creadores = new ArrayList<>();
        for (RolUsuario rol : RolUsuario.values()) {
            creadores.add(new CreadorDePrueba(rol));
        }
        UsuarioFactory factory = new UsuarioFactory(creadores);
        for (RolUsuario rol : RolUsuario.values()) {
            assertEquals("usuario-delegado", factory.crear("usuario", "hash", rol, null, "12345678").getNombreUsuario());
        }
    }

    @Test
    void detectaRolesFaltantesYDuplicadosAlConstruirse() {
        try {
            new UsuarioFactory(new ArrayList<CreadorUsuario>());
            fail("Se esperaba IllegalStateException");
        } catch (IllegalStateException esperada) {
            // Es la excepcion que se esperaba.
        }
        List<CreadorUsuario> duplicados = new ArrayList<>();
        duplicados.add(new CreadorAdministrador());
        duplicados.add(new CreadorAdministrador());
        duplicados.add(new CreadorCliente());
        duplicados.add(new CreadorVendedor());
        try {
            new UsuarioFactory(duplicados);
            fail("Se esperaba IllegalStateException");
        } catch (IllegalStateException esperada) {
            // Es la excepcion que se esperaba.
        }
    }
}
