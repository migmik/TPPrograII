package com.tijetravel.tijeback.modelos;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.TipoHospedaje;

class ModelosTest {

        @Test
        void rechazaHotelConCapacidadNegativa() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> new Hotel("Hotel", "Direccion", "Cordoba", "1234", -1));
        }

        @Test
        void rechazaVueloSiLasPlazasPorClaseSuperanElTotal() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> new Vuelo(
                                                100,
                                                LocalDateTime.of(2026, 10, 1, 10, 0),
                                                "Buenos Aires",
                                                "Cordoba",
                                                10,
                                                8,
                                                3));
        }

        @Test
        void reservaExigeUnIntervaloDeFechasValido() {
                Sucursal sucursal = new Sucursal("Direccion", "1234");
                Turista turista = new Turista(
                                "Ana", "Perez", "Direccion", "ana@example.com", "123", "456", sucursal);
                Hotel hotel = new Hotel("Hotel", "Direccion", "Cordoba", "1234", 5);
                Vuelo vuelo = new Vuelo(
                                100,
                                LocalDateTime.of(2026, 10, 1, 10, 0),
                                "Buenos Aires",
                                "Cordoba",
                                10,
                                8,
                                2);

                assertThrows(
                                IllegalArgumentException.class,
                                () -> new Reserva(
                                                turista,
                                                vuelo,
                                                hotel,
                                                ClaseVuelo.TURISTA,
                                                TipoHospedaje.MEDIA_PENSION,
                                                LocalDate.of(2026, 10, 2),
                                                LocalDate.of(2026, 10, 1)));
        }

        @Test
        void mantieneElPolimorfismoDePermisos() {
                Administrador administrador = new Administrador("admin", "clave");
                Vendedor vendedor = new Vendedor("vendedor", "clave");

                assertTrue(administrador.tienePermiso(Permiso.ADMINISTRAR_USUARIOS));
                assertTrue(vendedor.tienePermiso(Permiso.ADMINISTRAR_RESERVAS));
                assertFalse(vendedor.tienePermiso(Permiso.ADMINISTRAR_HOTELES));
                assertDoesNotThrow(() -> administrador.actualizarCredenciales("admin2", "otra-clave"));
        }

        @Test
        void normalizaLosTextosDelModelo() {
                Sucursal sucursal = new Sucursal("  Av. Colon 100  ", "  351-1000  ");
                Turista turista = new Turista(
                                "  Ana  ",
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

                assertThrows(
                                IllegalArgumentException.class,
                                () -> new Turista(
                                                "Ana", "Perez", "Direccion", "email-invalido", "123", "456", sucursal));
        }

        @Test
        void clienteNoPuedeAsociarseAUnFamiliar() {
                Sucursal sucursal = new Sucursal("Direccion", "1234");
                Turista titular = new Turista(
                                "Ana", "Perez", "Direccion", "ana@example.com", "123", "456", sucursal);
                Turista familiar = new Turista(
                                "Luis",
                                "Perez",
                                "Direccion",
                                "luis@example.com",
                                "123",
                                "789",
                                sucursal,
                                titular);

                assertThrows(
                                IllegalArgumentException.class,
                                () -> new Cliente("cliente", "clave", familiar));
        }
}
