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
import com.tijetravel.tijeback.RolesPrueba;
import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Empleado;
import com.tijetravel.tijeback.modelos.Cliente;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.repositorios.RolRepositorio;
import com.tijetravel.tijeback.repositorios.SucursalRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;
import com.tijetravel.tijeback.servicios.impl.UsuarioServicioImpl;

@ExtendWith(MockitoExtension.class)
class UsuarioServicioTest {
        @Mock
        private UsuarioRepositorio usuarioRepositorio;

        @Mock
        private RolRepositorio rolRepositorio;

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
                                rolRepositorio,
                                turistaRepositorio,
                                sucursalRepositorio,
                                new AutorizacionServicio(),
                                codificadorContrasenias,
                                org.mockito.Mockito.mock(BloqueoEscrituras.class),
                                new com.tijetravel.tijeback.servicios.usuarios.UsuarioFactory(java.util.List.of(
                                                new com.tijetravel.tijeback.servicios.usuarios.CreadorCliente(),
                                                new com.tijetravel.tijeback.servicios.usuarios.CreadorEmpleado())));
        }

        @Test
        void vendedorNoPuedeAdministrarUsuarios() {
                try {
                        servicio.crear(
                                new Empleado("vendedor", "clave", DnisPrueba.siguiente(), RolesPrueba.vendedor()),
                                "nuevo",
                                "clave",
                                2,
                                null, null);
                        fail("Se esperaba OperacionNoPermitidaException");
                } catch (OperacionNoPermitidaException esperada) {
                        // Es la excepcion que se esperaba.
                }

                verify(usuarioRepositorio, never()).save(any());
        }

        @Test
        void filtraUsuariosPorRolSinListarTodaLaTabla() {
                Empleado administrador = new Empleado(
                                "admin", "clave", DnisPrueba.siguiente(), RolesPrueba.administrador());
                Empleado vendedor = new Empleado(
                                "vendedor", "clave", DnisPrueba.siguiente(), RolesPrueba.vendedor());
                when(usuarioRepositorio.listarActivosPorRol(2)).thenReturn(List.of(vendedor));

                assertEquals(List.of(vendedor), servicio.listarPara(administrador, 2));

                verify(usuarioRepositorio).listarActivosPorRol(2);
                verify(usuarioRepositorio, never()).listarActivos();
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
                when(rolRepositorio.findFirstByTipoUsuarioAndFechaBajaIsNullOrderByCodigo(
                                com.tijetravel.tijeback.enums.TipoUsuario.CLIENTE)).thenReturn(RolesPrueba.cliente());

                Usuario usuario = servicio.registrarCliente(
                                "ana", "clave-segura", DnisPrueba.siguiente(), "Ana", "Perez",
                                "Calle 1", "ana@example.com", "100", "200", 1);

                Cliente cliente = assertInstanceOf(Cliente.class, usuario);
                assertEquals("Cliente", cliente.getRol().getNombre());
                assertTrue(cliente.tienePermiso(Permiso.CREAR_RESERVAS));
                assertTrue(cliente.getTurista().isTitular());
                assertEquals("{bcrypt}hash", cliente.getContrasenia());
                verify(turistaRepositorio).save(any(Turista.class));
                verify(usuarioRepositorio).save(cliente);
        }

        @Test
        void rechazaContraseniasVaciasAntesDeCodificarlas() {
                when(rolRepositorio.findById(2)).thenReturn(Optional.of(RolesPrueba.vendedor()));
                try {
                        servicio.crear(
                                new Empleado("admin", "clave", DnisPrueba.siguiente(), RolesPrueba.administrador()),
                                "nuevo",
                                "  ",
                                2,
                                null, DnisPrueba.siguiente());
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
                when(rolRepositorio.findById(3)).thenReturn(Optional.of(RolesPrueba.cliente()));
                when(codificadorContrasenias.encode("clave")).thenReturn("{bcrypt}hash");

                try {
                        servicio.crear(
                                new Empleado("admin", "clave", DnisPrueba.siguiente(), RolesPrueba.administrador()),
                                "cliente",
                                "clave",
                                3,
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
                when(rolRepositorio.findById(3)).thenReturn(Optional.of(RolesPrueba.cliente()));
                when(usuarioRepositorio.save(any(Usuario.class)))
                                .then(returnsFirstArg());
                when(codificadorContrasenias.encode("clave")).thenReturn("{bcrypt}hash");

                Usuario usuario = servicio.crear(
                                new Empleado("admin", "clave", DnisPrueba.siguiente(), RolesPrueba.administrador()),
                                "cliente",
                                "clave",
                                3,
                                1, null);

                Cliente cliente = assertInstanceOf(Cliente.class, usuario);
                assertSame(titular, cliente.getTurista());
                assertEquals("{bcrypt}hash", cliente.getContrasenia());
        }

        @Test
        void usuarioNoPuedeDarseDeBajaASiMismo() {
                Empleado administrador = new Empleado("admin", "clave", DnisPrueba.siguiente(), RolesPrueba.administrador());
                when(usuarioRepositorio.findById(1)).thenReturn(Optional.of(administrador));

                try {
                        servicio.darDeBaja(administrador, 1);
                        fail("Se esperaba OperacionNoPermitidaException");
                } catch (OperacionNoPermitidaException esperada) {
                        // Es la excepcion que se esperaba.
                }

                verify(usuarioRepositorio, never()).delete(any());
        }

        @Test
        void noDaDeBajaAlUltimoQueAdministraUsuarios() {
                Empleado actor = new Empleado("admin-principal", "clave", DnisPrueba.siguiente(), RolesPrueba.administrador());
                Empleado objetivo = new Empleado("admin-secundario", "clave", DnisPrueba.siguiente(), RolesPrueba.administrador());
                when(usuarioRepositorio.findById(2)).thenReturn(Optional.of(objetivo));
                when(usuarioRepositorio.contarActivosConPermiso(Permiso.ADMINISTRAR_USUARIOS)).thenReturn(1L);

                try {
                        servicio.darDeBaja(actor, 2);
                        fail("Se esperaba OperacionNoPermitidaException");
                } catch (OperacionNoPermitidaException esperada) {
                        // Es la excepcion que se esperaba.
                }

                verify(usuarioRepositorio, never()).delete(any());
        }

        @Test
        void cambiaElRolYConservaLaContraseniaSiNoSeEscribeOtra() {
                Empleado actor = new Empleado("admin", "clave", DnisPrueba.siguiente(), RolesPrueba.administrador());
                Empleado empleado = new Empleado("ana", "{bcrypt}viejo", "30111222", RolesPrueba.vendedor());
                when(usuarioRepositorio.findById(5)).thenReturn(Optional.of(empleado));
                com.tijetravel.tijeback.modelos.Rol hotelero = new com.tijetravel.tijeback.modelos.Rol("Hotelero",
                                com.tijetravel.tijeback.enums.TipoUsuario.EMPLEADO,
                                List.of(Permiso.ADMINISTRAR_HOTELES));
                when(rolRepositorio.findById(7)).thenReturn(Optional.of(hotelero));

                servicio.modificar(actor, 5, "ana", "", "30111222", 7);

                assertEquals("Hotelero", empleado.getRol().getNombre());
                assertTrue(empleado.tienePermiso(Permiso.ADMINISTRAR_HOTELES));
                assertEquals(false, empleado.tienePermiso(Permiso.ADMINISTRAR_VUELOS));
                assertEquals("{bcrypt}viejo", empleado.getContrasenia());
                verify(codificadorContrasenias, never()).encode(any());
        }

        @Test
        void unEmpleadoNoPuedeRecibirUnRolDeClientes() {
                Empleado actor = new Empleado("admin", "clave", DnisPrueba.siguiente(), RolesPrueba.administrador());
                Empleado empleado = new Empleado("ana", "{bcrypt}viejo", "30111222", RolesPrueba.vendedor());
                when(usuarioRepositorio.findById(5)).thenReturn(Optional.of(empleado));
                when(rolRepositorio.findById(3)).thenReturn(Optional.of(RolesPrueba.cliente()));

                try {
                        servicio.modificar(actor, 5, "ana", "", "30111222", 3);
                        fail("Se esperaba IllegalArgumentException");
                } catch (IllegalArgumentException esperada) {
                        // El rol de clientes no es para empleados.
                }
                assertEquals("Vendedor", empleado.getRol().getNombre());
        }
}
