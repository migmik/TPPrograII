package com.tijetravel.tijefront.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.clientes.VuelosApiCliente;
import com.tijetravel.tijefront.clientes.VuelosGestionApiCliente;
import com.tijetravel.tijefront.dto.SesionRespuesta;
import com.tijetravel.tijefront.dto.VueloRespuesta;
import com.tijetravel.tijefront.formularios.GuardarVueloFormulario;

@SpringBootTest
@AutoConfigureMockMvc
class VuelosGestionControladorTest {
    @Autowired MockMvc mvc;
    @MockitoBean AutenticacionApiCliente autenticacionApi;
    @MockitoBean VuelosApiCliente vuelosApi;
    @MockitoBean VuelosGestionApiCliente gestionApi;

    @Test
    void exigeAdministradorEnElBackend() throws Exception {
        mvc.perform(get("/vuelos/nuevo")).andExpect(redirectedUrl("/login"));
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("CLIENTE"));
        mvc.perform(get("/vuelos/nuevo").session(sesion())).andExpect(status().isForbidden());
        mvc.perform(post("/vuelos").with(csrf()).session(sesion())).andExpect(status().isForbidden());
        verifyNoInteractions(gestionApi);
    }

    @Test
    void validaSumaDePlazasAntesDeCrear() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        var solicitud = post("/vuelos").with(csrf()).session(sesion())
                .param("numero", "20").param("fechaYHora", "2027-03-10T14:30")
                .param("origen", "Buenos Aires").param("destino", "Córdoba")
                .param("totalPlazas", "10").param("plazasTurista", "8").param("plazasPrimera", "3");
        mvc.perform(solicitud).andExpect(view().name("vuelos/formulario"))
                .andExpect(model().attributeHasFieldErrors("vuelo", "totalPlazas"));
        verify(gestionApi, never()).crear(any());
    }

    @Test
    void creaYModificaConLaFechaIngresada() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        mvc.perform(post("/vuelos").with(csrf()).session(sesion())
                .param("numero", "20").param("fechaYHora", "2027-03-10T14:30")
                .param("origen", "Buenos Aires").param("destino", "Córdoba")
                .param("totalPlazas", "10").param("plazasTurista", "8").param("plazasPrimera", "2"))
                .andExpect(redirectedUrl("/vuelos"));
        verify(gestionApi).crear(any(GuardarVueloFormulario.class));
        mvc.perform(post("/vuelos/20/editar").with(csrf()).session(sesion())
                .param("numero", "20").param("fechaYHora", "2027-03-11T14:30")
                .param("origen", "Buenos Aires").param("destino", "Córdoba")
                .param("totalPlazas", "10").param("plazasTurista", "8").param("plazasPrimera", "2"))
                .andExpect(redirectedUrl("/vuelos"));
        verify(gestionApi).modificar(org.mockito.ArgumentMatchers.eq(20), any());
    }

    @Test
    void eliminarSoloConConfirmacionYCsrf() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        VueloRespuesta vuelo = new VueloRespuesta();
        vuelo.setNumero(20);
        when(vuelosApi.buscar(20)).thenReturn(vuelo);
        mvc.perform(get("/vuelos/20/eliminar").session(sesion())).andExpect(view().name("vuelos/eliminar"));
        verify(gestionApi, never()).eliminar(any());
        mvc.perform(post("/vuelos/20/eliminar").session(sesion())).andExpect(status().isForbidden());
        mvc.perform(post("/vuelos/20/eliminar").with(csrf()).session(sesion()))
                .andExpect(redirectedUrl("/vuelos"));
        verify(gestionApi).eliminar(20);
    }

    private MockHttpSession sesion() {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuarioActual", usuario("ADMINISTRADOR"));
        return sesion;
    }

    private SesionRespuesta usuario(String rol) {
        SesionRespuesta usuario = new SesionRespuesta();
        usuario.setRol(rol);
        return usuario;
    }
}
