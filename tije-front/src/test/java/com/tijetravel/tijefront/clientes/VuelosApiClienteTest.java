package com.tijetravel.tijefront.clientes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

class VuelosApiClienteTest {
    private MockRestServiceServer servidor;
    private VuelosApiCliente cliente;
    private static final String VUELO_JSON = """
            {"numero":101,"fechaYHora":"2027-02-01T09:30:00","origen":"Buenos Aires",
             "destino":"Córdoba","totalPlazas":100,"plazasTurista":80,"plazasPrimera":20}
            """;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://backend");
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new VuelosApiCliente(builder.build());
    }

    @Test
    void convierteLaFechaYLasCapacidadesDelJson() {
        servidor.expect(requestTo("http://backend/api/v1/vuelos"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[" + VUELO_JSON + "]", MediaType.APPLICATION_JSON));
        var vuelos = cliente.listar();
        assertEquals(1, vuelos.size());
        var vuelo = vuelos.get(0);
        assertEquals(101, vuelo.getNumero());
        assertEquals("Córdoba", vuelo.getDestino());
        assertEquals(LocalDateTime.of(2027, 2, 1, 9, 30), vuelo.getFechaYHora());
        assertEquals("01/02/2027 09:30", vuelo.getFechaYHoraFormateada());
        assertEquals(100, vuelo.getTotalPlazas());
        assertEquals(80, vuelo.getPlazasTurista());
        assertEquals(20, vuelo.getPlazasPrimera());
        servidor.verify();
    }

    @Test
    void buscaElVueloPorNumero() {
        servidor.expect(requestTo("http://backend/api/v1/vuelos/101"))
                .andRespond(withSuccess(VUELO_JSON, MediaType.APPLICATION_JSON));
        assertEquals(101, cliente.buscar(101).getNumero());
        servidor.verify();
    }

    @Test
    void aceptaUnCatalogoVacio() {
        servidor.expect(requestTo("http://backend/api/v1/vuelos"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        assertTrue(cliente.listar().isEmpty());
        servidor.verify();
    }

    @Test
    void consultaCadaClaseYConservaLaDisponibilidadRecibida() {
        for (String clase : new String[] {"TURISTA", "PRIMERA"}) {
            servidor.expect(requestTo("http://backend/api/v1/vuelos/101/disponibilidad?clase=" + clase))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("""
                            {"numeroVuelo":101,"claseVuelo":"%s","plazasDisponibles":0}
                            """.formatted(clase), MediaType.APPLICATION_JSON));
        }
        for (String clase : new String[] {"TURISTA", "PRIMERA"}) {
            var disponibilidad = cliente.consultarDisponibilidad(101, clase);
            assertEquals(101, disponibilidad.getNumeroVuelo());
            assertEquals(clase, disponibilidad.getClaseVuelo());
            assertEquals(0, disponibilidad.getPlazasDisponibles());
        }
        servidor.verify();
    }

    @Test
    void conservaElErrorDeVueloInexistente() {
        servidor.expect(requestTo("http://backend/api/v1/vuelos/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThrows(HttpClientErrorException.NotFound.class, () -> cliente.buscar(999));
        servidor.verify();
    }

    @Test
    void noConfundeUnaRespuestaSinCuerpoConUnCatalogoVacio() {
        servidor.expect(requestTo("http://backend/api/v1/vuelos"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));
        assertThrows(RestClientException.class, () -> cliente.listar());
        servidor.verify();
    }
}
