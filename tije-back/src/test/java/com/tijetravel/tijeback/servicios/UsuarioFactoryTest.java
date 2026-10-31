package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tijetravel.tijeback.DnisPrueba;
import com.tijetravel.tijeback.RolesPrueba;
import com.tijetravel.tijeback.enums.TipoUsuario;
import com.tijetravel.tijeback.modelos.Empleado;
import com.tijetravel.tijeback.modelos.Rol;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.servicios.usuarios.CreadorCliente;
import com.tijetravel.tijeback.servicios.usuarios.CreadorEmpleado;
import com.tijetravel.tijeback.servicios.usuarios.CreadorUsuario;
import com.tijetravel.tijeback.servicios.usuarios.UsuarioFactory;

class UsuarioFactoryTest {

    /** Creador falso: sirve para comprobar que la fabrica solo depende de la interfaz. */
    private static class CreadorDePrueba implements CreadorUsuario {
        private final TipoUsuario tipo;

        CreadorDePrueba(TipoUsuario tipo) {
            this.tipo = tipo;
        }

        @Override
        public TipoUsuario tipo() {
            return tipo;
        }

        @Override
        public Usuario crear(String nombre, String hash, Rol rol, Turista turista, String dni) {
            return new Empleado(nombre + "-delegado", hash, DnisPrueba.siguiente(), RolesPrueba.administrador());
        }
    }

    @Test
    void aceptaImplementacionesPorContratoSinConocerSusClases() {
        List<CreadorUsuario> creadores = new ArrayList<>();
        for (TipoUsuario tipo : TipoUsuario.values()) {
            creadores.add(new CreadorDePrueba(tipo));
        }
        UsuarioFactory factory = new UsuarioFactory(creadores);
        assertEquals("usuario-delegado",
                factory.crear("usuario", "hash", RolesPrueba.vendedor(), null, "12345678").getNombreUsuario());
        assertEquals("usuario-delegado",
                factory.crear("usuario", "hash", RolesPrueba.cliente(), null, null).getNombreUsuario());
    }

    @Test
    void eligeElCreadorSegunElTipoDelRol() {
        List<CreadorUsuario> creadores = new ArrayList<>();
        creadores.add(new CreadorEmpleado());
        creadores.add(new CreadorCliente());
        UsuarioFactory factory = new UsuarioFactory(creadores);

        Usuario hotelero = factory.crear("hotelero", "hash", RolesPrueba.vendedor(), null, DnisPrueba.siguiente());

        assertInstanceOf(Empleado.class, hotelero);
        assertEquals("Vendedor", hotelero.getRol().getNombre());
    }

    @Test
    void detectaTiposFaltantesYDuplicadosAlConstruirse() {
        try {
            new UsuarioFactory(new ArrayList<CreadorUsuario>());
            fail("Se esperaba IllegalStateException");
        } catch (IllegalStateException esperada) {
            // Es la excepcion que se esperaba.
        }
        List<CreadorUsuario> duplicados = new ArrayList<>();
        duplicados.add(new CreadorEmpleado());
        duplicados.add(new CreadorEmpleado());
        duplicados.add(new CreadorCliente());
        try {
            new UsuarioFactory(duplicados);
            fail("Se esperaba IllegalStateException");
        } catch (IllegalStateException esperada) {
            // Es la excepcion que se esperaba.
        }
    }
}
