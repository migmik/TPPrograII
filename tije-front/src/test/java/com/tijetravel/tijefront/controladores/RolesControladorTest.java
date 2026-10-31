package com.tijetravel.tijefront.controladores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import com.tijetravel.tijefront.SesionesPrueba;
import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.clientes.RolesApiCliente;
import com.tijetravel.tijefront.dto.SesionRespuesta;
import com.tijetravel.tijefront.formularios.GuardarRolFormulario;

@SpringBootTest
@AutoConfigureMockMvc
class RolesControladorTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AutenticacionApiCliente autenticacionApi;
    @MockitoBean
    private RolesApiCliente rolesApi;

    @Test
    void soloQuienAdministraUsuariosVeLosRoles() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("VENDEDOR"));
        mvc.perform(get("/roles").session(sesion())).andExpect(status().isForbidden());
        verifyNoInteractions(rolesApi);
    }

    @Test
    void listaLosRolesConSusPermisos() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        when(rolesApi.listar()).thenReturn(SesionesPrueba.roles());
        mvc.perform(get("/roles").session(sesion()))
                .andExpect(view().name("roles/lista"))
                .andExpect(model().attributeExists("roles", "permisos"));
        verify(rolesApi).listarPermisos();
    }

    @Test
    void creaUnRolHoteleroConLasCasillasTildadas() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocacion) {
                GuardarRolFormulario formulario = invocacion.getArgument(0);
                assertEquals("Hotelero", formulario.getNombre());
                assertEquals("EMPLEADO", formulario.getTipoUsuario());
                assertEquals(List.of("VER_CATALOGO_COMPLETO", "ADMINISTRAR_HOTELES"), formulario.getPermisos());
                return null;
            }
        }).when(rolesApi).crear(any());
        mvc.perform(post("/roles").session(sesion()).with(csrf())
                .param("nombre", " Hotelero ").param("tipoUsuario", "EMPLEADO")
                .param("permisos", "VER_CATALOGO_COMPLETO").param("permisos", "ADMINISTRAR_HOTELES"))
                .andExpect(redirectedUrl("/roles"));
        verify(rolesApi).crear(any());
    }

    @Test
    void sinNombreNoLlamaALaApi() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        mvc.perform(post("/roles").session(sesion()).with(csrf()).param("nombre", "").param("tipoUsuario", "EMPLEADO"))
                .andExpect(view().name("roles/formulario"))
                .andExpect(model().attributeHasFieldErrors("rol", "nombre"));
        verify(rolesApi, never()).crear(any());
    }

    @Test
    void elTipoDeUsuarioNoSeCambiaAlEditar() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        when(rolesApi.buscar(3)).thenReturn(SesionesPrueba.roles().get(2));
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocacion) {
                GuardarRolFormulario formulario = invocacion.getArgument(1);
                assertEquals("CLIENTE", formulario.getTipoUsuario());
                return null;
            }
        }).when(rolesApi).modificar(eq(3), any());
        mvc.perform(post("/roles/3/editar").session(sesion()).with(csrf())
                .param("nombre", "Cliente").param("tipoUsuario", "EMPLEADO").param("permisos", "CONSULTAR"))
                .andExpect(redirectedUrl("/roles"));
    }

    @Test
    void muestraElRechazoDelBackendAlDarDeBajaUnRolEnUso() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario("ADMINISTRADOR"));
        when(rolesApi.buscar(2)).thenReturn(SesionesPrueba.roles().get(1));
        doThrow(HttpClientErrorException.create(HttpStatus.FORBIDDEN, "En uso", HttpHeaders.EMPTY, new byte[0], null))
                .when(rolesApi).eliminar(2);
        mvc.perform(post("/roles/2/eliminar").session(sesion()).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(view().name("roles/eliminar"))
                .andExpect(model().attributeExists("errorGestion"));
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
