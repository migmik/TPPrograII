package com.tijetravel.tijeback.servicios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Usuario;

/**
 * Operaciones sobre los hoteles de la agencia.
 *
 * Las altas, bajas y modificaciones requieren el permiso ADMINISTRAR_HOTELES y
 * lanzan OperacionNoPermitidaException si el actor no lo tiene.
 */
public interface HotelServicio {

    /** @throws EntidadDuplicadaException si ya existe un hotel con ese nombre en la ciudad */
    Hotel crear(Usuario actor, String nombre, String direccion, String ciudad, String telefono,
            int capacidadTotal, int precioNochePersona);

    /** Catalogo completo ordenado por codigo, precioAsc o precioDesc. Solo para el personal. */
    List<Hotel> listarPara(Usuario actor, String orden);

    List<String> listarCiudades();

    List<String> listarNombres();

    /** Busqueda paginada por ciudad y nombre. Sin filtros se trata como el catalogo completo. */
    Page<Hotel> buscarPagina(Usuario actor, String ciudad, String nombre, String orden, int pagina, int tamanio);

    /** Hoteles de la ciudad con lugar para la cantidad de personas en esas fechas. */
    List<Hotel> buscarDisponibles(String ciudad, LocalDate fechaLlegada, LocalDate fechaPartida, int personas);

    /** @throws EntidadNoEncontradaException si no existe el hotel */
    Hotel encontrarPorId(Integer codigo);

    /**
     * @throws OperacionNoPermitidaException si la nueva ciudad deja reservas incompatibles
     * @throws CapacidadExcedidaException si la capacidad queda por debajo de la ocupacion registrada
     */
    Hotel modificar(Usuario actor, Integer codigo, String nombre, String direccion, String ciudad,
            String telefono, int capacidadTotal, int precioNochePersona);

    /** @throws OperacionNoPermitidaException si el hotel tiene reservas */
    void eliminar(Usuario actor, Integer codigo);
}
