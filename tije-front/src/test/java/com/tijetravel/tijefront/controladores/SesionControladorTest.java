package com.tijetravel.tijefront.controladores;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.dto.SesionRespuesta;

@SpringBootTest
@AutoConfigureMockMvc
class SesionControladorTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private AutenticacionApiCliente autenticacionApi;

    @Test
    void elFormularioEsPublicoYLaCuentaRequiereSesion() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("sesion/login"));
        mvc.perform(get("/cuenta")).andExpect(redirectedUrl("/login"));
        verifyNoInteractions(autenticacionApi);
    }

    @Test
    void rechazaLoginSinCsrfAntesDeEnviarLaContrasenia() throws Exception {
        mvc.perform(post("/login").param("nombreUsuario", "ana").param("contrasenia", "clave"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(autenticacionApi);
    }

    @Test
    void validaLosCamposVaciosSinLlamarALaApi() throws Exception {
        mvc.perform(post("/login").with(csrf()).param("nombreUsuario", "").param("contrasenia", ""))
                .andExpect(view().name("sesion/login"))
                .andExpect(model().attributeHasFieldErrors("credenciales", "nombreUsuario", "contrasenia"));
        verifyNoInteractions(autenticacionApi);
    }

    @Test
    void informaCredencialesIncorrectasSinGuardarUsuario() throws Exception {
        when(autenticacionApi.iniciarSesion("ana", "incorrecta")).thenThrow(noAutorizado());
        MockHttpSession sesion = new MockHttpSession();
        mvc.perform(post("/login").session(sesion).with(csrf())
                        .param("nombreUsuario", "ana").param("contrasenia", "incorrecta"))
                .andExpect(status().isUnauthorized()).andExpect(view().name("sesion/login"))
                .andExpect(model().attribute("errorIngreso", "El usuario o la contraseña son incorrectos."));
        assertTrue(sesion.getAttribute("usuarioActual") == null);
    }

    @Test
    void alIngresarRenuevaElIdYGuardaSoloLosDatosDelUsuario() throws Exception {
        when(autenticacionApi.iniciarSesion("ana", "clave")).thenReturn(usuario());
        MockHttpSession sesion = new MockHttpSession();
        String idAnterior = sesion.getId();
        mvc.perform(post("/login").session(sesion).with(csrf())
                        .param("nombreUsuario", " ana ").param("contrasenia", "clave"))
                .andExpect(redirectedUrl("/cuenta"));
        assertNotEquals(idAnterior, sesion.getId());
        assertTrue(sesion.getAttribute("usuarioActual") instanceof SesionRespuesta);
        assertTrue(sesion.getAttribute("credenciales") == null);
    }

    @Test
    void laCuentaConfirmaLaSesionEnElBackend() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuario());
        mvc.perform(get("/cuenta").session(sesionIngresada()))
                .andExpect(view().name("sesion/cuenta"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(model().attributeExists("usuarioActual"));
        verify(autenticacionApi).obtenerSesion();
    }

    @Test
    void unaSesionVencidaEnLaApiTambienSeInvalidaEnElFront() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenThrow(noAutorizado());
        MockHttpSession sesion = sesionIngresada();
        mvc.perform(get("/cuenta").session(sesion)).andExpect(redirectedUrl("/login?sesionVencida"));
        assertTrue(sesion.isInvalid());
    }

    @Test
    void salirRequierePostYTokenCsrf() throws Exception {
        mvc.perform(get("/logout")).andExpect(status().isMethodNotAllowed());
        mvc.perform(post("/logout").session(sesionIngresada())).andExpect(status().isForbidden());
        verifyNoInteractions(autenticacionApi);
    }

    @Test
    void cierraLaSesionDelBackendYLaLocal() throws Exception {
        MockHttpSession sesion = sesionIngresada();
        mvc.perform(post("/logout").session(sesion).with(csrf())).andExpect(redirectedUrl("/login?salida"));
        verify(autenticacionApi).cerrarSesion();
        assertTrue(sesion.isInvalid());
    }

    @Test
    void siFallaLaSalidaRemotaIgualmenteCierraLaSesionLocal() throws Exception {
        doThrow(new ResourceAccessException("No disponible")).when(autenticacionApi).cerrarSesion();
        MockHttpSession sesion = sesionIngresada();
        mvc.perform(post("/logout").session(sesion).with(csrf())).andExpect(redirectedUrl("/login?salidaLocal"));
        assertTrue(sesion.isInvalid());
    }

    @Test
    void unaFallaDeConexionNoSeInformaComoContraseniaIncorrecta() throws Exception {
        when(autenticacionApi.iniciarSesion("ana", "clave")).thenThrow(new ResourceAccessException("No disponible"));
        mvc.perform(post("/login").with(csrf()).param("nombreUsuario", "ana").param("contrasenia", "clave"))
                .andExpect(status().isServiceUnavailable()).andExpect(view().name("sesion/login"))
                .andExpect(model().attribute("errorIngreso", "No pudimos iniciar sesión en este momento. Intentá nuevamente."));
    }

    private MockHttpSession sesionIngresada() {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuarioActual", usuario());
        return sesion;
    }

    private SesionRespuesta usuario() {
        SesionRespuesta usuario = new SesionRespuesta();
        usuario.setCodigo(1);
        usuario.setNombreUsuario("ana");
        usuario.setRol("VENDEDOR");
        return usuario;
    }

    private HttpClientErrorException noAutorizado() {
        return HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "No autorizado", HttpHeaders.EMPTY, new byte[0], null);
    }
}
