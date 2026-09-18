package com.tijetravel.tijefront.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.tijetravel.tijefront.clientes.HotelesApiCliente;
import com.tijetravel.tijefront.dto.DisponibilidadHotelRespuesta;
import com.tijetravel.tijefront.dto.HotelRespuesta;

@ExtendWith(MockitoExtension.class)
class HotelesControladorTest {
    @Mock
    private HotelesApiCliente hotelesApi;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new HotelesControlador(hotelesApi), new InicioControlador())
                .setControllerAdvice(new ManejadorErrores())
                .setViewResolvers(new InternalResourceViewResolver("/WEB-INF/vistas/", ".jsp"))
                .build();
    }

    @Test
    void elInicioNoNecesitaConexionAlBackend() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("inicio"));
    }

    @Test
    void pasaLosHotelesALaJsp() throws Exception {
        when(hotelesApi.listar()).thenReturn(List.of(hotel()));
        mvc.perform(get("/hoteles"))
                .andExpect(status().isOk()).andExpect(view().name("hoteles/lista"))
                .andExpect(model().attributeExists("hoteles"));
    }

    @Test
    void consultaLaDisponibilidadConFechasValidas() throws Exception {
        when(hotelesApi.buscar(1)).thenReturn(hotel());
        when(hotelesApi.consultarDisponibilidad(1, LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 4)))
                .thenReturn(new DisponibilidadHotelRespuesta());
        mvc.perform(get("/hoteles/1/disponibilidad")
                .param("fechaLlegada", "2027-02-01").param("fechaPartida", "2027-02-04"))
                .andExpect(status().isOk()).andExpect(view().name("hoteles/detalle"))
                .andExpect(model().hasNoErrors()).andExpect(model().attributeExists("disponibilidad"));
    }

    @Test
    void pideLasFechasFaltantesSinConsultarDisponibilidad() throws Exception {
        when(hotelesApi.buscar(1)).thenReturn(hotel());
        mvc.perform(get("/hoteles/1/disponibilidad"))
                .andExpect(view().name("hoteles/detalle"))
                .andExpect(model().attributeHasFieldErrors("consulta", "fechaLlegada", "fechaPartida"));
        verify(hotelesApi, never()).consultarDisponibilidad(any(), any(), any());
    }

    @Test
    void rechazaFechasInvertidasSinConsultarDisponibilidad() throws Exception {
        when(hotelesApi.buscar(1)).thenReturn(hotel());
        mvc.perform(get("/hoteles/1/disponibilidad")
                .param("fechaLlegada", "2027-02-04").param("fechaPartida", "2027-02-01"))
                .andExpect(model().attributeHasFieldErrors("consulta", "fechaPartida"))
                .andExpect(model().attributeDoesNotExist("disponibilidad"));
        verify(hotelesApi, never()).consultarDisponibilidad(any(), any(), any());
    }

    @Test
    void rechazaFechasMalEscritasSinConsultarDisponibilidad() throws Exception {
        when(hotelesApi.buscar(1)).thenReturn(hotel());
        mvc.perform(get("/hoteles/1/disponibilidad")
                .param("fechaLlegada", "no-es-fecha").param("fechaPartida", "2027-02-04"))
                .andExpect(view().name("hoteles/detalle"))
                .andExpect(model().attributeHasFieldErrors("consulta", "fechaLlegada"));
        verify(hotelesApi, never()).consultarDisponibilidad(any(), any(), any());
    }

    @Test
    void muestraUnaPaginaClaraCuandoElBackendNoResponde() throws Exception {
        when(hotelesApi.listar()).thenThrow(new ResourceAccessException("Conexion rechazada"));
        mvc.perform(get("/hoteles"))
                .andExpect(status().isServiceUnavailable()).andExpect(view().name("error"))
                .andExpect(model().attributeExists("mensaje"));
    }

    private HotelRespuesta hotel() {
        HotelRespuesta hotel = new HotelRespuesta();
        hotel.setCodigo(1);
        hotel.setNombre("Hotel Centro");
        hotel.setCapacidadTotal(35);
        return hotel;
    }
}
