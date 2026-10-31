package com.tijetravel.tijefront.controladores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.tijetravel.tijefront.clientes.CiudadesApiCliente;
import com.tijetravel.tijefront.clientes.HotelesApiCliente;
import com.tijetravel.tijefront.clientes.VuelosApiCliente;
import com.tijetravel.tijefront.dto.CombinacionViaje;
import com.tijetravel.tijefront.dto.HotelRespuesta;
import com.tijetravel.tijefront.dto.VueloRespuesta;

@ExtendWith(MockitoExtension.class)
class InicioControladorTest {
    @Mock private VuelosApiCliente vuelosApi;
    @Mock private HotelesApiCliente hotelesApi;
        @Mock private CiudadesApiCliente ciudadesApi;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
                when(ciudadesApi.listar()).thenReturn(List.of("Buenos Aires", "Córdoba", "Goya"));
                mvc = MockMvcBuilders.standaloneSetup(new InicioControlador(vuelosApi, hotelesApi, ciudadesApi))
                .setViewResolvers(new InternalResourceViewResolver("/WEB-INF/vistas/", ".jsp"))
                .build();
    }

    @Test
    void elInicioMuestraElBuscadorSinNecesitarLaApi() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                                .andExpect(model().attributeExists("busqueda"))
                                .andExpect(model().attribute("ciudades", List.of("Buenos Aires", "Córdoba", "Goya")));
        verifyNoInteractions(vuelosApi, hotelesApi);
    }

        @Test
        void conservaElInicioSiNoSePuedenConsultarCiudades() throws Exception {
                when(ciudadesApi.listar()).thenThrow(new org.springframework.web.client.ResourceAccessException("Sin conexión"));

                mvc.perform(get("/"))
                                .andExpect(status().isOk())
                                .andExpect(model().attribute("ciudades", List.of()));
        }

    @Test
    void muestraLasOpcionesFiltradasQueDevuelveElBackend() throws Exception {
        VueloRespuesta correcto = vuelo(101, "Buenos Aires", "Córdoba", LocalDate.of(2027, 2, 1));
        HotelRespuesta hotel = hotel(1, "Cordoba");
        when(vuelosApi.buscar("Buenos Aires", "Córdoba", LocalDate.of(2027, 2, 1), 2, false))
                .thenReturn(List.of(correcto));
        when(hotelesApi.buscar("Córdoba", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 4), 2))
                .thenReturn(List.of(hotel));

        mvc.perform(busqueda())
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attribute("vuelosEncontrados", List.of(correcto)))
                .andExpect(model().attribute("avisoVuelos", (Object) null))
                .andExpect(model().attribute("hotelesEncontrados", List.of(hotel)))
                .andExpect(model().attribute("busquedaRealizada", true));
    }

    @Test
    void recomiendaLasSeisCombinacionesMasBaratasYGuardaElResto() throws Exception {
        VueloRespuesta caro = vuelo(301, "Buenos Aires", "Córdoba", LocalDate.of(2027, 2, 1));
        caro.setPrecio(250000);
        VueloRespuesta barato = vuelo(302, "Buenos Aires", "Córdoba", LocalDate.of(2027, 2, 1));
        barato.setPrecio(150000);
        VueloRespuesta medio = vuelo(303, "Buenos Aires", "Córdoba", LocalDate.of(2027, 2, 1));
        medio.setPrecio(200000);
        HotelRespuesta economico = hotel(1, "Córdoba");
        economico.setPrecioNochePersona(25000);
        HotelRespuesta lujoso = hotel(2, "Córdoba");
        lujoso.setPrecioNochePersona(55000);
        HotelRespuesta comun = hotel(3, "Córdoba");
        comun.setPrecioNochePersona(40000);
        when(vuelosApi.buscar("Buenos Aires", "Córdoba", LocalDate.of(2027, 2, 1), 2, false))
                .thenReturn(List.of(caro, barato, medio));
        when(hotelesApi.buscar("Córdoba", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 4), 2))
                .thenReturn(List.of(economico, lujoso, comun));

        MvcResult resultado = mvc.perform(busqueda()).andExpect(status().isOk()).andReturn();

        List<?> recomendados = (List<?>) resultado.getModelAndView().getModel().get("recomendados");
        List<?> otras = (List<?>) resultado.getModelAndView().getModel().get("otrasCombinaciones");
        assertEquals(6, recomendados.size());
        assertEquals(3, otras.size());
        CombinacionViaje primera = (CombinacionViaje) recomendados.get(0);
        // 150.000 de vuelo + 3 noches x 25.000 de hotel.
        assertEquals(302, primera.getVuelo().getNumero());
        assertEquals(1, primera.getHotel().getCodigo());
        assertEquals(225000, primera.getPrecioPorPersona());
        CombinacionViaje ultima = (CombinacionViaje) otras.get(2);
        assertEquals(415000, ultima.getPrecioPorPersona());
    }

    @Test
    void siNoHayVueloEnLaFechaMuestraLosMasCercanosConSuHotel() throws Exception {
        VueloRespuesta cercano = vuelo(201, "Buenos Aires", "Cordoba", LocalDate.of(2027, 2, 2));
        VueloRespuesta lejano = vuelo(202, "Buenos Aires", "Cordoba", LocalDate.of(2027, 2, 10));
        HotelRespuesta hotel = hotel(1, "Córdoba");
        when(vuelosApi.buscar("Buenos Aires", "Córdoba", LocalDate.of(2027, 2, 1), 2, false))
                .thenReturn(List.of());
        when(vuelosApi.buscar("Buenos Aires", "Córdoba", LocalDate.of(2027, 2, 1), 2, true))
                .thenReturn(List.of(cercano, lejano));
        when(hotelesApi.buscar("Córdoba", LocalDate.of(2027, 2, 2), LocalDate.of(2027, 2, 5), 2))
                .thenReturn(List.of(hotel));

        MvcResult resultado = mvc.perform(busqueda())
                .andExpect(status().isOk())
                .andExpect(model().attribute("vuelosEncontrados", List.of(cercano, lejano)))
                .andExpect(model().attribute("avisoVuelos",
                        "No hay vuelos el 01/02/2027. Te mostramos los más cercanos."))
                .andReturn();

        // El hotel se reserva desde el dia del vuelo cercano, con las mismas noches pedidas.
        List<?> recomendados = (List<?>) resultado.getModelAndView().getModel().get("recomendados");
        assertEquals(1, recomendados.size());
        CombinacionViaje combinacion = (CombinacionViaje) recomendados.get(0);
        assertEquals(201, combinacion.getVuelo().getNumero());
        assertEquals(LocalDate.of(2027, 2, 2), combinacion.getFechaLlegada());
        assertEquals(LocalDate.of(2027, 2, 5), combinacion.getFechaPartida());
    }

    @Test
    void sinFechasBuscaLosProximosVuelosConTresNochesDeHotel() throws Exception {
        VueloRespuesta proximo = vuelo(401, "Buenos Aires", "Córdoba", LocalDate.of(2027, 3, 10));
        HotelRespuesta hotel = hotel(1, "Córdoba");
        when(vuelosApi.buscar("Buenos Aires", "Córdoba", null, 2, false)).thenReturn(List.of(proximo));
        when(hotelesApi.buscar("Córdoba", LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 13), 2))
                .thenReturn(List.of(hotel));

        mvc.perform(get("/buscar")
                .param("origen", "Buenos Aires").param("destino", "Córdoba").param("personas", "2"))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("vuelosEncontrados", List.of(proximo)))
                .andExpect(model().attribute("hotelesEncontrados", List.of(hotel)))
                .andExpect(model().attribute("avisoVuelos",
                        "Como no elegiste fecha, te mostramos los próximos vuelos."))
                .andExpect(model().attribute("hotelLlegada", "10/03/2027"))
                .andExpect(model().attribute("cantidadNoches", 3L));
    }

    @Test
    void laPartidaSinLlegadaEsUnError() throws Exception {
        mvc.perform(get("/buscar")
                .param("origen", "Buenos Aires").param("destino", "Córdoba")
                .param("fechaPartida", "2027-02-04").param("personas", "2"))
                .andExpect(view().name("inicio"))
                .andExpect(model().attributeHasFieldErrors("busqueda", "fechaLlegada"));
        verifyNoInteractions(vuelosApi, hotelesApi);
    }

    @Test
    void noConsultaLaApiCuandoLasFechasSonInvalidas() throws Exception {
        mvc.perform(get("/buscar")
                .param("origen", "Buenos Aires").param("destino", "Córdoba")
                .param("fechaLlegada", "2027-02-04").param("fechaPartida", "2027-02-01")
                .param("personas", "2"))
                .andExpect(view().name("inicio"))
                .andExpect(model().attributeHasFieldErrors("busqueda", "fechaPartida"));
        verifyNoInteractions(vuelosApi, hotelesApi);
    }

    @Test
    void noConsultaLaApiSiLasCiudadesCoincidenOLaCantidadEsInvalida() throws Exception {
        mvc.perform(get("/buscar")
                .param("origen", "Cordoba").param("destino", "Córdoba")
                .param("fechaLlegada", "2027-02-01").param("fechaPartida", "2027-02-04")
                .param("personas", "0"))
                .andExpect(view().name("inicio"))
                .andExpect(model().attributeHasFieldErrors("busqueda", "destino", "personas"));
        verifyNoInteractions(vuelosApi, hotelesApi);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder busqueda() {
        return get("/buscar")
                .param("origen", "Buenos Aires").param("destino", "Córdoba")
                .param("fechaLlegada", "2027-02-01").param("fechaPartida", "2027-02-04")
                .param("personas", "2");
    }

    private VueloRespuesta vuelo(int numero, String origen, String destino, LocalDate fecha) {
        VueloRespuesta vuelo = new VueloRespuesta();
        vuelo.setNumero(numero);
        vuelo.setOrigen(origen);
        vuelo.setDestino(destino);
        vuelo.setFechaYHora(LocalDateTime.of(fecha, java.time.LocalTime.NOON));
        return vuelo;
    }

    private HotelRespuesta hotel(int codigo, String ciudad) {
        HotelRespuesta hotel = new HotelRespuesta();
        hotel.setCodigo(codigo);
        hotel.setCiudad(ciudad);
        return hotel;
    }

}
