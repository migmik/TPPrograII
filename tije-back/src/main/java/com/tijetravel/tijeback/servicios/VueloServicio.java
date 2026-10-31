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
 * Las altas, bajas, modificaciones y reactivaciones requieren el permiso
 * ADMINISTRAR_VUELOS y lanzan OperacionNoPermitidaException si el actor no lo
 * tiene. Las consultas solo devuelven vuelos activos.
 */
public interface VueloServicio {

    /** @throws EntidadDuplicadaException si ya existe un vuelo con ese numero, aunque este dado de baja */
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

    /** Hasta tres vuelos de otros dias, empezando por los mas cercanos a la fecha pedida. */
    List<Vuelo> sugerirOtrosDias(String origen, String destino, LocalDate fecha, int personas);

    /** Hasta tres vuelos desde ahora, para cuando no se elige fecha. */
    List<Vuelo> buscarProximos(String origen, String destino, int personas);

    /** @throws EntidadNoEncontradaException si no existe el vuelo o esta dado de baja */
    Vuelo encontrarPorId(Integer numero);

    /**
     * @throws CapacidadExcedidaException si las plazas quedan por debajo de las reservas hechas
     * @throws OperacionNoPermitidaException si el cambio deja reservas incompatibles
     */
    Vuelo modificar(Usuario actor, Integer numero, LocalDateTime fechaYHora, String origen, String destino,
            int totalPlazas, int plazasTurista, int plazasPrimera, int precio);

    /**
     * Baja logica. Las reservas de vuelos que ya salieron siguen mostrandolo.
     *
     * @throws OperacionNoPermitidaException si el vuelo todavia no salio y tiene reservas activas
     */
    void darDeBaja(Usuario actor, Integer numero);

    /** Vuelos dados de baja, del mas reciente al mas antiguo. */
    List<Vuelo> listarBajasPara(Usuario actor);

    Vuelo reactivar(Usuario actor, Integer numero);
}
