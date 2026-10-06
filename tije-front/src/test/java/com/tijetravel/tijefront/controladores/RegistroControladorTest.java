package com.tijetravel.tijefront.controladores;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import com.tijetravel.tijefront.clientes.RegistroApiCliente;
import com.tijetravel.tijefront.clientes.SucursalesApiCliente;
import com.tijetravel.tijefront.dto.SucursalResumen;

@SpringBootTest
@AutoConfigureMockMvc
class RegistroControladorTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private RegistroApiCliente registroApi;
    @MockitoBean
    private SucursalesApiCliente sucursalesApi;

    @Test
    void muestraRegistroPublicoConSucursales() throws Exception {
        when(sucursalesApi.listar()).thenReturn(List.of(new SucursalResumen()));

        mvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(view().name("sesion/registro"))
                .andExpect(model().attributeExists("registro", "sucursales"));
    }

    @Test
    void validaLosDatosAntesDeEnviarElRegistro() throws Exception {
        mvc.perform(post("/registro").with(csrf()).param("nombreUsuario", "")
                .param("dni", "123").param("codigoSucursal", "0"))
                .andExpect(view().name("sesion/registro"))
                .andExpect(model().attributeHasFieldErrors("registro", "nombreUsuario", "dni", "codigoSucursal"));
        verifyNoInteractions(registroApi);
    }

    @Test
    void creaLaCuentaYRedirigeAlLogin() throws Exception {
        mvc.perform(registroValido())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("registroExitoso", true));

        verify(registroApi).registrar(any());
    }

    @Test
    void muestraConflictoDeUsuarioODniDuplicado() throws Exception {
        doThrow(HttpClientErrorException.create(HttpStatus.CONFLICT, "Duplicado", null, new byte[0], null))
                .when(registroApi).registrar(any());

        mvc.perform(registroValido())
                .andExpect(status().isConflict())
                .andExpect(view().name("sesion/registro"))
                .andExpect(model().attributeExists("errorRegistro", "sucursales"));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder registroValido() {
        return post("/registro").with(csrf())
                .param("nombreUsuario", "ana")
                .param("contrasenia", "clave-segura")
                .param("dni", "12345678")
                .param("nombre", "Ana")
                .param("apellido", "Perez")
                .param("direccion", "Calle 1")
                .param("email", "ana@example.com")
                .param("telefonoFijo", "111")
                .param("telefonoCelular", "222")
                .param("codigoSucursal", "1");
    }
}