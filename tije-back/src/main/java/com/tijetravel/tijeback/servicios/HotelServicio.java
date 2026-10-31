package com.tijetravel.tijeback.servicios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Usuario;

/**
 * Operaciones sobre los hoteles de la agencia.
 *
 * Las altas, bajas, modificaciones y reactivaciones requieren el permiso
 * ADMINISTRAR_HOTELES y lanzan OperacionNoPermitidaException si el actor no lo
 * tiene. Las consultas solo devuelven hoteles activos.
 */
public interface HotelServicio {

    /** @throws EntidadDuplicadaException si ya existe un hotel activo con ese nombre en la ciudad */
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

    /** @throws EntidadNoEncontradaException si no existe el hotel o esta dado de baja */
    Hotel encontrarPorId(Integer codigo);

    /**
     * @throws OperacionNoPermitidaException si la nueva ciudad deja reservas incompatibles
     * @throws CapacidadExcedidaException si la capacidad queda por debajo de la ocupacion registrada
     */
    Hotel modificar(Usuario actor, Integer codigo, String nombre, String direccion, String ciudad,
            String telefono, int capacidadTotal, int precioNochePersona);

    /**
     * Baja logica. Las reservas que ya terminaron siguen mostrando el hotel.
     *
     * @throws OperacionNoPermitidaException si tiene reservas activas que todavia no terminaron
     */
    void darDeBaja(Usuario actor, Integer codigo);

    /** Hoteles dados de baja, del mas reciente al mas antiguo. */
    List<Hotel> listarBajasPara(Usuario actor);

    /** @throws EntidadDuplicadaException si ya hay otro hotel activo con ese nombre en la ciudad */
    Hotel reactivar(Usuario actor, Integer codigo);
}
