package com.tijetravel.tijefront.clientes;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class CiudadesApiCliente {
    private final RestClient clienteBackend;

    public CiudadesApiCliente(RestClient clienteBackend) {
        this.clienteBackend = clienteBackend;
    }

    public List<String> listar() {
        String[] ciudades = clienteBackend.get().uri("/api/v1/ciudades")
                .retrieve().body(String[].class);
        if (ciudades == null) {
            throw new RestClientException("La API no devolvió las ciudades disponibles.");
        }
        return Arrays.asList(ciudades);
    }
}