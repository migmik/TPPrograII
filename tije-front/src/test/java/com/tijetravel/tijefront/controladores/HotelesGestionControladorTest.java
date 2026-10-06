package com.tijetravel.tijefront.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import com.tijetravel.tijefront.clientes.HotelesApiCliente;
import com.tijetravel.tijefront.clientes.HotelesGestionApiCliente;
import com.tijetravel.tijefront.dto.HotelRespuesta;
import com.tijetravel.tijefront.dto.SesionRespuesta;
import com.tijetravel.tijefront.formularios.GuardarHotelFormulario;

@SpringBootTest
@AutoConfigureMockMvc
class HotelesGestionControladorTest {
    @Autowired MockMvc mvc;
    @MockitoBean AutenticacionApiCliente autenticacionApi;
    @MockitoBean HotelesApiCliente hotelesApi;
    @MockitoBean HotelesGestionApiCliente gestionApi;

    @Test
    void exigeSesionYRolVigente() throws Exception {
        mvc.perform(get("/hoteles/nuevo")).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/hoteles").with(csrf())).andExpect(redirectedUrl("/login"));
        verifyNoInteractions(gestionApi);
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("VENDEDOR"));
        mvc.perform(get("/hoteles/nuevo").session(sesion())).andExpect(status().isForbidden());
        mvc.perform(post("/hoteles").with(csrf()).session(sesion())).andExpect(status().isForbidden());
        verifyNoInteractions(gestionApi);
    }

    @Test
    void validaCapacidadYPermiteCero() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        mvc.perform(post("/hoteles").with(csrf()).session(sesion())
                .param("nombre", "Hotel A").param("direccion", "Calle 1")
                .param("ciudad", "Córdoba").param("telefono", "123").param("capacidadTotal", "-1"))
                .andExpect(view().name("hoteles/formulario")).andExpect(model().hasErrors());
        verify(gestionApi, never()).crear(any());
        mvc.perform(post("/hoteles").with(csrf()).session(sesion())
                .param("nombre", "Hotel A").param("direccion", "Calle 1")
                .param("ciudad", "Córdoba").param("telefono", "123").param("capacidadTotal", "0"))
                .andExpect(redirectedUrl("/hoteles"));
        verify(gestionApi).crear(any(GuardarHotelFormulario.class));
    }

    @Test
    void eliminarRequiereConfirmacionPostConCsrf() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        HotelRespuesta hotel = new HotelRespuesta();
        hotel.setCodigo(5);
        when(hotelesApi.buscar(5)).thenReturn(hotel);
        mvc.perform(get("/hoteles/5/eliminar").session(sesion())).andExpect(view().name("hoteles/eliminar"));
        verify(gestionApi, never()).eliminar(any());
        mvc.perform(post("/hoteles/5/eliminar").session(sesion())).andExpect(status().isForbidden());
        mvc.perform(post("/hoteles/5/eliminar").with(csrf()).session(sesion()))
                .andExpect(redirectedUrl("/hoteles"));
        verify(gestionApi).eliminar(eq(5));
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
