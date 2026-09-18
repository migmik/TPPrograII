package com.tijetravel.tijefront.clientes;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tijetravel.tijefront.dto.DisponibilidadVueloRespuesta;
import com.tijetravel.tijefront.dto.VueloRespuesta;

@Service
public class VuelosApiCliente {
    private final RestClient clienteBackend;

    public VuelosApiCliente(RestClient clienteBackend) {
        this.clienteBackend = clienteBackend;
    }

    public List<VueloRespuesta> listar() {
        VueloRespuesta[] vuelos = clienteBackend.get()
                .uri("/api/v1/vuelos")
                .retrieve()
                .body(VueloRespuesta[].class);
        if (vuelos == null) {
            throw new RestClientException("La API no devolvió el listado de vuelos.");
        }
        return Arrays.asList(vuelos);
    }

    public VueloRespuesta buscar(Integer numero) {
        VueloRespuesta vuelo = clienteBackend.get()
                .uri("/api/v1/vuelos/{numero}", numero)
                .retrieve()
                .body(VueloRespuesta.class);
        if (vuelo == null) {
            throw new RestClientException("La API no devolvió el vuelo.");
        }
        return vuelo;
    }

    public DisponibilidadVueloRespuesta consultarDisponibilidad(Integer numero, String clase) {
        DisponibilidadVueloRespuesta disponibilidad = clienteBackend.get()
                .uri(uri -> uri.path("/api/v1/vuelos/{numero}/disponibilidad")
                        .queryParam("clase", clase)
                        .build(numero))
                .retrieve()
                .body(DisponibilidadVueloRespuesta.class);
        if (disponibilidad == null) {
            throw new RestClientException("La API no devolvió la disponibilidad del vuelo.");
        }
        return disponibilidad;
    }
}
