package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.*;
import com.tijetravel.tijeback.servicios.usuarios.*;

class UsuarioFactoryTest {
    @Test
    void aceptaImplementacionesPorContratoSinConocerSusClases() {
        List<CreadorUsuario> creadores = Arrays.stream(RolUsuario.values())
                .map(rol -> new CreadorUsuario() {
                    public RolUsuario rol() {
                        return rol;
                    }

                    public Usuario crear(String nombre, String hash, Turista turista) {
                        return new Administrador(nombre + "-delegado", hash);
                    }
                }).map(CreadorUsuario.class::cast).toList();
        UsuarioFactory factory = new UsuarioFactory(creadores);
        for (RolUsuario rol : RolUsuario.values()) {
            assertEquals("usuario-delegado", factory.crear("usuario", "hash", rol, null).getNombreUsuario());
        }
    }

    @Test
    void detectaRolesFaltantesYDuplicadosAlConstruirse() {
        assertThrows(IllegalStateException.class, () -> new UsuarioFactory(List.of()));
        assertThrows(IllegalStateException.class, () -> new UsuarioFactory(List.of(
                new CreadorAdministrador(), new CreadorAdministrador(), new CreadorCliente(), new CreadorVendedor())));
    }
}
