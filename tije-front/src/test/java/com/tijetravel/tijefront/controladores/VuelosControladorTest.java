package com.tijetravel.tijefront.controladores;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.tijetravel.tijefront.clientes.VuelosApiCliente;
import com.tijetravel.tijefront.dto.DisponibilidadVueloRespuesta;
import com.tijetravel.tijefront.dto.VueloRespuesta;

@ExtendWith(MockitoExtension.class)
class VuelosControladorTest {
    @Mock
    private VuelosApiCliente vuelosApi;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new VuelosControlador(vuelosApi))
                .setControllerAdvice(new ManejadorErrores())
                .setViewResolvers(new InternalResourceViewResolver("/WEB-INF/vistas/", ".jsp"))
                .build();
    }

    @Test
    void entregaElCatalogoALaVista() throws Exception {
        var vuelos = List.of(new VueloRespuesta());
        when(vuelosApi.listar()).thenReturn(vuelos);
        mvc.perform(get("/vuelos"))
                .andExpect(status().isOk()).andExpect(view().name("vuelos/lista"))
                .andExpect(model().attribute("vuelos", vuelos));
    }

    @Test
    void muestraLasPlazasLibresDelBackendSinUsarLaCapacidadComoDisponibilidad() throws Exception {
        VueloRespuesta vuelo = new VueloRespuesta();
        vuelo.setPlazasTurista(80);
        vuelo.setPlazasPrimera(20);
        DisponibilidadVueloRespuesta turista = new DisponibilidadVueloRespuesta();
        turista.setPlazasDisponibles(3);
        DisponibilidadVueloRespuesta primera = new DisponibilidadVueloRespuesta();
        primera.setPlazasDisponibles(0);
        when(vuelosApi.buscar(101)).thenReturn(vuelo);
        when(vuelosApi.consultarDisponibilidad(101, "TURISTA")).thenReturn(turista);
        when(vuelosApi.consultarDisponibilidad(101, "PRIMERA")).thenReturn(primera);
        mvc.perform(get("/vuelos/101"))
                .andExpect(status().isOk()).andExpect(view().name("vuelos/detalle"))
                .andExpect(model().attribute("vuelo", vuelo))
                .andExpect(model().attribute("disponibilidadTurista", turista))
                .andExpect(model().attribute("disponibilidadPrimera", primera));
    }

    @Test
    void responde404SiElVueloNoExiste() throws Exception {
        when(vuelosApi.buscar(999)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
        mvc.perform(get("/vuelos/999"))
                .andExpect(status().isNotFound()).andExpect(view().name("error"))
                .andExpect(model().attribute("mensaje", "No encontramos el recurso solicitado."));
    }

    @Test
    void rechazaUnNumeroMalEscritoAntesDeLlamarAlBackend() throws Exception {
        mvc.perform(get("/vuelos/abc"))
                .andExpect(status().isBadRequest()).andExpect(view().name("error"));
        verifyNoInteractions(vuelosApi);
    }

    @Test
    void informaSiElBackendNoResponde() throws Exception {
        when(vuelosApi.listar()).thenThrow(new ResourceAccessException("Conexion rechazada"));
        mvc.perform(get("/vuelos"))
                .andExpect(status().isServiceUnavailable()).andExpect(view().name("error"))
                .andExpect(model().attribute("mensaje",
                        "No pudimos consultar los datos en este momento. Intentá nuevamente en unos minutos."));
    }
}
