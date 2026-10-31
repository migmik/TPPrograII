package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.tijetravel.tijeback.DnisPrueba;
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
import com.tijetravel.tijeback.servicios.impl.DisponibilidadServicioImpl;
import com.tijetravel.tijeback.servicios.impl.ReservaServicioImpl;

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
                servicio = new ReservaServicioImpl(
                                reservaRepositorio,
                                turistaRepositorio,
                                vueloRepositorio,
                                hotelRepositorio,
                                new AutorizacionServicio(),
                                new DisponibilidadServicioImpl(reservaRepositorio, vueloRepositorio, hotelRepositorio),
                                org.mockito.Mockito.mock(BloqueoEscrituras.class));

                sucursal = conCodigo(new Sucursal("Av. Siempre Viva 100", "1234"), "codigo", 1);
                turista = conCodigo(
                                new Turista(DnisPrueba.siguiente(), "Ana",
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
                                .then(returnsFirstArg());

                Reserva reserva = servicio.crear(
                                new Vendedor("vendedor", "clave", DnisPrueba.siguiente()),
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

                try {
                        servicio.crear(
                                new Vendedor("vendedor", "clave", DnisPrueba.siguiente()),
                                1,
                                100,
                                1,
                                ClaseVuelo.TURISTA,
                                TipoHospedaje.MEDIA_PENSION,
                                LocalDate.of(2026, 10, 1),
                                LocalDate.of(2026, 10, 3));
                        fail("Se esperaba CapacidadExcedidaException");
                } catch (CapacidadExcedidaException esperada) {
                        // Es la excepcion que se esperaba.
                }
        }

        @Test
        void rechazaUnaSegundaReservaDelTuristaParaElMismoVuelo() {
                when(turistaRepositorio.findById(1)).thenReturn(Optional.of(turista));
                when(vueloRepositorio.findById(100)).thenReturn(Optional.of(vuelo));
                when(hotelRepositorio.findById(1)).thenReturn(Optional.of(hotel));
                when(reservaRepositorio.existsByTuristaCodigoAndVueloNumero(1, 100))
                                .thenReturn(true);

                try {
                        ingresarReservaValida(new Vendedor("vendedor", "clave", DnisPrueba.siguiente()));
                        fail("Se esperaba EntidadDuplicadaException");
                } catch (EntidadDuplicadaException esperada) {
                        // Es la excepcion que se esperaba.
                }
        }

        @Test
        void rechazaHotelCuyaCiudadNoCoincideConElDestino() {
                hotel = conCodigo(new Hotel("Hotel", "Direccion", "Rosario", "1234", 1), "codigo", 1);
                prepararBusquedas();

                try {
                        ingresarReservaValida(new Vendedor("vendedor", "clave", DnisPrueba.siguiente()));
                        fail("Se esperaba OperacionNoPermitidaException");
                } catch (OperacionNoPermitidaException esperada) {
                        // Es la excepcion que se esperaba.
                }
        }

        @Test
        void rechazaReservaCuandoElHotelEstaCompletoEnLasFechasSolicitadas() {
                prepararBusquedas();
                when(reservaRepositorio.buscarSuperpuestasEnHotel(
                                1, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3))).thenReturn(java.util.List.of(
                                                new Reserva(turista, vuelo, hotel, ClaseVuelo.TURISTA,
                                                                TipoHospedaje.MEDIA_PENSION, LocalDate.of(2026, 10, 1),
                                                                LocalDate.of(2026, 10, 3))));

                try {
                        ingresarReservaValida(new Vendedor("vendedor", "clave", DnisPrueba.siguiente()));
                        fail("Se esperaba CapacidadExcedidaException");
                } catch (CapacidadExcedidaException esperada) {
                        // Es la excepcion que se esperaba.
                }
        }

        @Test
        void clientePuedeCrearReservaParaSiMismo() {
                prepararBusquedas();
                when(reservaRepositorio.save(any(Reserva.class)))
                                .then(returnsFirstArg());
                Reserva reserva = ingresarReservaValida(new Cliente("cliente", "clave", turista));
                assertSame(turista, reserva.getTurista());
        }

        @Test
        void clienteNoPuedeCrearReservaParaOtroGrupo() {
                Turista ajeno = conCodigo(new Turista(DnisPrueba.siguiente(), "Juan", "Lopez",
                                "Direccion", "juan@example.com", "123", "456", sucursal), "codigo", 2);
                when(turistaRepositorio.findById(2)).thenReturn(Optional.of(ajeno));
                try {
                        servicio.crear(new Cliente("cliente", "clave", turista), 2, 100, 1,
                                ClaseVuelo.TURISTA, TipoHospedaje.MEDIA_PENSION,
                                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3));
                        fail("Se esperaba OperacionNoPermitidaException");
                } catch (OperacionNoPermitidaException esperada) {
                        // Es la excepcion que se esperaba.
                }
        }

        private void prepararBusquedas() {
                when(turistaRepositorio.findById(1)).thenReturn(Optional.of(turista));
                when(vueloRepositorio.findById(100)).thenReturn(Optional.of(vuelo));
                when(hotelRepositorio.findById(1)).thenReturn(Optional.of(hotel));
        }

        @Test
        void buscaUnaPaginaPorTuristaSinConsultarTodasLasReservas() {
                Vendedor vendedor = new Vendedor("vendedor", "clave", DnisPrueba.siguiente());
                Page<Reserva> pagina = new PageImpl<>(java.util.List.of(), PageRequest.of(0, 20), 0);
                when(reservaRepositorio.buscarPagina(eq(null), eq(1), eq(null), eq(null), eq(null),
                                eq(null), eq(null), eq(null), any(Pageable.class))).thenReturn(pagina);

                Page<Reserva> resultado = servicio.paginaPara(
                                vendedor, 1, null, null, null, null, null, null, 0, 20);

                assertSame(pagina, resultado);
                verify(reservaRepositorio).buscarPagina(eq(null), eq(1), eq(null), eq(null), eq(null),
                                eq(null), eq(null), eq(null), any(Pageable.class));
                verify(reservaRepositorio, never()).findAll(any(Pageable.class));
        }

        @Test
        void aceptaFiltrosCombinadosYFechasIncompletas() {
                Vendedor vendedor = new Vendedor("vendedor", "clave", DnisPrueba.siguiente());
                servicio.paginaPara(vendedor, 1, null, 100, null, null, null, null, 0, 20);
                servicio.paginaPara(vendedor, null, null, null, null,
                                LocalDate.of(2026, 10, 1), null, null, 0, 20);
                verify(reservaRepositorio).buscarPagina(eq(null), eq(1), eq(null), eq(100), eq(null),
                                eq(null), eq(null), eq(null), any(Pageable.class));
                verify(reservaRepositorio, never()).findAll(any(Pageable.class));
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
