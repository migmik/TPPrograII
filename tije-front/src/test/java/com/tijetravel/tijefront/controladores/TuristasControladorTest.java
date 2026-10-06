package com.tijetravel.tijefront.controladores;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.tijetravel.tijefront.formularios.GuardarTuristaFormulario;

@SpringBootTest
@AutoConfigureMockMvc
class TuristasControladorTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AutenticacionApiCliente autenticacionApi;
    @MockitoBean
    private TuristasApiCliente turistasApi;
    @MockitoBean
    private SucursalesApiCliente sucursalesApi;

    @Test
    void sinSesionNoConsultaNiModificaDatos() throws Exception {
        for (String ruta : List.of("/turistas", "/turistas/nuevo", "/turistas/2", "/turistas/2/editar",
                "/turistas/2/eliminar")) {
            mvc.perform(get(ruta)).andExpect(redirectedUrl("/login"));
        }
        mvc.perform(post("/turistas").with(csrf())).andExpect(redirectedUrl("/login"));
        verifyNoInteractions(turistasApi, sucursalesApi);
    }

    @ParameterizedTest
    @ValueSource(strings = { "ADMINISTRADOR", "VENDEDOR" })
    void ambosRolesPuedenCrearTitulares(String rol) throws Exception {
        preparar(rol);
        mvc.perform(formulario("/turistas").param("codigoSucursal", "1"))
                .andExpect(redirectedUrl("/turistas"));
        verify(turistasApi).crear(any());
    }

    @Test
    void clienteSoloConsultaLoQueDevuelveElBackend() throws Exception {
        preparar("CLIENTE");
        List<TuristaRespuesta> grupo = List.of(turista(false));
        when(turistasApi.listarDetalles()).thenReturn(grupo);
        mvc.perform(get("/turistas").session(sesion())).andExpect(model().attribute("turistas", grupo))
                .andExpect(model().attribute("puedeGestionar", false));
        for (String ruta : List.of("/turistas/nuevo", "/turistas/2/editar", "/turistas/2/eliminar")) {
            mvc.perform(get(ruta).session(sesion())).andExpect(status().isForbidden());
        }
        for (String ruta : List.of("/turistas", "/turistas/2/editar", "/turistas/2/eliminar")) {
            mvc.perform(formulario(ruta)).andExpect(status().isForbidden());
        }
        verify(turistasApi, never()).crear(any());
        verify(turistasApi, never()).modificar(any(), any());
        verify(turistasApi, never()).eliminar(any());
    }

    @Test
    void buscaTuristasPorDni() throws Exception {
        preparar("ADMINISTRADOR");
        List<TuristaRespuesta> resultado = List.of(turista(true));
        when(turistasApi.listarDetalles("12345678", null)).thenReturn(resultado);

        mvc.perform(get("/turistas").param("dni", "12345678").session(sesion()))
                .andExpect(view().name("turistas/lista"))
                .andExpect(model().attribute("turistas", resultado))
                .andExpect(model().attribute("dniBusqueda", "12345678"));

        verify(turistasApi).listarDetalles("12345678", null);
    }

    @Test
    void filtraTuristasFamiliares() throws Exception {
        preparar("ADMINISTRADOR");
        List<TuristaRespuesta> resultado = List.of(turista(false));
        when(turistasApi.listarDetalles(null, false)).thenReturn(resultado);

        mvc.perform(get("/turistas").param("titular", "false").session(sesion()))
                .andExpect(view().name("turistas/lista"))
                .andExpect(model().attribute("turistas", resultado))
                .andExpect(model().attribute("titularBusqueda", "false"));

        verify(turistasApi).listarDetalles(null, false);
    }

    @Test
    void titularRequiereSucursalYFamiliarLaHereda() throws Exception {
        preparar("VENDEDOR");
        mvc.perform(formulario("/turistas")).andExpect(model().attributeHasFieldErrors("turista", "codigoSucursal"));
        mvc.perform(formulario("/turistas").param("codigoTitular", "1").param("codigoSucursal", "1"))
                .andExpect(model().attributeHasFieldErrors("turista", "codigoSucursal"));
        verify(turistasApi, never()).crear(any());
        mvc.perform(formulario("/turistas").param("codigoTitular", "1"))
                .andExpect(redirectedUrl("/turistas"));
        verify(turistasApi).crear(any());
    }

    @Test
    void selectorOfreceSoloTitulares() throws Exception {
        preparar("ADMINISTRADOR");
        TuristaResumen titular = new TuristaResumen();
        titular.setTitular(true);
        TuristaResumen familiar = new TuristaResumen();
        when(turistasApi.listar(true)).thenReturn(List.of(titular));
        mvc.perform(get("/turistas/nuevo").session(sesion()))
                .andExpect(model().attribute("titulares", List.of(titular)));
        verify(turistasApi).listar(true);
    }

    @Test
    void editarFamiliarConservaTitularYSucursalAunqueManipulenFormulario() throws Exception {
        preparar("VENDEDOR");
        when(turistasApi.buscar(2)).thenReturn(turista(false));
        doAnswer(inv -> {
            GuardarTuristaFormulario datos = inv.getArgument(1);
            assertEquals("76543210", datos.getDni());
            assertEquals(1, datos.getCodigoTitular());
            assertEquals(3, datos.getCodigoSucursal());
            return null;
        }).when(turistasApi).modificar(eq(2), any());
        mvc.perform(formulario("/turistas/2/editar").param("codigoSucursal", "999").param("codigoTitular", "999"))
                .andExpect(redirectedUrl("/turistas"));
        verify(turistasApi).modificar(eq(2), any());
    }

    @Test
    void erroresDeCamposConservanElFormulario() throws Exception {
        preparar("ADMINISTRADOR");
        mvc.perform(post("/turistas").session(sesion()).with(csrf()).param("email", "invalido")
                .param("codigoSucursal", "abc"))
                .andExpect(view().name("turistas/formulario"))
                .andExpect(model().attributeHasFieldErrors("turista", "nombre", "email", "codigoSucursal"));
        verify(turistasApi, never()).crear(any());
    }

    @Test
    void emailDuplicadoSeMuestraEnElFormulario() throws Exception {
        preparar("VENDEDOR");
        doThrow(new HttpClientErrorException(HttpStatus.CONFLICT)).when(turistasApi).crear(any());
        mvc.perform(formulario("/turistas").param("codigoSucursal", "1"))
                .andExpect(status().isConflict()).andExpect(view().name("turistas/formulario"))
                .andExpect(model().attributeExists("errorOperacion", "sucursales", "titulares"));
    }

    @Test
    void consultarConfirmacionNoBorraYRespetaLasRelacionesDelBackend() throws Exception {
        preparar("VENDEDOR");
        when(turistasApi.buscar(2)).thenReturn(turista(true));
        mvc.perform(get("/turistas/2/eliminar").session(sesion())).andExpect(view().name("turistas/eliminar"));
        verify(turistasApi, never()).eliminar(any());
        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN)).when(turistasApi).eliminar(2);
        mvc.perform(post("/turistas/2/eliminar").session(sesion()).with(csrf()))
                .andExpect(status().isForbidden()).andExpect(model().attributeExists("errorOperacion"));
    }

    @Test
    void eliminaTrasConfirmar() throws Exception {
        preparar("VENDEDOR");
        when(turistasApi.buscar(2)).thenReturn(turista(true));
        mvc.perform(post("/turistas/2/eliminar").session(sesion()).with(csrf()))
                .andExpect(redirectedUrl("/turistas"));
        verify(turistasApi).eliminar(2);
    }

    @Test
    void csrfEsObligatorioParaTodasLasEscrituras() throws Exception {
        for (String ruta : List.of("/turistas", "/turistas/2/editar", "/turistas/2/eliminar")) {
            mvc.perform(post(ruta).session(sesion())).andExpect(status().isForbidden());
        }
        verifyNoInteractions(turistasApi);
    }

    @Test
    void noReintentaGuardadoAnteTimeout() throws Exception {
        preparar("ADMINISTRADOR");
        doThrow(new ResourceAccessException("Timeout")).when(turistasApi).crear(any());
        mvc.perform(formulario("/turistas").param("codigoSucursal", "1"))
                .andExpect(status().isServiceUnavailable()).andExpect(view().name("turistas/formulario"));
        verify(turistasApi, times(1)).crear(any());
    }

    @Test
    void sesionVencidaYTuristaAjenoSeRespetan() throws Exception {
        preparar("CLIENTE");
        when(turistasApi.buscar(2)).thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));
        mvc.perform(get("/turistas/2").session(sesion())).andExpect(status().isForbidden());
        when(autenticacionApi.obtenerSesion()).thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        MockHttpSession actual = sesion();
        mvc.perform(get("/turistas").session(actual)).andExpect(redirectedUrl("/login?sesionVencida"));
        assertTrue(actual.isInvalid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123456", "123456789", "12.345.678", "abcdefg", " 12345678"})
    void noEnviaDniInvalidoALaApi(String dni) throws Exception {
        preparar("ADMINISTRADOR");
        mvc.perform(formulario("/turistas").param("codigoSucursal", "1")
                .with(request -> { request.setParameter("dni", dni); return request; }))
                .andExpect(model().attributeHasFieldErrors("turista", "dni"));
        verify(turistasApi, never()).crear(any());
    }

    private void preparar(String rol) {
        SesionRespuesta usuario = new SesionRespuesta();
        usuario.setCodigo(1);
        usuario.setRol(rol);
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario);
        when(sucursalesApi.listar()).thenReturn(List.of());
        when(turistasApi.listar()).thenReturn(List.of());
    }

    private MockHttpSession sesion() {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuarioActual", new SesionRespuesta());
        return sesion;
    }

    private MockHttpServletRequestBuilder formulario(String ruta) {
        return post(ruta).session(sesion()).with(csrf()).param("dni", "76543210").param("nombre", "Ana").param("apellido", "Perez")
                .param("direccion", "Calle 1").param("email", "ana@example.test")
                .param("telefonoFijo", "111").param("telefonoCelular", "222");
    }

    private TuristaRespuesta turista(boolean titular) {
        TuristaRespuesta turista = new TuristaRespuesta();
        turista.setCodigo(2);
        turista.setDni("76543210");
        turista.setTitular(titular);
        turista.setCodigoTitular(titular ? null : 1);
        turista.setCodigoSucursal(3);
        return turista;
    }
}
