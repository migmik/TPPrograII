package com.tijetravel.tijeback.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import com.tijetravel.tijeback.DnisPrueba;
import com.tijetravel.tijeback.RolesPrueba;
import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.excepciones.CapacidadExcedidaException;
import com.tijetravel.tijeback.modelos.Empleado;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;
import com.tijetravel.tijeback.servicios.impl.VueloServicioImpl;

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
                servicio = new VueloServicioImpl(
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
                lenient().when(vueloRepositorio.findById(100)).thenReturn(Optional.of(vuelo));
        }

        @Test
        void noReducePlazasPorDebajoDeLasReservasRegistradas() {
                when(reservaRepositorio.countByVueloNumeroAndClaseVueloAndFechaBajaIsNull(100, ClaseVuelo.TURISTA))
                                .thenReturn(6L);

                try {
                        modificarVuelo(5, 5);
                        fail("Se esperaba CapacidadExcedidaException");
                } catch (CapacidadExcedidaException esperada) {
                        // Es la excepcion que se esperaba.
                }
        }

        @Test
        void permiteCapacidadIgualALasReservasRegistradas() {
                when(reservaRepositorio.countByVueloNumeroAndClaseVueloAndFechaBajaIsNull(100, ClaseVuelo.TURISTA))
                                .thenReturn(5L);
                when(reservaRepositorio.countByVueloNumeroAndClaseVueloAndFechaBajaIsNull(100, ClaseVuelo.PRIMERA))
                                .thenReturn(2L);

                Vuelo modificado = modificarVuelo(5, 2);

                assertEquals(5, modificado.getPlazasTurista());
                assertEquals(2, modificado.getPlazasPrimera());
        }

        @Test
        void sugiereComoMaximoTresVuelosEmpezandoPorLosMasCercanos() {
                List<Vuelo> otrosDias = new ArrayList<>();
                otrosDias.add(vueloDelDia(201, LocalDate.of(2026, 12, 20)));
                otrosDias.add(vueloDelDia(202, LocalDate.of(2026, 12, 11)));
                otrosDias.add(vueloDelDia(203, LocalDate.of(2026, 12, 1)));
                otrosDias.add(vueloDelDia(204, LocalDate.of(2026, 12, 8)));
                otrosDias.add(vueloDelDia(205, LocalDate.of(2027, 1, 30)));
                when(vueloRepositorio.buscarOtrosDias(any(), any(), any(), any(), any(), any(), anyInt()))
                                .thenReturn(otrosDias);

                List<Vuelo> sugeridos = servicio.sugerirOtrosDias("Buenos Aires", "Cordoba",
                                LocalDate.of(2026, 12, 10), 2);

                assertEquals(3, sugeridos.size());
                assertEquals(202, sugeridos.get(0).getNumero());
                assertEquals(204, sugeridos.get(1).getNumero());
                assertEquals(203, sugeridos.get(2).getNumero());
        }

        @Test
        void sinFechaPideLosTresProximosVuelos() {
                List<Vuelo> proximos = new ArrayList<>();
                proximos.add(vueloDelDia(301, LocalDate.of(2026, 12, 1)));
                when(vueloRepositorio.buscarProximos(any(), any(), any(), any(), anyInt(),
                                eq(PageRequest.of(0, 3))))
                                .thenReturn(proximos);

                List<Vuelo> encontrados = servicio.buscarProximos("Buenos Aires", "Cordoba", 2);

                assertEquals(1, encontrados.size());
                assertEquals(301, encontrados.get(0).getNumero());
        }

        private Vuelo vueloDelDia(int numero, LocalDate dia) {
                return new Vuelo(numero, dia.atTime(10, 0), "Buenos Aires", "Cordoba", 20, 15, 5);
        }

        private Vuelo modificarVuelo(int plazasTurista, int plazasPrimera) {
                return servicio.modificar(
                                new Empleado("admin", "clave", DnisPrueba.siguiente(), RolesPrueba.administrador()),
                                100,
                                LocalDateTime.of(2026, 10, 1, 12, 0),
                                "Buenos Aires",
                                "Cordoba",
                                20,
                                plazasTurista,
                                plazasPrimera,
                                150000);
        }
}
