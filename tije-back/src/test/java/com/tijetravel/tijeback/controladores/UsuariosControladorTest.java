package com.tijetravel.tijeback.controladores;

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
class UsuariosControladorTest {
    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private TuristaRepositorio turistaRepositorio;

    private UsuariosControlador controlador;

    @BeforeEach
    void prepararControlador() {
        controlador = new UsuariosControlador(
                usuarioRepositorio,
                turistaRepositorio,
                new AutorizacionControlador());
    }

    @Test
    void vendedorNoPuedeAdministrarUsuarios() {
        assertThrows(
                OperacionNoPermitidaException.class,
                () -> controlador.ingresar(
                        new Vendedor("vendedor", "clave"),
                        "nuevo",
                        "clave",
                        RolUsuario.VENDEDOR,
                        null));

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

        assertThrows(
                IllegalArgumentException.class,
                () -> controlador.ingresar(
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

        Usuario usuario = controlador.ingresar(
                new Administrador("admin", "clave"),
                "cliente",
                "clave",
                RolUsuario.CLIENTE,
                1);

        Cliente cliente = assertInstanceOf(Cliente.class, usuario);
        assertSame(titular, cliente.getTurista());
    }

    @Test
    void usuarioNoPuedeEliminarseASiMismo() {
        Administrador administrador = new Administrador("admin", "clave");
        when(usuarioRepositorio.findById(1)).thenReturn(Optional.of(administrador));

        assertThrows(
                OperacionNoPermitidaException.class,
                () -> controlador.eliminar(administrador, 1));

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
                () -> controlador.eliminar(actor, 2));

        verify(usuarioRepositorio, never()).delete(any());
    }

    @Test
    void normalizaElNombreAntesDeBuscar() {
        Administrador administrador = new Administrador("admin", "clave");
        when(usuarioRepositorio.findByNombreUsuarioIgnoreCase("admin"))
                .thenReturn(Optional.of(administrador));

        assertSame(administrador, controlador.encontrarPorNombre("  admin  "));
    }
}
