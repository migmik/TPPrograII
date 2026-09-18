package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.TipoHospedaje;
import com.tijetravel.tijeback.excepciones.CapacidadExcedidaException;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Cliente;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vendedor;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.repositorios.HotelRepositorio;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;

@ExtendWith(MockitoExtension.class)
class ReservaServicioTest {
        @Mock
        private ReservaRepositorio reservaRepositorio;
        @Mock
        private TuristaRepositorio turistaRepositorio;
        @Mock
        private VueloRepositorio vueloRepositorio;
        @Mock
        private HotelRepositorio hotelRepositorio;

        private ReservaServicio servicio;
        private Sucursal sucursal;
        private Turista turista;
        private Vuelo vuelo;
        private Hotel hotel;

        @BeforeEach
        void prepararServicio() {
                servicio = new ReservaServicio(
                                reservaRepositorio,
                                turistaRepositorio,
                                vueloRepositorio,
                                hotelRepositorio,
                                new AutorizacionServicio(),
                                new DisponibilidadServicio(reservaRepositorio, vueloRepositorio, hotelRepositorio),
                                org.mockito.Mockito.mock(BloqueoEscrituras.class));

                sucursal = conCodigo(new Sucursal("Av. Siempre Viva 100", "1234"), "codigo", 1);
                turista = conCodigo(
                                new Turista(
                                                "Ana",
                                                "Perez",
                                                "Direccion",
                                                "ana@example.com",
                                                "123",
                                                "456",
                                                sucursal),
                                "codigo",
                                1);
                vuelo = new Vuelo(
                                100,
                                LocalDateTime.of(2026, 10, 1, 10, 0),
                                "Buenos Aires",
                                "Cordoba",
                                2,
                                1,
                                1);
                hotel = conCodigo(new Hotel("Hotel", "Direccion", "Cordoba", "1234", 1), "codigo", 1);
        }

        @Test
        void creaReservaCuandoLosDatosSonCompatiblesYHayCapacidad() {
                prepararBusquedas();
                when(reservaRepositorio.save(any(Reserva.class)))
                                .thenAnswer(invocacion -> invocacion.getArgument(0));

                Reserva reserva = servicio.crear(
                                new Vendedor("vendedor", "clave"),
                                1,
                                100,
                                1,
                                ClaseVuelo.TURISTA,
                                TipoHospedaje.MEDIA_PENSION,
                                LocalDate.of(2026, 10, 1),
                                LocalDate.of(2026, 10, 3));

                assertSame(turista, reserva.getTurista());
                assertSame(sucursal, reserva.getSucursalContratacion());
                assertEquals(ClaseVuelo.TURISTA, reserva.getClaseVuelo());
        }

        @Test
        void rechazaReservaCuandoLaClaseDelVueloEstaCompleta() {
                prepararBusquedas();
                when(reservaRepositorio.countByVueloNumeroAndClaseVuelo(100, ClaseVuelo.TURISTA))
                                .thenReturn(1L);

                assertThrows(
                                CapacidadExcedidaException.class,
                                () -> servicio.crear(
                                                new Vendedor("vendedor", "clave"),
                                                1,
                                                100,
                                                1,
                                                ClaseVuelo.TURISTA,
                                                TipoHospedaje.MEDIA_PENSION,
                                                LocalDate.of(2026, 10, 1),
                                                LocalDate.of(2026, 10, 3)));
        }

        @Test
        void rechazaUnaSegundaReservaDelTuristaParaElMismoVuelo() {
                when(turistaRepositorio.findById(1)).thenReturn(Optional.of(turista));
                when(vueloRepositorio.findById(100)).thenReturn(Optional.of(vuelo));
                when(hotelRepositorio.findById(1)).thenReturn(Optional.of(hotel));
                when(reservaRepositorio.existsByTuristaCodigoAndVueloNumero(1, 100))
                                .thenReturn(true);

                assertThrows(
                                EntidadDuplicadaException.class,
                                () -> ingresarReservaValida(new Vendedor("vendedor", "clave")));
        }

        @Test
        void rechazaHotelCuyaCiudadNoCoincideConElDestino() {
                hotel = conCodigo(new Hotel("Hotel", "Direccion", "Rosario", "1234", 1), "codigo", 1);
                prepararBusquedas();

                assertThrows(
                                OperacionNoPermitidaException.class,
                                () -> ingresarReservaValida(new Vendedor("vendedor", "clave")));
        }

        @Test
        void rechazaReservaCuandoElHotelEstaCompletoEnLasFechasSolicitadas() {
                prepararBusquedas();
                when(reservaRepositorio.buscarSuperpuestasEnHotel(
                                1, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3))).thenReturn(java.util.List.of(
                                                new Reserva(turista, vuelo, hotel, ClaseVuelo.TURISTA,
                                                                TipoHospedaje.MEDIA_PENSION, LocalDate.of(2026, 10, 1),
                                                                LocalDate.of(2026, 10, 3))));

                assertThrows(
                                CapacidadExcedidaException.class,
                                () -> ingresarReservaValida(new Vendedor("vendedor", "clave")));
        }

        @Test
        void clienteNoPuedeCrearReservas() {
                assertThrows(
                                OperacionNoPermitidaException.class,
                                () -> ingresarReservaValida(new Cliente("cliente", "clave", turista)));
        }

        private void prepararBusquedas() {
                when(turistaRepositorio.findById(1)).thenReturn(Optional.of(turista));
                when(vueloRepositorio.findById(100)).thenReturn(Optional.of(vuelo));
                when(hotelRepositorio.findById(1)).thenReturn(Optional.of(hotel));
        }

        private Reserva ingresarReservaValida(Usuario actor) {
                return servicio.crear(
                                actor,
                                1,
                                100,
                                1,
                                ClaseVuelo.TURISTA,
                                TipoHospedaje.MEDIA_PENSION,
                                LocalDate.of(2026, 10, 1),
                                LocalDate.of(2026, 10, 3));
        }

        private <T> T conCodigo(T entidad, String campo, Integer codigo) {
                try {
                        Field atributo = entidad.getClass().getDeclaredField(campo);
                        atributo.setAccessible(true);
                        atributo.set(entidad, codigo);
                        return entidad;
                } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("No se pudo preparar la entidad de prueba", e);
                }
        }
}
