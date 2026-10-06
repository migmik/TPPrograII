package com.tijetravel.tijefront.controladores;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import com.tijetravel.tijefront.clientes.*;
import com.tijetravel.tijefront.dto.*;
import com.tijetravel.tijefront.formularios.GuardarReservaFormulario;

@SpringBootTest
@AutoConfigureMockMvc
class ReservasControladorTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private AutenticacionApiCliente autenticacionApi;
    @MockitoBean private ReservasApiCliente reservasApi;
    @MockitoBean private TuristasApiCliente turistasApi;
    @MockitoBean private HotelesApiCliente hotelesApi;
    @MockitoBean private VuelosApiCliente vuelosApi;

    @Test
    void sinSesionNoConsultaNiModificaReservas() throws Exception {
        for (String ruta : List.of("/reservas", "/reservas/nueva", "/reservas/1", "/reservas/1/editar", "/reservas/1/eliminar")) {
            mvc.perform(get(ruta)).andExpect(redirectedUrl("/login"));
        }
        for (String ruta : List.of("/reservas", "/reservas/1/editar", "/reservas/1/eliminar")) {
            mvc.perform(post(ruta).with(csrf())).andExpect(redirectedUrl("/login"));
        }
        verifyNoInteractions(reservasApi, turistasApi, hotelesApi, vuelosApi);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMINISTRADOR", "VENDEDOR"})
    void ambosRolesCreanConFechasYCodigosDelFormulario(String rol) throws Exception {
        preparar(rol);
        doAnswer(inv -> {
            GuardarReservaFormulario datos = inv.getArgument(0);
            assertEquals(2, datos.getCodigoTurista());
            assertEquals(100, datos.getNumeroVuelo());
            assertEquals(LocalDate.of(2027, 1, 10), datos.getFechaLlegada());
            return null;
        }).when(reservasApi).crear(any());
        mvc.perform(formulario("/reservas")).andExpect(redirectedUrl("/reservas"))
                .andExpect(flash().attribute("mensajeExito", "Reserva guardada correctamente."));
        verify(reservasApi).crear(any());
    }

    @Test
    void clienteConsultaSusReservasYCreaPeroNoEditaNiElimina() throws Exception {
        preparar("CLIENTE");
        var grupo = List.of(reserva());
        when(reservasApi.listar()).thenReturn(grupo);
        mvc.perform(get("/reservas").session(sesion()))
                .andExpect(model().attribute("reservas", grupo)).andExpect(model().attribute("puedeGestionar", false));
        mvc.perform(get("/reservas/nueva").session(sesion()))
                .andExpect(view().name("reservas/formulario"));
        mvc.perform(formulario("/reservas")).andExpect(redirectedUrl("/reservas"));
        for (String ruta : List.of("/reservas/1/editar", "/reservas/1/eliminar")) {
            mvc.perform(get(ruta).session(sesion())).andExpect(status().isForbidden());
        }
        for (String ruta : List.of("/reservas/1/editar", "/reservas/1/eliminar")) {
            mvc.perform(formulario(ruta)).andExpect(status().isForbidden());
        }
        verify(reservasApi).crear(any());
        verify(reservasApi, never()).modificar(any(), any());
        verify(reservasApi, never()).eliminar(any());
    }

    @Test
    void reservaDesdeElBuscadorLlegaConVueloHotelYFechasPrecargados() throws Exception {
        preparar("CLIENTE");
        var resultado = mvc.perform(get("/reservas/nueva").session(sesion())
                .param("numeroVuelo", "100").param("codigoHotel", "3")
                .param("fechaLlegada", "2027-01-10").param("fechaPartida", "2027-01-12"))
                .andExpect(view().name("reservas/formulario")).andReturn();
        var formulario = (GuardarReservaFormulario) resultado.getModelAndView().getModel().get("reserva");
        assertEquals(100, formulario.getNumeroVuelo());
        assertEquals(3, formulario.getCodigoHotel());
        assertEquals(LocalDate.of(2027, 1, 10), formulario.getFechaLlegada());
        assertEquals("TURISTA", formulario.getClaseVuelo());
    }

    @Test
    void unClienteNoObtieneDetallesDeUnaReservaAjena() throws Exception {
        preparar("CLIENTE");
        when(reservasApi.buscar(1)).thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));
        mvc.perform(get("/reservas/1").session(sesion())).andExpect(status().isForbidden());
        verifyNoInteractions(turistasApi, hotelesApi, vuelosApi);
    }

    @Test
    void erroresDeFechasYEnumeracionesNoSeEnvianAlBackend() throws Exception {
        preparar("VENDEDOR");
        mvc.perform(formulario("/reservas").with(r -> { r.setParameter("fechaPartida", "2027-01-10"); return r; }))
                .andExpect(model().attributeHasFieldErrors("reserva", "fechaPartida"));
        mvc.perform(formulario("/reservas").with(r -> { r.setParameter("fechaLlegada", "invalida"); r.setParameter("claseVuelo", "OTRA"); return r; }))
                .andExpect(model().attributeHasFieldErrors("reserva", "fechaLlegada", "claseVuelo"));
        mvc.perform(post("/reservas").session(sesion()).with(csrf()))
                .andExpect(model().attributeHasFieldErrors("reserva", "codigoTurista", "fechaLlegada", "fechaPartida"));
        verify(reservasApi, never()).crear(any());
    }

    @Test
    void edicionRecuperaTodosLosCamposYConservaSucursalHistoricaEnLaVista() throws Exception {
        preparar("VENDEDOR");
        when(reservasApi.buscar(1)).thenReturn(reserva());
        var resultado = mvc.perform(get("/reservas/1/editar").session(sesion()))
                .andExpect(view().name("reservas/formulario")).andReturn();
        var formulario = (GuardarReservaFormulario) resultado.getModelAndView().getModel().get("reserva");
        assertEquals(LocalDate.of(2027, 1, 10), formulario.getFechaLlegada());
        assertEquals("PRIMERA", formulario.getClaseVuelo());
        var actual = (ReservaRespuesta) resultado.getModelAndView().getModel().get("actual");
        assertEquals(7, actual.getCodigoSucursalContratacion());
        mvc.perform(formulario("/reservas/1/editar")).andExpect(redirectedUrl("/reservas"));
        verify(reservasApi).modificar(eq(1), any());
        verify(reservasApi, never()).crear(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 403, 409})
    void rechazosDelBackendConservanElFormulario(int estado) throws Exception {
        preparar("ADMINISTRADOR");
        doThrow(new HttpClientErrorException(HttpStatus.valueOf(estado))).when(reservasApi).crear(any());
        mvc.perform(formulario("/reservas")).andExpect(status().is(estado))
                .andExpect(view().name("reservas/formulario"))
                .andExpect(model().attributeExists("errorOperacion", "turistas", "hoteles", "vuelos"));
    }

    @Test
    void noReintentaUnaEscrituraSiLaConexionFalla() throws Exception {
        preparar("VENDEDOR");
        doThrow(new ResourceAccessException("Timeout")).when(reservasApi).crear(any());
        mvc.perform(formulario("/reservas")).andExpect(status().isServiceUnavailable())
                .andExpect(model().attributeExists("errorOperacion"));
        verify(reservasApi, times(1)).crear(any());
    }

    @Test
    void confirmarPorGetNoBorraYPostEliminaUnaSolaVez() throws Exception {
        preparar("VENDEDOR");
        when(reservasApi.buscar(1)).thenReturn(reserva());
        mvc.perform(get("/reservas/1/eliminar").session(sesion())).andExpect(view().name("reservas/eliminar"));
        verify(reservasApi, never()).eliminar(any());
        mvc.perform(post("/reservas/1/eliminar").session(sesion()).with(csrf()))
                .andExpect(redirectedUrl("/reservas"));
        verify(reservasApi).eliminar(1);
    }

    @Test
    void todasLasEscriturasRequierenCsrf() throws Exception {
        for (String ruta : List.of("/reservas", "/reservas/1/editar", "/reservas/1/eliminar")) {
            mvc.perform(post(ruta).session(sesion())).andExpect(status().isForbidden());
        }
        verifyNoInteractions(reservasApi);
    }

    @Test
    void sesionVencidaSeInvalida() throws Exception {
        MockHttpSession sesion = sesion();
        when(autenticacionApi.obtenerSesion()).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        mvc.perform(get("/reservas").session(sesion)).andExpect(redirectedUrl("/login?sesionVencida"));
        assertTrue(sesion.isInvalid());
    }

    private void preparar(String rol) {
        SesionRespuesta usuario = new SesionRespuesta();
        usuario.setRol(rol);
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario);
    }

    private MockHttpSession sesion() {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuarioActual", new SesionRespuesta());
        return sesion;
    }

    private MockHttpServletRequestBuilder formulario(String ruta) {
        return post(ruta).session(sesion()).with(csrf()).param("codigoTurista", "2").param("numeroVuelo", "100")
                .param("codigoHotel", "3").param("claseVuelo", "PRIMERA").param("tipoHospedaje", "MEDIA_PENSION")
                .param("fechaLlegada", "2027-01-10").param("fechaPartida", "2027-01-12");
    }

    private ReservaRespuesta reserva() {
        ReservaRespuesta reserva = new ReservaRespuesta();
        reserva.setCodigo(1);
        reserva.setCodigoTurista(2);
        reserva.setNumeroVuelo(100);
        reserva.setCodigoHotel(3);
        reserva.setClaseVuelo("PRIMERA");
        reserva.setTipoHospedaje("MEDIA_PENSION");
        reserva.setFechaLlegada(LocalDate.of(2027, 1, 10));
        reserva.setFechaPartida(LocalDate.of(2027, 1, 12));
        reserva.setCodigoSucursalContratacion(7);
        return reserva;
    }
}
