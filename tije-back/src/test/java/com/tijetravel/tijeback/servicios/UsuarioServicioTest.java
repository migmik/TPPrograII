package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tijetravel.tijeback.DnisPrueba;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Administrador;
import com.tijetravel.tijeback.modelos.Cliente;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vendedor;
import com.tijetravel.tijeback.repositorios.SucursalRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;
import com.tijetravel.tijeback.servicios.impl.UsuarioServicioImpl;

@ExtendWith(MockitoExtension.class)
class UsuarioServicioTest {
        @Mock
        private UsuarioRepositorio usuarioRepositorio;

        @Mock
        private TuristaRepositorio turistaRepositorio;
        @Mock
        private SucursalRepositorio sucursalRepositorio;

        @Mock
        private PasswordEncoder codificadorContrasenias;

        private UsuarioServicio servicio;

        @BeforeEach
        void prepararServicio() {
                servicio = new UsuarioServicioImpl(
                                usuarioRepositorio,
                                turistaRepositorio,
                                sucursalRepositorio,
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
                try {
                        servicio.crear(
                                new Vendedor("vendedor", "clave", DnisPrueba.siguiente()),
                                "nuevo",
                                "clave",
                                RolUsuario.VENDEDOR,
                                null, null);
                        fail("Se esperaba OperacionNoPermitidaException");
                } catch (OperacionNoPermitidaException esperada) {
                        // Es la excepcion que se esperaba.
                }

                verify(usuarioRepositorio, never()).save(any());
        }

        @Test
        void filtraUsuariosPorRolSinListarTodaLaTabla() {
                Administrador administrador = new Administrador(
                                "admin", "clave", DnisPrueba.siguiente());
                Vendedor vendedor = new Vendedor(
                                "vendedor", "clave", DnisPrueba.siguiente());
                when(usuarioRepositorio.findByRol(RolUsuario.VENDEDOR)).thenReturn(List.of(vendedor));

                assertEquals(List.of(vendedor), servicio.listarPara(administrador, RolUsuario.VENDEDOR));

                verify(usuarioRepositorio).findByRol(RolUsuario.VENDEDOR);
                verify(usuarioRepositorio, never()).findAll();
        }

        @Test
        void registraClienteYTuristaTitularEnLaMismaOperacion() {
                Sucursal sucursal = new Sucursal("Av. Colon 100", "351-1000");
                when(sucursalRepositorio.findById(1)).thenReturn(Optional.of(sucursal));
                when(codificadorContrasenias.encode("clave-segura")).thenReturn("{bcrypt}hash");
                when(turistaRepositorio.save(any(Turista.class)))
                                .then(returnsFirstArg());
                when(usuarioRepositorio.save(any(Usuario.class)))
                                .then(returnsFirstArg());

                Usuario usuario = servicio.registrarCliente(
                                "ana", "clave-segura", DnisPrueba.siguiente(), "Ana", "Perez",
                                "Calle 1", "ana@example.com", "100", "200", 1);

                Cliente cliente = assertInstanceOf(Cliente.class, usuario);
                assertEquals(RolUsuario.CLIENTE, cliente.getRol());
                assertTrue(cliente.getTurista().isTitular());
                assertEquals("{bcrypt}hash", cliente.getContrasenia());
                verify(turistaRepositorio).save(any(Turista.class));
                verify(usuarioRepositorio).save(cliente);
        }

        @Test
        void rechazaContraseniasVaciasAntesDeCodificarlas() {
                try {
                        servicio.crear(
                                new Administrador("admin", "clave", DnisPrueba.siguiente()),
                                "nuevo",
                                "  ",
                                RolUsuario.VENDEDOR,
                                null, null);
                        fail("Se esperaba IllegalArgumentException");
                } catch (IllegalArgumentException esperada) {
                        // Es la excepcion que se esperaba.
                }

                verify(codificadorContrasenias, never()).encode(any());
                verify(usuarioRepositorio, never()).save(any());
        }

        @Test
        void clienteSoloPuedeAsociarseAUnTuristaTitular() {
                Sucursal sucursal = new Sucursal("Av. Colon 100", "351-1000");
                Turista titular = new Turista(DnisPrueba.siguiente(), "Ana", "Perez", "Calle 1", "ana@example.com", "100", "200", sucursal);
                Turista familiar = new Turista(DnisPrueba.siguiente(), "Luis", "Perez", "Calle 1", "luis@example.com", "100", "300", sucursal, titular);
                when(turistaRepositorio.findById(2)).thenReturn(Optional.of(familiar));
                when(codificadorContrasenias.encode("clave")).thenReturn("{bcrypt}hash");

                try {
                        servicio.crear(
                                new Administrador("admin", "clave", DnisPrueba.siguiente()),
                                "cliente",
                                "clave",
                                RolUsuario.CLIENTE,
                                2, null);
                        fail("Se esperaba IllegalArgumentException");
                } catch (IllegalArgumentException esperada) {
                        // Es la excepcion que se esperaba.
                }

                verify(usuarioRepositorio, never()).save(any());
        }

        @Test
        void creaClienteVinculadoAlTitular() {
                Sucursal sucursal = new Sucursal("Av. Colon 100", "351-1000");
                Turista titular = new Turista(DnisPrueba.siguiente(), "Ana", "Perez", "Calle 1", "ana@example.com", "100", "200", sucursal);
                when(turistaRepositorio.findById(1)).thenReturn(Optional.of(titular));
                when(usuarioRepositorio.save(any(Usuario.class)))
                                .then(returnsFirstArg());
                when(codificadorContrasenias.encode("clave")).thenReturn("{bcrypt}hash");

                Usuario usuario = servicio.crear(
                                new Administrador("admin", "clave", DnisPrueba.siguiente()),
                                "cliente",
                                "clave",
                                RolUsuario.CLIENTE,
                                1, null);

                Cliente cliente = assertInstanceOf(Cliente.class, usuario);
                assertSame(titular, cliente.getTurista());
                assertEquals("{bcrypt}hash", cliente.getContrasenia());
        }

        @Test
        void usuarioNoPuedeEliminarseASiMismo() {
                Administrador administrador = new Administrador("admin", "clave", DnisPrueba.siguiente());
                when(usuarioRepositorio.findById(1)).thenReturn(Optional.of(administrador));

                try {
                        servicio.eliminar(administrador, 1);
                        fail("Se esperaba OperacionNoPermitidaException");
                } catch (OperacionNoPermitidaException esperada) {
                        // Es la excepcion que se esperaba.
                }

                verify(usuarioRepositorio, never()).delete(any());
        }

        @Test
        void noEliminaElUltimoAdministrador() {
                Administrador actor = new Administrador("admin-principal", "clave", DnisPrueba.siguiente());
                Administrador objetivo = new Administrador("admin-secundario", "clave", DnisPrueba.siguiente());
                when(usuarioRepositorio.findById(2)).thenReturn(Optional.of(objetivo));
                when(usuarioRepositorio.countByRol(RolUsuario.ADMINISTRADOR)).thenReturn(1L);

                try {
                        servicio.eliminar(actor, 2);
                        fail("Se esperaba OperacionNoPermitidaException");
                } catch (OperacionNoPermitidaException esperada) {
                        // Es la excepcion que se esperaba.
                }

                verify(usuarioRepositorio, never()).delete(any());
        }

}
