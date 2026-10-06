package com.tijetravel.tijefront.clientes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class CiudadesApiClienteTest {
    private CiudadesApiCliente cliente;
    private MockRestServiceServer servidor;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://backend");
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new CiudadesApiCliente(builder.build());
    }

    @Test
    void obtieneLasCiudadesDisponiblesDesdeLaApi() {
        servidor.expect(requestTo("http://backend/api/v1/ciudades"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[\"Buenos Aires\",\"Córdoba\",\"Goya\"]", MediaType.APPLICATION_JSON));

        assertEquals(List.of("Buenos Aires", "Córdoba", "Goya"), cliente.listar());
        servidor.verify();
    }
}