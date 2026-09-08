package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Administrador;
import com.tijetravel.tijeback.modelos.Cliente;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vendedor;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;

@ExtendWith(MockitoExtension.class)
class UsuarioServicioTest {
    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private TuristaRepositorio turistaRepositorio;

    @Mock
    private PasswordEncoder codificadorContrasenias;

    private UsuarioServicio servicio;

    @BeforeEach
    void prepararServicio() {
        servicio = new UsuarioServicio(
                usuarioRepositorio,
                turistaRepositorio,
                new AutorizacionServicio(),
                codificadorContrasenias,
                org.mockito.Mockito.mock(BloqueoEscrituras.class),
                new com.tijetravel.tijeback.servicios.usuarios.UsuarioFactory(java.util.List.of(
                        new com.tijetravel.tijeback.servicios.usuarios.CreadorCliente(),
                        new com.tijetravel.tijeback.servicios.usuarios.CreadorVendedor(),
                        new com.tijetravel.tijeback.servicios.usuarios.CreadorAdministrador())));
    }

    @Test
    void vendedorNoPuedeAdministrarUsuarios() {
        assertThrows(
                OperacionNoPermitidaException.class,
                () -> servicio.crear(
                        new Vendedor("vendedor", "clave"),
                        "nuevo",
                        "clave",
                        RolUsuario.VENDEDOR,
                        null));

        verify(usuarioRepositorio, never()).save(any());
    }

    @Test
    void rechazaContraseniasVaciasAntesDeCodificarlas() {
        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.crear(
                        new Administrador("admin", "clave"),
                        "nuevo",
                        "  ",
                        RolUsuario.VENDEDOR,
                        null));

        verify(codificadorContrasenias, never()).encode(any());
        verify(usuarioRepositorio, never()).save(any());
    }

    @Test
    void clienteSoloPuedeAsociarseAUnTuristaTitular() {
        Sucursal sucursal = new Sucursal("Av. Colon 100", "351-1000");
        Turista titular = new Turista(
                "Ana", "Perez", "Calle 1", "ana@example.com", "100", "200", sucursal);
        Turista familiar = new Turista(
                "Luis", "Perez", "Calle 1", "luis@example.com", "100", "300", sucursal, titular);
        when(turistaRepositorio.findById(2)).thenReturn(Optional.of(familiar));
        when(codificadorContrasenias.encode("clave")).thenReturn("{bcrypt}hash");

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.crear(
                        new Administrador("admin", "clave"),
                        "cliente",
                        "clave",
                        RolUsuario.CLIENTE,
                        2));

        verify(usuarioRepositorio, never()).save(any());
    }

    @Test
    void creaClienteVinculadoAlTitular() {
        Sucursal sucursal = new Sucursal("Av. Colon 100", "351-1000");
        Turista titular = new Turista(
                "Ana", "Perez", "Calle 1", "ana@example.com", "100", "200", sucursal);
        when(turistaRepositorio.findById(1)).thenReturn(Optional.of(titular));
        when(usuarioRepositorio.save(any(Usuario.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        when(codificadorContrasenias.encode("clave")).thenReturn("{bcrypt}hash");

        Usuario usuario = servicio.crear(
                new Administrador("admin", "clave"),
                "cliente",
                "clave",
                RolUsuario.CLIENTE,
                1);

        Cliente cliente = assertInstanceOf(Cliente.class, usuario);
        assertSame(titular, cliente.getTurista());
        assertEquals("{bcrypt}hash", cliente.getContrasenia());
    }

    @Test
    void usuarioNoPuedeEliminarseASiMismo() {
        Administrador administrador = new Administrador("admin", "clave");
        when(usuarioRepositorio.findById(1)).thenReturn(Optional.of(administrador));

        assertThrows(
                OperacionNoPermitidaException.class,
                () -> servicio.eliminar(administrador, 1));

        verify(usuarioRepositorio, never()).delete(any());
    }

    @Test
    void noEliminaElUltimoAdministrador() {
        Administrador actor = new Administrador("admin-principal", "clave");
        Administrador objetivo = new Administrador("admin-secundario", "clave");
        when(usuarioRepositorio.findById(2)).thenReturn(Optional.of(objetivo));
        when(usuarioRepositorio.countByRol(RolUsuario.ADMINISTRADOR)).thenReturn(1L);

        assertThrows(
                OperacionNoPermitidaException.class,
                () -> servicio.eliminar(actor, 2));

        verify(usuarioRepositorio, never()).delete(any());
    }

}
