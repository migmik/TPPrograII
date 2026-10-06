package com.tijetravel.tijefront.clientes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import com.tijetravel.tijefront.dto.CsrfRespuesta;
import com.tijetravel.tijefront.dto.PaginaReservasRespuesta;
import com.tijetravel.tijefront.formularios.GuardarReservaFormulario;

class ReservasApiClienteTest {
    private MockRestServiceServer servidor;
    private ReservasApiCliente cliente;
    private ConexionApiSesion conexion;

    @BeforeEach
    void preparar() {
        var builder = RestClient.builder().baseUrl("http://backend");
        servidor = MockRestServiceServer.bindTo(builder).build();
        conexion = mock(ConexionApiSesion.class);
        when(conexion.getCliente()).thenReturn(builder.build());
        cliente = new ReservasApiCliente(conexion);
    }

    @Test
    void leeFechasYLaSucursalHistoricaEnListadoYDetalle() {
        String json = """
                {"codigo":1,"codigoTurista":2,"codigoSucursalContratacion":7,"numeroVuelo":100,
                 "codigoHotel":3,"claseVuelo":"PRIMERA","tipoHospedaje":"MEDIA_PENSION",
                 "fechaLlegada":"2027-01-10","fechaPartida":"2027-01-12"}
                """;
        servidor.expect(requestTo("http://backend/api/v1/reservas")).andRespond(withSuccess("[" + json + "]", MediaType.APPLICATION_JSON));
        servidor.expect(requestTo("http://backend/api/v1/reservas/1")).andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
        assertEquals("10/01/2027", cliente.listar().get(0).getFechaLlegadaFormateada());
        assertEquals(7, cliente.buscar(1).getCodigoSucursalContratacion());
        servidor.verify();
    }

    @Test
    void consultaUnaPaginaFiltradaPorTurista() {
        servidor.expect(requestTo("http://backend/api/v1/reservas/pagina?pagina=2&tamanio=20&codigoTurista=4"))
                .andRespond(withSuccess("""
                        {"elementos":[],"pagina":2,"totalPaginas":3,"hayAnterior":true,"haySiguiente":false}
                        """, MediaType.APPLICATION_JSON));

        PaginaReservasRespuesta pagina = cliente.listarPagina(4, null, null, null, null, 2, 20);

        assertEquals(2, pagina.pagina());
        assertEquals(3, pagina.totalPaginas());
        assertTrue(pagina.hayAnterior());
        assertFalse(pagina.haySiguiente());
        servidor.verify();
    }

    @Test
    void escribeConCsrfYFechasIsoSinModificarSucursalHistorica() {
        CsrfRespuesta csrf = new CsrfRespuesta();
        csrf.setNombreEncabezado("X-CSRF-TOKEN");
        csrf.setToken("token-prueba");
        when(conexion.obtenerCsrf()).thenReturn(csrf);
        String json = """
                {"codigoTurista":2,"numeroVuelo":100,"codigoHotel":3,"claseVuelo":"PRIMERA",
                 "tipoHospedaje":"MEDIA_PENSION","fechaLlegada":"2027-01-10","fechaPartida":"2027-01-12"}
                """;
        servidor.expect(requestTo("http://backend/api/v1/reservas"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("X-CSRF-TOKEN", "token-prueba"))
                .andExpect(content().json(json))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("seleccionBuscador"))))
                .andRespond(withStatus(HttpStatus.CREATED));
        servidor.expect(requestTo("http://backend/api/v1/reservas/1"))
                .andExpect(method(HttpMethod.PUT)).andExpect(header("X-CSRF-TOKEN", "token-prueba"))
                .andExpect(content().json(json)).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("codigoSucursal"))))
                .andRespond(withSuccess());
        servidor.expect(requestTo("http://backend/api/v1/reservas/1"))
                .andExpect(method(HttpMethod.DELETE)).andExpect(header("X-CSRF-TOKEN", "token-prueba"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));
        GuardarReservaFormulario formulario = new GuardarReservaFormulario();
        formulario.setCodigoTurista(2);
        formulario.setNumeroVuelo(100);
        formulario.setCodigoHotel(3);
        formulario.setClaseVuelo("PRIMERA");
        formulario.setTipoHospedaje("MEDIA_PENSION");
        formulario.setFechaLlegada(LocalDate.of(2027, 1, 10));
        formulario.setFechaPartida(LocalDate.of(2027, 1, 12));
        formulario.setSeleccionBuscador(true);
        cliente.crear(formulario);
        cliente.modificar(1, formulario);
        cliente.eliminar(1);
        servidor.verify();
    }

    @Test
    void conservaElRechazoDeAccesoDelBackend() {
        servidor.expect(requestTo("http://backend/api/v1/reservas/1")).andRespond(withStatus(HttpStatus.FORBIDDEN));
        assertThrows(HttpClientErrorException.Forbidden.class, () -> cliente.buscar(1));
        servidor.verify();
    }
}
