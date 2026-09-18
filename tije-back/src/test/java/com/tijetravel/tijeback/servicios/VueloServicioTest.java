package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.excepciones.CapacidadExcedidaException;
import com.tijetravel.tijeback.modelos.Administrador;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;

@ExtendWith(MockitoExtension.class)
class VueloServicioTest {
        @Mock
        private VueloRepositorio vueloRepositorio;

        @Mock
        private ReservaRepositorio reservaRepositorio;

        private VueloServicio servicio;
        private Vuelo vuelo;

        @BeforeEach
        void prepararServicio() {
                servicio = new VueloServicio(
                                vueloRepositorio,
                                reservaRepositorio,
                                new AutorizacionServicio(),
                                org.mockito.Mockito.mock(BloqueoEscrituras.class));
                vuelo = new Vuelo(
                                100,
                                LocalDateTime.of(2026, 10, 1, 10, 0),
                                "Buenos Aires",
                                "Cordoba",
                                20,
                                15,
                                5);
                when(vueloRepositorio.findById(100)).thenReturn(Optional.of(vuelo));
        }

        @Test
        void noReducePlazasPorDebajoDeLasReservasRegistradas() {
                when(reservaRepositorio.countByVueloNumeroAndClaseVuelo(100, ClaseVuelo.TURISTA))
                                .thenReturn(6L);

                assertThrows(
                                CapacidadExcedidaException.class,
                                () -> modificarVuelo(5, 5));
        }

        @Test
        void permiteCapacidadIgualALasReservasRegistradas() {
                when(reservaRepositorio.countByVueloNumeroAndClaseVuelo(100, ClaseVuelo.TURISTA))
                                .thenReturn(5L);
                when(reservaRepositorio.countByVueloNumeroAndClaseVuelo(100, ClaseVuelo.PRIMERA))
                                .thenReturn(2L);

                Vuelo modificado = modificarVuelo(5, 2);

                assertEquals(5, modificado.getPlazasTurista());
                assertEquals(2, modificado.getPlazasPrimera());
        }

        private Vuelo modificarVuelo(int plazasTurista, int plazasPrimera) {
                return servicio.modificar(
                                new Administrador("admin", "clave"),
                                100,
                                LocalDateTime.of(2026, 10, 1, 12, 0),
                                "Buenos Aires",
                                "Cordoba",
                                20,
                                plazasTurista,
                                plazasPrimera);
        }
}
