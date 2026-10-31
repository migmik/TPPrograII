package com.tijetravel.tijefront.controladores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.HttpClientErrorException;

import com.tijetravel.tijefront.SesionesPrueba;
import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.clientes.BajasApiCliente;
import com.tijetravel.tijefront.dto.FilaBaja;
import com.tijetravel.tijefront.dto.RegistroDadoDeBaja;
import com.tijetravel.tijefront.dto.SesionRespuesta;

@SpringBootTest
@AutoConfigureMockMvc
class BajasControladorTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AutenticacionApiCliente autenticacionApi;
    @MockitoBean
    private BajasApiCliente bajasApi;

    @Test
    void listaLosHotelesDadosDeBajaConSusFechas() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        RegistroDadoDeBaja hotel = new RegistroDadoDeBaja();
        hotel.setCodigo(4);
        hotel.setNombre("Hotel Sierras");
        hotel.setCiudad("Córdoba");
        hotel.setFechaCreacion(LocalDateTime.of(2026, 9, 1, 10, 0));
        hotel.setFechaBaja(LocalDateTime.of(2026, 10, 30, 18, 45));
        when(bajasApi.listar("hoteles")).thenReturn(List.of(hotel));

        MvcResult resultado = mvc.perform(get("/bajas/hoteles").session(sesion()))
                .andExpect(view().name("bajas/lista")).andReturn();

        List<?> filas = (List<?>) resultado.getModelAndView().getModel().get("filas");
        FilaBaja fila = (FilaBaja) filas.get(0);
        assertEquals(4, fila.getCodigo());
        assertEquals("Hotel Sierras (Córdoba)", fila.getDescripcion());
        assertEquals("30/10/2026 18:45", fila.getBaja());
    }

    @Test
    void unVendedorNoVeLosHotelesDadosDeBajaPeroSiLosTuristas() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("VENDEDOR"));
        when(bajasApi.listar("turistas")).thenReturn(List.of());
        mvc.perform(get("/bajas/hoteles").session(sesion())).andExpect(status().isForbidden());
        mvc.perform(get("/bajas/turistas").session(sesion())).andExpect(view().name("bajas/lista"));
    }

    @Test
    void unModuloInexistenteDevuelve404SinLlamarALaApi() throws Exception {
        mvc.perform(get("/bajas/planetas").session(sesion())).andExpect(status().isNotFound());
        verifyNoInteractions(bajasApi, autenticacionApi);
    }

    @Test
    void reactivaYVuelveAlListado() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        mvc.perform(post("/bajas/vuelos/1203/reactivar").session(sesion()).with(csrf()))
                .andExpect(redirectedUrl("/bajas/vuelos"))
                .andExpect(flash().attribute("mensajeExito", "Registro 1203 reactivado correctamente."));
        verify(bajasApi).reactivar("vuelos", 1203);
    }

    @Test
    void muestraElMotivoQueDaLaApiCuandoNoSePuedeReactivar() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        byte[] cuerpo = "{\"mensaje\":\"Primero hay que reactivar al titular del grupo familiar\"}"
                .getBytes(StandardCharsets.UTF_8);
        doThrow(HttpClientErrorException.create(HttpStatus.FORBIDDEN, "Prohibido", HttpHeaders.EMPTY, cuerpo,
                StandardCharsets.UTF_8)).when(bajasApi).reactivar("turistas", 2);
        mvc.perform(post("/bajas/turistas/2/reactivar").session(sesion()).with(csrf()))
                .andExpect(redirectedUrl("/bajas/turistas"))
                .andExpect(flash().attributeExists("errorReactivacion"));
    }

    @Test
    void reactivarRequiereCsrf() throws Exception {
        mvc.perform(post("/bajas/vuelos/1203/reactivar").session(sesion())).andExpect(status().isForbidden());
        verifyNoInteractions(bajasApi);
    }

    private SesionRespuesta usuario(String rol) {
        SesionRespuesta usuario = new SesionRespuesta();
        usuario.setCodigo(1);
        usuario.setNombreUsuario("admin");
        SesionesPrueba.asignar(usuario, rol);
        return usuario;
    }

    private MockHttpSession sesion() {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuarioActual", usuario("ADMINISTRADOR"));
        return sesion;
    }
}
