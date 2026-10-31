package com.tijetravel.tijefront.clientes;

import java.util.Arrays;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tijetravel.tijefront.dto.DisponibilidadVueloRespuesta;
import com.tijetravel.tijefront.dto.VueloRespuesta;
import com.tijetravel.tijefront.dto.PaginaVuelosRespuesta;

@Service
public class VuelosApiCliente {
    private final RestClient clienteBackend;
    private final ConexionApiSesion conexion;

    public VuelosApiCliente(RestClient clienteBackend) {
        this(clienteBackend, null);
    }

    @Autowired
    public VuelosApiCliente(RestClient clienteBackend, ConexionApiSesion conexion) {
        this.clienteBackend = clienteBackend;
        this.conexion = conexion;
    }

    public List<String> listarOrigenes() { return opciones("/api/v1/vuelos/origenes"); }

    public List<String> listarDestinos() { return opciones("/api/v1/vuelos/destinos"); }

    private List<String> opciones(String ruta) {
        String[] valores = clienteBackend.get().uri(ruta).retrieve().body(String[].class);
        if (valores == null) throw new RestClientException("La API no devolvio las opciones de vuelos.");
        return Arrays.asList(valores);
    }

    public PaginaVuelosRespuesta listarPagina(String origen, String destino, LocalDate fechaDesde,
            LocalDate fechaHasta, String orden, int pagina, boolean personal) {
        RestClient cliente = personal ? conexion.getCliente() : clienteBackend;
        PaginaVuelosRespuesta resultado = cliente.get().uri(uri -> {
            var ruta = uri.path("/api/v1/vuelos/pagina")
                    .queryParam("pagina", pagina).queryParam("tamanio", 20).queryParam("orden", orden);
            if (origen != null) ruta.queryParam("origen", origen);
            if (destino != null) ruta.queryParam("destino", destino);
            if (fechaDesde != null) ruta.queryParam("fechaDesde", fechaDesde);
            if (fechaHasta != null) ruta.queryParam("fechaHasta", fechaHasta);
            return ruta.build();
        }).retrieve().body(PaginaVuelosRespuesta.class);
        if (resultado == null) throw new RestClientException("La API no devolvió la página de vuelos.");
        return resultado;
    }

    public List<VueloRespuesta> listar() {
        VueloRespuesta[] vuelos = clienteBackend.get().uri("/api/v1/vuelos")
                .retrieve().body(VueloRespuesta[].class);
        if (vuelos == null) throw new RestClientException("La API no devolvió el listado de vuelos.");
        return Arrays.asList(vuelos);
    }

    public List<VueloRespuesta> listar(String orden) {
        if ("numero".equals(orden)) return listar();
        VueloRespuesta[] vuelos = clienteBackend.get()
                .uri(uri -> uri.path("/api/v1/vuelos").queryParam("orden", orden).build())
                .retrieve()
                .body(VueloRespuesta[].class);
        if (vuelos == null) {
            throw new RestClientException("La API no devolvió el listado de vuelos.");
        }
        return Arrays.asList(vuelos);
    }

    public List<VueloRespuesta> buscar(String origen, String destino, LocalDate fecha,
            int personas, boolean sugerencias) {
        VueloRespuesta[] vuelos = clienteBackend.get()
                .uri(uri -> uri.path("/api/v1/vuelos/buscar")
                        .queryParam("origen", origen).queryParam("destino", destino)
                        .queryParam("fecha", fecha).queryParam("personas", personas)
                        .queryParam("sugerencias", sugerencias).build())
                .retrieve().body(VueloRespuesta[].class);
        if (vuelos == null) {
            throw new RestClientException("La API no devolvio los resultados de vuelos.");
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
