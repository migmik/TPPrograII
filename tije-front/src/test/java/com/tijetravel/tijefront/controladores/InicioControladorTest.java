package com.tijetravel.tijefront.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.tijetravel.tijefront.clientes.HotelesApiCliente;
import com.tijetravel.tijefront.clientes.VuelosApiCliente;
import com.tijetravel.tijefront.dto.DisponibilidadHotelRespuesta;
import com.tijetravel.tijefront.dto.DisponibilidadVueloRespuesta;
import com.tijetravel.tijefront.dto.HotelRespuesta;
import com.tijetravel.tijefront.dto.VueloRespuesta;

@ExtendWith(MockitoExtension.class)
class InicioControladorTest {
    @Mock private VuelosApiCliente vuelosApi;
    @Mock private HotelesApiCliente hotelesApi;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new InicioControlador(vuelosApi, hotelesApi))
                .setViewResolvers(new InternalResourceViewResolver("/WEB-INF/vistas/", ".jsp"))
                .build();
    }

    @Test
    void elInicioMuestraElBuscadorSinNecesitarLaApi() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attributeExists("busqueda"));
        verifyNoInteractions(vuelosApi, hotelesApi);
    }

    @Test
    void buscaSoloOpcionesConPlazasParaTodasLasPersonas() throws Exception {
        VueloRespuesta correcto = vuelo(101, "Buenos Aires", "Córdoba", LocalDate.of(2027, 2, 1));
        VueloRespuesta otroDestino = vuelo(102, "Buenos Aires", "Salta", LocalDate.of(2027, 2, 1));
        VueloRespuesta sinPlazas = vuelo(103, "Buenos Aires", "Córdoba", LocalDate.of(2027, 2, 1));
        HotelRespuesta hotel = hotel(1, "Cordoba");
        HotelRespuesta lleno = hotel(2, "Córdoba");
        when(vuelosApi.listar()).thenReturn(List.of(correcto, otroDestino, sinPlazas));
        when(vuelosApi.consultarDisponibilidad(101, "TURISTA")).thenReturn(plazasVuelo(3));
        when(vuelosApi.consultarDisponibilidad(103, "TURISTA")).thenReturn(plazasVuelo(1));
        when(hotelesApi.listar()).thenReturn(List.of(hotel, lleno));
        when(hotelesApi.consultarDisponibilidad(1, LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 4)))
                .thenReturn(plazasHotel(3));
        when(hotelesApi.consultarDisponibilidad(2, LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 4)))
                .thenReturn(plazasHotel(0));

        mvc.perform(busqueda())
                .andExpect(status().isOk())
                .andExpect(view().name("inicio"))
                .andExpect(model().attribute("vuelosEncontrados", List.of(correcto)))
                .andExpect(model().attribute("hotelesEncontrados", List.of(hotel)))
                .andExpect(model().attribute("busquedaRealizada", true));
        verify(vuelosApi, never()).consultarDisponibilidad(102, "TURISTA");
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

    private DisponibilidadVueloRespuesta plazasVuelo(int cantidad) {
        DisponibilidadVueloRespuesta respuesta = new DisponibilidadVueloRespuesta();
        respuesta.setPlazasDisponibles(cantidad);
        return respuesta;
    }

    private DisponibilidadHotelRespuesta plazasHotel(int cantidad) {
        DisponibilidadHotelRespuesta respuesta = new DisponibilidadHotelRespuesta();
        respuesta.setPlazasDisponibles(cantidad);
        return respuesta;
    }
}
