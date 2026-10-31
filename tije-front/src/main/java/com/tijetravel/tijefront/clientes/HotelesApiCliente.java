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

    public List<String> listarCiudades() { return opciones("/api/v1/hoteles/ciudades"); }

    public List<String> listarNombres() { return opciones("/api/v1/hoteles/nombres"); }

    private List<String> opciones(String ruta) {
        String[] valores = clienteBackend.get().uri(ruta).retrieve().body(String[].class);
        if (valores == null) throw new RestClientException("La API no devolvio las opciones de hoteles.");
        return Arrays.asList(valores);
    }

    public PaginaHotelesRespuesta listarPagina(String ciudad, String nombre, String orden,
            int pagina, boolean personal) {
        RestClient cliente = clienteBackend;
        if (personal) {
            cliente = conexion.getCliente();
        }
        RutaConsulta ruta = new RutaConsulta("/api/v1/hoteles/pagina")
                .parametro("pagina", pagina).parametro("tamanio", 20).parametro("orden", orden)
                .parametro("ciudad", ciudad).parametro("nombre", nombre);
        PaginaHotelesRespuesta resultado = cliente.get().uri(ruta.getPlantilla(), ruta.getValores())
                .retrieve().body(PaginaHotelesRespuesta.class);
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
        RutaConsulta ruta = new RutaConsulta("/api/v1/hoteles").parametro("orden", orden);
        HotelRespuesta[] hoteles = clienteBackend.get().uri(ruta.getPlantilla(), ruta.getValores())
                .retrieve()
                .body(HotelRespuesta[].class);
        if (hoteles == null) {
            throw new RestClientException("La API no devolvió el listado de hoteles.");
        }
        return Arrays.asList(hoteles);
    }

    public List<HotelRespuesta> buscar(String ciudad, LocalDate llegada, LocalDate partida,
            int personas) {
        RutaConsulta ruta = new RutaConsulta("/api/v1/hoteles/buscar")
                .parametro("ciudad", ciudad).parametro("fechaLlegada", llegada)
                .parametro("fechaPartida", partida).parametro("personas", personas);
        HotelRespuesta[] hoteles = clienteBackend.get().uri(ruta.getPlantilla(), ruta.getValores())
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
        RutaConsulta ruta = new RutaConsulta("/api/v1/hoteles/{codigo}/disponibilidad")
                .variable("codigo", codigo)
                .parametro("fechaLlegada", llegada)
                .parametro("fechaPartida", partida);
        DisponibilidadHotelRespuesta disponibilidad = clienteBackend.get()
                .uri(ruta.getPlantilla(), ruta.getValores())
                .retrieve()
                .body(DisponibilidadHotelRespuesta.class);
        if (disponibilidad == null) {
            throw new RestClientException("La API no devolvió la disponibilidad.");
        }
        return disponibilidad;
    }
}
