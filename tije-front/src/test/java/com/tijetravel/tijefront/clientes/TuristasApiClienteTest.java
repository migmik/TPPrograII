package com.tijetravel.tijefront.clientes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.formularios.GuardarTuristaFormulario;

class TuristasApiClienteTest {
    private TuristasApiCliente cliente;
    private MockRestServiceServer servidor;
    private ConexionApiSesion conexion;

    @BeforeEach
    void preparar() {
        var builder = RestClient.builder().baseUrl("http://backend");
        servidor = MockRestServiceServer.bindTo(builder).build();
        conexion = mock(ConexionApiSesion.class);
        when(conexion.getCliente()).thenReturn(builder.build());
        cliente = new TuristasApiCliente(conexion);
    }

    @Test
    void enviaDniEnAltaYModificacionConCsrf() {
        CsrfRespuesta csrf = new CsrfRespuesta();
        csrf.setNombreEncabezado("X-CSRF-TOKEN");
        csrf.setToken("token-prueba");
        when(conexion.obtenerCsrf()).thenReturn(csrf);
        GuardarTuristaFormulario formulario = new GuardarTuristaFormulario();
        formulario.setDni("12345678");
        servidor.expect(requestTo("http://backend/api/v1/turistas"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("X-CSRF-TOKEN", "token-prueba"))
                .andExpect(content().json("{\"dni\":\"12345678\"}"))
                .andRespond(withSuccess());
        servidor.expect(requestTo("http://backend/api/v1/turistas/1"))
                .andExpect(method(HttpMethod.PUT)).andExpect(header("X-CSRF-TOKEN", "token-prueba"))
                .andExpect(content().json("{\"dni\":\"12345678\"}"))
                .andRespond(withSuccess());
        cliente.crear(formulario);
        cliente.modificar(1, formulario);
        servidor.verify();
    }

    @Test
    void leeDniEnDetalleListadoYSelector() {
        String turista = "{\"codigo\":1,\"dni\":\"12345678\",\"nombre\":\"Ana\",\"titular\":true}";
        servidor.expect(requestTo("http://backend/api/v1/turistas/1"))
                .andRespond(withSuccess(turista, MediaType.APPLICATION_JSON));
        servidor.expect(requestTo("http://backend/api/v1/turistas"))
                .andRespond(withSuccess("[" + turista + "]", MediaType.APPLICATION_JSON));
        servidor.expect(requestTo("http://backend/api/v1/turistas"))
                .andRespond(withSuccess("[" + turista + "]", MediaType.APPLICATION_JSON));
        assertEquals("12345678", cliente.buscar(1).getDni());
        assertEquals("12345678", cliente.listarDetalles().get(0).getDni());
        assertEquals("12345678", cliente.listar().get(0).getDni());
        servidor.verify();
    }
}
