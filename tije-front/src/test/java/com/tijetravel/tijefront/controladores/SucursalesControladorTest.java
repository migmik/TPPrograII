package com.tijetravel.tijefront.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.clientes.SucursalesApiCliente;
import com.tijetravel.tijefront.dto.SesionRespuesta;
import com.tijetravel.tijefront.dto.SucursalResumen;
import com.tijetravel.tijefront.formularios.GuardarSucursalFormulario;

@SpringBootTest
@AutoConfigureMockMvc
class SucursalesControladorTest {
    @Autowired MockMvc mvc;
    @MockitoBean AutenticacionApiCliente autenticacionApi;
    @MockitoBean SucursalesApiCliente sucursalesApi;

    @Test
    void listadoEsPublicoYContieneDatosDelBackend() throws Exception {
        SucursalResumen sucursal = sucursal();
        when(sucursalesApi.listar()).thenReturn(List.of(sucursal));
        mvc.perform(get("/sucursales"))
                .andExpect(view().name("sucursales/lista"))
                .andExpect(model().attribute("sucursales", List.of(sucursal)));
        verifyNoInteractions(autenticacionApi);
    }

    @Test
    void soloElAdministradorPuedeGestionar() throws Exception {
        mvc.perform(get("/sucursales/nuevo")).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/sucursales").with(csrf())).andExpect(redirectedUrl("/login"));
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("VENDEDOR"));
        mvc.perform(get("/sucursales/nuevo").session(sesion())).andExpect(status().isForbidden());
        mvc.perform(post("/sucursales").with(csrf()).session(sesion())).andExpect(status().isForbidden());
        verify(sucursalesApi, never()).crear(any());
    }

    @Test
    void validaYPermiteCrearUnaSucursal() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        mvc.perform(post("/sucursales").with(csrf()).session(sesion())
                .param("direccion", "").param("telefono", "123"))
                .andExpect(view().name("sucursales/formulario")).andExpect(model().hasErrors());
        verify(sucursalesApi, never()).crear(any());
        mvc.perform(post("/sucursales").with(csrf()).session(sesion())
                .param("direccion", "Calle 1").param("telefono", "123"))
                .andExpect(redirectedUrl("/sucursales"));
        verify(sucursalesApi).crear(any(GuardarSucursalFormulario.class));
    }

    @Test
    void muestraDireccionDuplicadaEnElFormulario() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        doThrow(new HttpClientErrorException(HttpStatus.CONFLICT)).when(sucursalesApi).crear(any());
        mvc.perform(post("/sucursales").with(csrf()).session(sesion())
                .param("direccion", "Calle 1").param("telefono", "123"))
                .andExpect(status().isConflict()).andExpect(view().name("sucursales/formulario"))
                .andExpect(model().attributeExists("errorGestion"));
    }

    @Test
    void borrarRequiereConfirmacionYRespetaRelacionConTuristas() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        when(sucursalesApi.buscar(5)).thenReturn(sucursal());
        mvc.perform(get("/sucursales/5/eliminar").session(sesion()))
                .andExpect(view().name("sucursales/eliminar"));
        verify(sucursalesApi, never()).eliminar(any());
        mvc.perform(post("/sucursales/5/eliminar").session(sesion())).andExpect(status().isForbidden());
        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN)).when(sucursalesApi).eliminar(5);
        mvc.perform(post("/sucursales/5/eliminar").with(csrf()).session(sesion()))
                .andExpect(status().isForbidden()).andExpect(view().name("sucursales/eliminar"))
                .andExpect(model().attributeExists("errorGestion"));
    }

    private SucursalResumen sucursal() {
        SucursalResumen sucursal = new SucursalResumen();
        sucursal.setCodigo(5);
        sucursal.setDireccion("Calle 1");
        sucursal.setTelefono("123");
        return sucursal;
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
