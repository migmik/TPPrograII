package com.tijetravel.tijefront.clientes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tijetravel.tijefront.dto.DisponibilidadHotelRespuesta;
import com.tijetravel.tijefront.dto.HotelRespuesta;

@Service
public class HotelesApiCliente {
    private final RestClient clienteBackend;

    public HotelesApiCliente(RestClient clienteBackend) {
        this.clienteBackend = clienteBackend;
    }

    public List<HotelRespuesta> listar() {
        HotelRespuesta[] hoteles = clienteBackend.get()
                .uri("/api/v1/hoteles")
                .retrieve()
                .body(HotelRespuesta[].class);
        if (hoteles == null) {
            throw new RestClientException("La API no devolvió el listado de hoteles.");
        }
        return Arrays.asList(hoteles);
    }

    public HotelRespuesta buscar(Integer codigo) {
        HotelRespuesta hotel = clienteBackend.get()
                .uri("/api/v1/hoteles/{codigo}", codigo)
                .retrieve()
                .body(HotelRespuesta.class);
        if (hotel == null) {
            throw new RestClientException("La API no devolvió el hotel.");
        }
        return hotel;
    }

    public DisponibilidadHotelRespuesta consultarDisponibilidad(
            Integer codigo, LocalDate llegada, LocalDate partida) {
        DisponibilidadHotelRespuesta disponibilidad = clienteBackend.get()
                .uri(uri -> uri.path("/api/v1/hoteles/{codigo}/disponibilidad")
                        .queryParam("fechaLlegada", llegada)
                        .queryParam("fechaPartida", partida)
                        .build(codigo))
                .retrieve()
                .body(DisponibilidadHotelRespuesta.class);
        if (disponibilidad == null) {
            throw new RestClientException("La API no devolvió la disponibilidad.");
        }
        return disponibilidad;
    }
}
