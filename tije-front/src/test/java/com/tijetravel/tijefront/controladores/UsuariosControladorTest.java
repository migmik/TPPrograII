package com.tijetravel.tijefront.controladores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import com.tijetravel.tijefront.clientes.AutenticacionApiCliente;
import com.tijetravel.tijefront.clientes.TuristasApiCliente;
import com.tijetravel.tijefront.clientes.UsuariosApiCliente;
import com.tijetravel.tijefront.dto.SesionRespuesta;
import com.tijetravel.tijefront.dto.TuristaResumen;
import com.tijetravel.tijefront.dto.UsuarioRespuesta;
import com.tijetravel.tijefront.formularios.CrearUsuarioFormulario;
import com.tijetravel.tijefront.formularios.ModificarUsuarioFormulario;

@SpringBootTest
@AutoConfigureMockMvc
class UsuariosControladorTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private AutenticacionApiCliente autenticacionApi;
    @MockitoBean
    private UsuariosApiCliente usuariosApi;
    @MockitoBean
    private TuristasApiCliente turistasApi;

    @Test
    void sinSesionNoPermiteVerNiCrearUsuarios() throws Exception {
        mvc.perform(get("/usuarios")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/usuarios/nuevo")).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/usuarios").with(csrf())).andExpect(redirectedUrl("/login"));
        verifyNoInteractions(autenticacionApi, usuariosApi, turistasApi);
    }

    @ParameterizedTest
    @ValueSource(strings = { "VENDEDOR", "CLIENTE" })
    void elRolVigenteDelBackendPrevaleceSobreElMenuGuardado(String rol) throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuarioActual(rol));
        mvc.perform(get("/usuarios").session(sesion())).andExpect(status().isForbidden());
        mvc.perform(get("/usuarios/nuevo").session(sesion())).andExpect(status().isForbidden());
        mvc.perform(alta("ADMINISTRADOR")).andExpect(status().isForbidden());
        verifyNoInteractions(usuariosApi, turistasApi);
    }

    @Test
    void unAdministradorNecesitaCsrfParaCrear() throws Exception {
        mvc.perform(post("/usuarios").session(sesion())).andExpect(status().isForbidden());
        verifyNoInteractions(autenticacionApi, usuariosApi, turistasApi);
    }

    @Test
    void entregaElListadoDelBackend() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuarioActual("ADMINISTRADOR"));
        var usuarios = List.of(new UsuarioRespuesta());
        when(usuariosApi.listar()).thenReturn(usuarios);
        mvc.perform(get("/usuarios").session(sesion()))
                .andExpect(view().name("usuarios/lista")).andExpect(model().attribute("usuarios", usuarios));
    }

    @Test
    void filtraUsuariosPorRol() throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuarioActual("ADMINISTRADOR"));
        List<UsuarioRespuesta> resultado = List.of(new UsuarioRespuesta());
        when(usuariosApi.listar("VENDEDOR")).thenReturn(resultado);

        mvc.perform(get("/usuarios").param("rol", "VENDEDOR").session(sesion()))
                .andExpect(view().name("usuarios/lista"))
                .andExpect(model().attribute("usuarios", resultado))
                .andExpect(model().attribute("rolBusqueda", "VENDEDOR"));

        verify(usuariosApi).listar("VENDEDOR");
    }

    @Test
    void ofreceSolamenteLosTuristasSinCuenta() throws Exception {
        prepararFormulario();
        UsuarioRespuesta cliente = new UsuarioRespuesta();
        cliente.setCodigoTurista(1);
        when(usuariosApi.listar("CLIENTE")).thenReturn(List.of(cliente));
        TuristaResumen disponible = turista(2);
        when(turistasApi.listar(true)).thenReturn(List.of(turista(1), disponible));
        mvc.perform(get("/usuarios/nuevo").session(sesion()))
                .andExpect(view().name("usuarios/nuevo"))
                .andExpect(model().attribute("turistasDisponibles", List.of(disponible)));
            verify(usuariosApi).listar("CLIENTE");
            verify(turistasApi).listar(true);
    }

    @ParameterizedTest
    @ValueSource(strings = { "ADMINISTRADOR", "VENDEDOR", "CLIENTE" })
    void creaCadaRolYRedirigeParaNoReenviarElFormulario(String rol) throws Exception {
        prepararFormulario();
        doAnswer(invocacion -> {
            CrearUsuarioFormulario formulario = invocacion.getArgument(0);
            assertEquals("nuevo", formulario.getNombreUsuario());
            assertEquals("clave-elegida", formulario.getContrasenia());
            assertEquals(rol, formulario.getRol());
            assertEquals("CLIENTE".equals(rol) ? 1 : null, formulario.getCodigoTurista());
            return null;
        }).when(usuariosApi).crear(any());
        var solicitud = alta(rol);
        if ("CLIENTE".equals(rol))
            solicitud.param("codigoTurista", "1");
        mvc.perform(solicitud).andExpect(redirectedUrl("/usuarios"))
                .andExpect(flash().attribute("mensajeExito", "Usuario creado correctamente."));
        verify(usuariosApi).crear(any());
    }

    @Test
    void clienteRequiereUnTuristaDisponible() throws Exception {
        prepararFormulario();
        mvc.perform(alta("CLIENTE")).andExpect(model().attributeHasFieldErrors("usuario", "codigoTurista"));
        mvc.perform(alta("CLIENTE").param("codigoTurista", "999"))
                .andExpect(model().attributeHasFieldErrors("usuario", "codigoTurista"));
        verify(usuariosApi, never()).crear(any());
    }

    @Test
    void vendedorNoPuedeAsociarseAUnTurista() throws Exception {
        prepararFormulario();
        mvc.perform(alta("VENDEDOR").param("codigoTurista", "1"))
                .andExpect(model().attributeHasFieldErrors("usuario", "codigoTurista"));
        verify(usuariosApi, never()).crear(any());
    }

    @Test
    void validaRolCodigoYLongitudDeContraseniaEnBytes() throws Exception {
        prepararFormulario();
        mvc.perform(alta("OTRO")).andExpect(model().attributeHasFieldErrors("usuario", "rol"));
        mvc.perform(alta("CLIENTE").param("codigoTurista", "abc"))
                .andExpect(model().attributeHasFieldErrors("usuario", "codigoTurista"));
        mvc.perform(post("/usuarios").session(sesion()).with(csrf()).param("nombreUsuario", "nuevo")
                .param("rol", "VENDEDOR").param("contrasenia", "á".repeat(40)))
                .andExpect(model().attributeHasFieldErrors("usuario", "contrasenia"));
        verify(usuariosApi, never()).crear(any());
    }

    @Test
    void duplicadoConservaCamposPeroLimpiaLaContrasenia() throws Exception {
        prepararFormulario();
        doThrow(error(HttpStatus.CONFLICT)).when(usuariosApi).crear(any());
        var resultado = mvc.perform(alta("CLIENTE").param("codigoTurista", "1"))
                .andExpect(status().isConflict()).andExpect(view().name("usuarios/nuevo"))
                .andExpect(model().attributeExists("errorCreacion", "turistasDisponibles")).andReturn();
        var formulario = (CrearUsuarioFormulario) resultado.getModelAndView().getModel().get("usuario");
        assertEquals("nuevo", formulario.getNombreUsuario());
        assertEquals("CLIENTE", formulario.getRol());
        assertEquals(1, formulario.getCodigoTurista());
        assertEquals("", formulario.getContrasenia());
    }

    @Test
    void unaSesionVencidaNoPuedeCrear() throws Exception {
        MockHttpSession sesion = sesion();
        when(autenticacionApi.obtenerSesion()).thenThrow(error(HttpStatus.UNAUTHORIZED));
        mvc.perform(get("/usuarios").session(sesion)).andExpect(redirectedUrl("/login?sesionVencida"));
        assertTrue(sesion.isInvalid());
        verifyNoInteractions(usuariosApi, turistasApi);
    }

    @Test
    void respetaElRechazoDelBackendAunqueLaSesionDigaAdministrador() throws Exception {
        prepararFormulario();
        doThrow(error(HttpStatus.FORBIDDEN)).when(usuariosApi).crear(any());
        mvc.perform(alta("VENDEDOR")).andExpect(status().isForbidden()).andExpect(view().name("error"));
    }

    @Test
    void anteUnTimeoutNoReintentaLaCreacion() throws Exception {
        prepararFormulario();
        doThrow(new ResourceAccessException("Tiempo agotado")).when(usuariosApi).crear(any());
        mvc.perform(alta("VENDEDOR")).andExpect(status().isServiceUnavailable())
                .andExpect(model().attribute("errorCreacion",
                        "No pudimos confirmar la creación. Consultá el listado antes de volver a enviar el formulario."));
        verify(usuariosApi).crear(any());
    }

    @Test
    void editarCargaElNombreSinContrasenia() throws Exception {
        prepararEdicion();
        var resultado = mvc.perform(get("/usuarios/2/editar").session(sesion()))
                .andExpect(view().name("usuarios/editar")).andReturn();
        var formulario = (ModificarUsuarioFormulario) resultado.getModelAndView().getModel().get("usuario");
        assertEquals("anterior", formulario.getNombreUsuario());
        assertTrue(formulario.getContrasenia() == null || formulario.getContrasenia().isEmpty());
    }

    @Test
    void modificaCredencialesYConservaLaSesionDelAdministrador() throws Exception {
        prepararEdicion();
        doAnswer(invocacion -> {
            ModificarUsuarioFormulario formulario = invocacion.getArgument(1);
            assertEquals("nuevo", formulario.getNombreUsuario());
            assertEquals("clave-nueva", formulario.getContrasenia());
            return null;
        }).when(usuariosApi).modificar(eq(2), any());
        mvc.perform(edicion(2, "clave-nueva")).andExpect(redirectedUrl("/usuarios"));
        verify(usuariosApi).modificar(eq(2), any());
        verify(autenticacionApi, never()).cerrarSesion();
    }

    @Test
    void noModificaSinContraseniaNiConMasDe72Bytes() throws Exception {
        prepararEdicion();
        for (String clave : List.of("", "á".repeat(40))) {
            mvc.perform(edicion(2, clave)).andExpect(view().name("usuarios/editar"))
                    .andExpect(model().attributeHasFieldErrors("usuario", "contrasenia"));
        }
        verify(usuariosApi, never()).modificar(any(), any());
    }

    @Test
    void nombreDuplicadoNoDevuelveLaContrasenia() throws Exception {
        prepararEdicion();
        doThrow(error(HttpStatus.CONFLICT)).when(usuariosApi).modificar(any(), any());
        var resultado = mvc.perform(edicion(2, "clave-nueva")).andExpect(status().isConflict())
                .andExpect(model().attributeExists("errorEdicion")).andReturn();
        var formulario = (ModificarUsuarioFormulario) resultado.getModelAndView().getModel().get("usuario");
        assertEquals("", formulario.getContrasenia());
    }

    @Test
    void cambiarLaPropiaCuentaObligaAIngresarNuevamente() throws Exception {
        prepararEdicion();
        MockHttpSession actual = sesion();
        mvc.perform(edicion(1, "clave-nueva").session(actual))
                .andExpect(redirectedUrl("/login?credencialesActualizadas"));
        assertTrue(actual.isInvalid());
        verify(autenticacionApi).cerrarSesion();
    }

    @Test
    void cierraSesionLocalAunqueElBackendNoConfirmeLaSalida() throws Exception {
        prepararEdicion();
        doThrow(new ResourceAccessException("Sin conexión")).when(autenticacionApi).cerrarSesion();
        MockHttpSession actual = sesion();
        mvc.perform(edicion(1, "clave-nueva").session(actual))
                .andExpect(redirectedUrl("/login?credencialesActualizadas&salidaLocal"));
        assertTrue(actual.isInvalid());
    }

    @Test
    void consultarLaConfirmacionNoElimina() throws Exception {
        prepararEdicion();
        mvc.perform(get("/usuarios/2/eliminar").session(sesion()))
                .andExpect(view().name("usuarios/eliminar")).andExpect(model().attribute("esMiCuenta", false));
        verify(usuariosApi, never()).eliminar(any());
    }

    @Test
    void eliminarRequiereConfirmacionYNoPermiteLaPropiaCuenta() throws Exception {
        prepararEdicion();
        mvc.perform(post("/usuarios/1/eliminar").session(sesion()).with(csrf()))
                .andExpect(status().isForbidden()).andExpect(model().attribute("esMiCuenta", true));
        verify(usuariosApi, never()).eliminar(any());
        mvc.perform(post("/usuarios/2/eliminar").session(sesion()).with(csrf()))
                .andExpect(redirectedUrl("/usuarios"));
        verify(usuariosApi).eliminar(2);
    }

    @Test
    void respetaLaProteccionDelUltimoAdministradorEnElBackend() throws Exception {
        prepararEdicion();
        doThrow(error(HttpStatus.FORBIDDEN)).when(usuariosApi).eliminar(2);
        mvc.perform(post("/usuarios/2/eliminar").session(sesion()).with(csrf()))
                .andExpect(status().isForbidden()).andExpect(view().name("usuarios/eliminar"))
                .andExpect(model().attributeExists("errorEliminacion"));
    }

    @Test
    void editarYEliminarRequierenCsrf() throws Exception {
        mvc.perform(post("/usuarios/2/editar").session(sesion())).andExpect(status().isForbidden());
        mvc.perform(post("/usuarios/2/eliminar").session(sesion())).andExpect(status().isForbidden());
        verifyNoInteractions(usuariosApi);
    }

    @ParameterizedTest
    @ValueSource(strings = { "VENDEDOR", "CLIENTE" })
    void otrosRolesNoPuedenEditarNiEliminar(String rol) throws Exception {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuarioActual(rol));
        for (String accion : List.of("editar", "eliminar")) {
            mvc.perform(get("/usuarios/2/" + accion).session(sesion())).andExpect(status().isForbidden());
            mvc.perform(post("/usuarios/2/" + accion).session(sesion()).with(csrf()))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(usuariosApi);
    }

    @Test
    void empleadoRequiereDniYClienteNoLoCargaDosVeces() throws Exception {
        prepararFormulario();
        mvc.perform(alta("VENDEDOR").with(request -> { request.setParameter("dni", ""); return request; }))
                .andExpect(model().attributeHasFieldErrors("usuario", "dni"));
        mvc.perform(alta("CLIENTE").param("codigoTurista", "1")
                .with(request -> { request.setParameter("dni", "12345678"); return request; }))
                .andExpect(model().attributeHasFieldErrors("usuario", "dni"));
        verify(usuariosApi, never()).crear(any());
    }

    @Test
    void noOfreceTitularesSinDocumentoParaCrearCuenta() throws Exception {
        prepararFormulario();
        TuristaResumen pendiente = turista(1);
        pendiente.setDni(null);
        when(turistasApi.listar(true)).thenReturn(List.of(pendiente));
        mvc.perform(get("/usuarios/nuevo").session(sesion()))
                .andExpect(model().attribute("turistasDisponibles", List.of()));
    }

    private void prepararEdicion() {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuarioActual("ADMINISTRADOR"));
        UsuarioRespuesta cuenta = new UsuarioRespuesta();
        cuenta.setCodigo(2);
        cuenta.setNombreUsuario("anterior");
        cuenta.setRol("VENDEDOR");
        when(usuariosApi.buscar(any())).thenReturn(cuenta);
    }

    private MockHttpServletRequestBuilder edicion(int codigo, String clave) {
        return post("/usuarios/" + codigo + "/editar").session(sesion()).with(csrf())
                .param("nombreUsuario", " nuevo ").param("contrasenia", clave).param("dni", "74567890");
    }

    private void prepararFormulario() {
        when(autenticacionApi.obtenerSesion()).thenReturn(usuarioActual("ADMINISTRADOR"));
        when(usuariosApi.listar("CLIENTE")).thenReturn(List.of());
        when(turistasApi.listar(true)).thenReturn(List.of(turista(1)));
    }

    private MockHttpServletRequestBuilder alta(String rol) {
        return post("/usuarios").session(sesion()).with(csrf()).param("nombreUsuario", " nuevo ")
                .param("contrasenia", "clave-elegida").param("rol", rol).param("dni", "CLIENTE".equals(rol) ? "" : "74567890");
    }

    private SesionRespuesta usuarioActual(String rol) {
        SesionRespuesta usuario = new SesionRespuesta();
        usuario.setCodigo(1);
        usuario.setRol(rol);
        usuario.setNombreUsuario("administrador");
        return usuario;
    }

    private MockHttpSession sesion() {
        MockHttpSession sesion = new MockHttpSession();
        sesion.setAttribute("usuarioActual", usuarioActual("ADMINISTRADOR"));
        return sesion;
    }

    private TuristaResumen turista(int codigo) {
        TuristaResumen turista = new TuristaResumen();
        turista.setCodigo(codigo);
        turista.setTitular(true);
        turista.setDni("76543210");
        turista.setNombre("Ana");
        turista.setApellido("Pérez");
        return turista;
    }

    private HttpClientErrorException error(HttpStatus estado) {
        return HttpClientErrorException.create(estado, "Error de prueba", HttpHeaders.EMPTY, new byte[0], null);
    }
}
