package com.tijetravel.tijeback.modelos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tijetravel.tijeback.DnisPrueba;
import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.TipoHospedaje;

class ModelosTest {

        @Test
        void rechazaHotelConCapacidadNegativa() {
                try {
                        new Hotel("Hotel", "Direccion", "Cordoba", "1234", -1);
                        fail("Se esperaba IllegalArgumentException");
                } catch (IllegalArgumentException esperada) {
                        // Es la excepcion que se esperaba.
                }
        }

        @Test
        void rechazaVueloSiLasPlazasPorClaseSuperanElTotal() {
                try {
                        new Vuelo(
                                100,
                                LocalDateTime.of(2026, 10, 1, 10, 0),
                                "Buenos Aires",
                                "Cordoba",
                                10,
                                8,
                                3);
                        fail("Se esperaba IllegalArgumentException");
                } catch (IllegalArgumentException esperada) {
                        // Es la excepcion que se esperaba.
                }
        }

        @Test
        void reservaExigeUnIntervaloDeFechasValido() {
                Sucursal sucursal = new Sucursal("Direccion", "1234");
                Turista turista = new Turista(DnisPrueba.siguiente(), "Ana", "Perez", "Direccion", "ana@example.com", "123", "456", sucursal);
                Hotel hotel = new Hotel("Hotel", "Direccion", "Cordoba", "1234", 5);
                Vuelo vuelo = new Vuelo(
                                100,
                                LocalDateTime.of(2026, 10, 1, 10, 0),
                                "Buenos Aires",
                                "Cordoba",
                                10,
                                8,
                                2);

                try {
                        new Reserva(
                                turista,
                                vuelo,
                                hotel,
                                ClaseVuelo.TURISTA,
                                TipoHospedaje.MEDIA_PENSION,
                                LocalDate.of(2026, 10, 2),
                                LocalDate.of(2026, 10, 1));
                        fail("Se esperaba IllegalArgumentException");
                } catch (IllegalArgumentException esperada) {
                        // Es la excepcion que se esperaba.
                }
        }

        @Test
        void mantieneElPolimorfismoDePermisos() {
                Administrador administrador = new Administrador("admin", "clave", DnisPrueba.siguiente());
                Vendedor vendedor = new Vendedor("vendedor", "clave", DnisPrueba.siguiente());

                assertTrue(administrador.tienePermiso(Permiso.ADMINISTRAR_USUARIOS));
                assertTrue(vendedor.tienePermiso(Permiso.ADMINISTRAR_RESERVAS));
                assertFalse(vendedor.tienePermiso(Permiso.ADMINISTRAR_HOTELES));
                administrador.actualizarCredenciales("admin2", "otra-clave");
        }

        @Test
        void normalizaLosTextosDelModelo() {
                Sucursal sucursal = new Sucursal("  Av. Colon 100  ", "  351-1000  ");
                Turista turista = new Turista(DnisPrueba.siguiente(), "  Ana  ",
                                "  Perez  ",
                                "  Calle 1  ",
                                "  ana@example.com  ",
                                "  100  ",
                                "  200  ",
                                sucursal);

                assertEquals("Av. Colon 100", sucursal.getDireccion());
                assertEquals("Ana", turista.getNombre());
                assertEquals("ana@example.com", turista.getEmail());
        }

        @Test
        void rechazaEmailConFormatoInvalido() {
                Sucursal sucursal = new Sucursal("Direccion", "1234");

                try {
                        new Turista(DnisPrueba.siguiente(), "Ana", "Perez", "Direccion", "email-invalido", "123", "456", sucursal);
                        fail("Se esperaba IllegalArgumentException");
                } catch (IllegalArgumentException esperada) {
                        // Es la excepcion que se esperaba.
                }
        }

        @Test
        void clienteNoPuedeAsociarseAUnFamiliar() {
                Sucursal sucursal = new Sucursal("Direccion", "1234");
                Turista titular = new Turista(DnisPrueba.siguiente(), "Ana", "Perez", "Direccion", "ana@example.com", "123", "456", sucursal);
                Turista familiar = new Turista(DnisPrueba.siguiente(), "Luis",
                                "Perez",
                                "Direccion",
                                "luis@example.com",
                                "123",
                                "789",
                                sucursal,
                                titular);

                try {
                        new Cliente("cliente", "clave", familiar);
                        fail("Se esperaba IllegalArgumentException");
                } catch (IllegalArgumentException esperada) {
                        // Es la excepcion que se esperaba.
                }
        }

        @Test
        void clienteSoloVeASuGrupoFamiliarYLosEmpleadosVenATodos() {
                Sucursal sucursal = new Sucursal("Direccion", "1234");
                Turista titular = new Turista(DnisPrueba.siguiente(), "Ana", "Perez", "Direccion", "ana@example.com", "123", "456", sucursal);
                Turista familiar = new Turista(DnisPrueba.siguiente(), "Luis", "Perez", "Direccion", "luis@example.com", "123", "789", sucursal, titular);
                Turista otro = new Turista(DnisPrueba.siguiente(), "Eva", "Gomez", "Direccion", "eva@example.com", "123", "111", sucursal);
                ReflectionTestUtils.setField(titular, "codigo", 1);
                ReflectionTestUtils.setField(familiar, "codigo", 2);
                ReflectionTestUtils.setField(otro, "codigo", 3);
                Cliente cliente = new Cliente("cliente", "clave", titular);
                Vendedor vendedor = new Vendedor("vendedor", "clave", DnisPrueba.siguiente());

                assertTrue(cliente.puedeVerTurista(titular));
                assertTrue(cliente.puedeVerTurista(familiar));
                assertFalse(cliente.puedeVerTurista(otro));
                assertEquals(1, cliente.getCodigoGrupoFamiliar());
                assertTrue(vendedor.puedeVerTurista(otro));
                assertNull(vendedor.getCodigoGrupoFamiliar());
                assertTrue(vendedor.tienePermiso(Permiso.VER_CATALOGO_COMPLETO));
                assertFalse(vendedor.tienePermiso(Permiso.VER_BALANCE));
                assertFalse(cliente.tienePermiso(Permiso.VER_CATALOGO_COMPLETO));
        }
}
