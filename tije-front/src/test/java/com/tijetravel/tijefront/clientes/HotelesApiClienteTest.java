package com.tijetravel.tijefront.clientes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

class HotelesApiClienteTest {
    private MockRestServiceServer servidor;
    private HotelesApiCliente cliente;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://backend");
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new HotelesApiCliente(builder.build());
    }

    @Test
    void convierteElJsonDelBackendEnHoteles() {
        servidor.expect(requestTo("http://backend/api/v1/hoteles"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"codigo":1,"nombre":"Hotel Centro","direccion":"Calle 1",
                          "ciudad":"Córdoba","telefono":"123","capacidadTotal":35}]
                        """, MediaType.APPLICATION_JSON));
        var hoteles = cliente.listar();
        assertEquals(1, hoteles.size());
        assertEquals("Córdoba", hoteles.get(0).getCiudad());
        assertEquals(35, hoteles.get(0).getCapacidadTotal());
        servidor.verify();
    }

    @Test
    void aceptaUnCatalogoVacio() {
        servidor.expect(requestTo("http://backend/api/v1/hoteles"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        assertTrue(cliente.listar().isEmpty());
        servidor.verify();
    }

    @Test
    void enviaLasFechasYLeeLaDisponibilidadCalculada() {
        servidor.expect(requestTo("http://backend/api/v1/hoteles/1/disponibilidad?fechaLlegada=2027-02-01&fechaPartida=2027-02-04"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"codigoHotel":1,"fechaLlegada":"2027-02-01",
                         "fechaPartida":"2027-02-04","plazasDisponibles":0}
                        """, MediaType.APPLICATION_JSON));
        var disponibilidad = cliente.consultarDisponibilidad(
                1, LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 4));
        assertEquals(0, disponibilidad.getPlazasDisponibles());
        assertEquals(LocalDate.of(2027, 2, 4), disponibilidad.getFechaPartida());
        servidor.verify();
    }

    @Test
    void conservaElErrorDeHotelInexistenteParaMostrarloEnLaVista() {
        servidor.expect(requestTo("http://backend/api/v1/hoteles/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThrows(HttpClientErrorException.NotFound.class, () -> cliente.buscar(999));
        servidor.verify();
    }
}
