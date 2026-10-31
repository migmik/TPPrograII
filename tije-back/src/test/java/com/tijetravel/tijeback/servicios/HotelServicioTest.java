package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.tijetravel.tijeback.DnisPrueba;
import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.TipoHospedaje;
import com.tijetravel.tijeback.excepciones.CapacidadExcedidaException;
import com.tijetravel.tijeback.modelos.Administrador;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.repositorios.HotelRepositorio;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.servicios.impl.HotelServicioImpl;

@ExtendWith(MockitoExtension.class)
class HotelServicioTest {
        @Mock
        private HotelRepositorio hotelRepositorio;

        @Mock
        private ReservaRepositorio reservaRepositorio;

        private HotelServicio servicio;
        private Hotel hotel;
        private Sucursal sucursal;
        private Vuelo vuelo;

        @BeforeEach
        void prepararServicio() {
                servicio = new HotelServicioImpl(
                                hotelRepositorio,
                                reservaRepositorio,
                                new AutorizacionServicio(),
                                org.mockito.Mockito.mock(DisponibilidadServicio.class),
                                org.mockito.Mockito.mock(BloqueoEscrituras.class));
                hotel = new Hotel("Hotel Centro", "Calle 1", "Cordoba", "351-1000", 5);
                ReflectionTestUtils.setField(hotel, "codigo", 1);
                sucursal = new Sucursal("Av. Colon 100", "351-2000");
                vuelo = new Vuelo(
                                100,
                                LocalDateTime.of(2026, 10, 1, 10, 0),
                                "Buenos Aires",
                                "Cordoba",
                                20,
                                15,
                                5);
        }

        @Test
        void noReduceLaCapacidadPorDebajoDeLaOcupacionMaxima() {
                when(reservaRepositorio.findByHotelCodigoAndFechaBajaIsNull(1)).thenReturn(List.of(
                                reserva("ana@example.com", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 4)),
                                reserva("luis@example.com", LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 3))));
                when(hotelRepositorio.findById(1)).thenReturn(Optional.of(hotel));

                try {
                        servicio.modificar(
                                new Administrador("admin", "clave", DnisPrueba.siguiente()),
                                1,
                                "Hotel Centro",
                                "Calle 1",
                                "Cordoba",
                                "351-1000",
                                1,
                                25000);
                        fail("Se esperaba CapacidadExcedidaException");
                } catch (CapacidadExcedidaException esperada) {
                        // Es la excepcion que se esperaba.
                }
        }

        @Test
        void permiteUnaCapacidadIgualALaOcupacionMaxima() {
                when(reservaRepositorio.findByHotelCodigoAndFechaBajaIsNull(1)).thenReturn(List.of(
                                reserva("ana@example.com", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 4)),
                                reserva("luis@example.com", LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 3)),
                                reserva("maria@example.com", LocalDate.of(2026, 10, 4), LocalDate.of(2026, 10, 5))));
                when(hotelRepositorio.findById(1)).thenReturn(Optional.of(hotel));

                Hotel modificado = servicio.modificar(
                                new Administrador("admin", "clave", DnisPrueba.siguiente()),
                                1,
                                "Hotel Centro",
                                "Calle 1",
                                "Cordoba",
                                "351-1000",
                                2,
                                25000);

                assertEquals(2, modificado.getCapacidadTotal());
        }

        @Test
        void aceptaLaMismaCiudadEscritaConAcento() {
                when(reservaRepositorio.findByHotelCodigoAndFechaBajaIsNull(1)).thenReturn(List.of(
                                reserva("ana@example.com", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 4))));
                when(hotelRepositorio.findById(1)).thenReturn(Optional.of(hotel));

                Hotel modificado = servicio.modificar(
                                new Administrador("admin", "clave", DnisPrueba.siguiente()),
                                1, "Hotel Centro", "Calle 1", "Córdoba", "351-1000", 5, 25000);

                assertEquals("Córdoba", modificado.getCiudad());
        }

        private Reserva reserva(String email, LocalDate llegada, LocalDate partida) {
                Turista turista = new Turista(DnisPrueba.siguiente(), "Nombre", "Apellido", "Direccion", email, "100", "200", sucursal);
                return new Reserva(
                                turista,
                                vuelo,
                                hotel,
                                ClaseVuelo.TURISTA,
                                TipoHospedaje.MEDIA_PENSION,
                                llegada,
                                partida);
        }
}
