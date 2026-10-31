package com.tijetravel.tijeback.servicios;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vuelo;

/**
 * Operaciones sobre los vuelos.
 *
 * Las altas, bajas y modificaciones requieren el permiso ADMINISTRAR_VUELOS y
 * lanzan OperacionNoPermitidaException si el actor no lo tiene.
 */
public interface VueloServicio {

    /** @throws EntidadDuplicadaException si ya existe un vuelo con ese numero */
    Vuelo crear(Usuario actor, int numero, LocalDateTime fechaYHora, String origen, String destino,
            int totalPlazas, int plazasTurista, int plazasPrimera, int precio);

    /** Catalogo completo ordenado por numero, precioAsc o precioDesc. Solo para el personal. */
    List<Vuelo> listarPara(Usuario actor, String orden);

    List<String> listarOrigenes();

    List<String> listarDestinos();

    /** Busqueda paginada por origen, destino y fechas. Sin filtros se trata como el catalogo completo. */
    Page<Vuelo> buscarPagina(Usuario actor, String origen, String destino, LocalDate desde, LocalDate hasta,
            String orden, int pagina, int tamanio);

    /** Vuelos de ese dia con lugar en clase turista para la cantidad de personas. */
    List<Vuelo> buscarEnFecha(String origen, String destino, LocalDate fecha, int personas);

    /** Hasta cinco vuelos de otros dias, empezando por los mas cercanos a la fecha pedida. */
    List<Vuelo> sugerirOtrosDias(String origen, String destino, LocalDate fecha, int personas);

    /** @throws EntidadNoEncontradaException si no existe el vuelo */
    Vuelo encontrarPorId(Integer numero);

    /**
     * @throws CapacidadExcedidaException si las plazas quedan por debajo de las reservas hechas
     * @throws OperacionNoPermitidaException si el cambio deja reservas incompatibles
     */
    Vuelo modificar(Usuario actor, Integer numero, LocalDateTime fechaYHora, String origen, String destino,
            int totalPlazas, int plazasTurista, int plazasPrimera, int precio);

    /** @throws OperacionNoPermitidaException si el vuelo tiene reservas */
    void eliminar(Usuario actor, Integer numero);
}
