package com.tijetravel.tijefront.clientes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tijetravel.tijefront.dto.DisponibilidadHotelRespuesta;
import com.tijetravel.tijefront.dto.HotelRespuesta;
import com.tijetravel.tijefront.dto.PaginaHotelesRespuesta;

@Service
public class HotelesApiCliente {
    private final RestClient clienteBackend;
    private final ConexionApiSesion conexion;

    public HotelesApiCliente(RestClient clienteBackend) {
        this(clienteBackend, null);
    }

    @Autowired
    public HotelesApiCliente(RestClient clienteBackend, ConexionApiSesion conexion) {
        this.clienteBackend = clienteBackend;
        this.conexion = conexion;
    }

    public PaginaHotelesRespuesta listarPagina(String ciudad, String nombre, String orden,
            int pagina, boolean personal) {
        RestClient cliente = personal ? conexion.getCliente() : clienteBackend;
        PaginaHotelesRespuesta resultado = cliente.get().uri(uri -> {
            var ruta = uri.path("/api/v1/hoteles/pagina")
                    .queryParam("pagina", pagina).queryParam("tamanio", 20).queryParam("orden", orden);
            if (ciudad != null) ruta.queryParam("ciudad", ciudad);
            if (nombre != null) ruta.queryParam("nombre", nombre);
            return ruta.build();
        }).retrieve().body(PaginaHotelesRespuesta.class);
        if (resultado == null) throw new RestClientException("La API no devolvió la página de hoteles.");
        return resultado;
    }

    public List<HotelRespuesta> listar() {
        HotelRespuesta[] hoteles = clienteBackend.get().uri("/api/v1/hoteles")
                .retrieve().body(HotelRespuesta[].class);
        if (hoteles == null) throw new RestClientException("La API no devolvió el listado de hoteles.");
        return Arrays.asList(hoteles);
    }

    public List<HotelRespuesta> listar(String orden) {
        if ("codigo".equals(orden)) return listar();
        HotelRespuesta[] hoteles = clienteBackend.get()
                .uri(uri -> uri.path("/api/v1/hoteles").queryParam("orden", orden).build())
                .retrieve()
                .body(HotelRespuesta[].class);
        if (hoteles == null) {
            throw new RestClientException("La API no devolvió el listado de hoteles.");
        }
        return Arrays.asList(hoteles);
    }

    public List<HotelRespuesta> buscar(String ciudad, LocalDate llegada, LocalDate partida,
            int personas) {
        HotelRespuesta[] hoteles = clienteBackend.get()
                .uri(uri -> uri.path("/api/v1/hoteles/buscar")
                        .queryParam("ciudad", ciudad).queryParam("fechaLlegada", llegada)
                        .queryParam("fechaPartida", partida).queryParam("personas", personas).build())
                .retrieve().body(HotelRespuesta[].class);
        if (hoteles == null) {
            throw new RestClientException("La API no devolvio los resultados de hoteles.");
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
