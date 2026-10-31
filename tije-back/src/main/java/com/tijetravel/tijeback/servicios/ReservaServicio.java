package com.tijetravel.tijeback.servicios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.TipoHospedaje;
import com.tijetravel.tijeback.modelos.BalanceVentas;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Usuario;

/**
 * Operaciones sobre las reservas de vuelo y hotel. Un cliente solo puede
 * reservar y consultar para su grupo familiar. Las consultas solo devuelven
 * reservas activas; una reserva dada de baja no ocupa lugar ni suma en el balance.
 */
public interface ReservaServicio {

    /**
     * El hotel tiene que estar en el destino del vuelo y la llegada coincidir con el dia del vuelo.
     *
     * @throws EntidadDuplicadaException si el turista ya tiene una reserva en ese vuelo
     * @throws CapacidadExcedidaException si no hay lugar en el vuelo o en el hotel
     */
    Reserva crear(Usuario actor, Integer codigoTurista, Integer numeroVuelo, Integer codigoHotel,
            ClaseVuelo claseVuelo, TipoHospedaje tipoHospedaje, LocalDate fechaLlegada, LocalDate fechaPartida);

    /** Reservas visibles para el actor ordenadas por codigo. */
    List<Reserva> listarPara(Usuario actor);

    /** Busqueda paginada; todos los filtros son opcionales. */
    Page<Reserva> paginaPara(Usuario actor, Integer codigoTurista, String dni, Integer numeroVuelo,
            Integer codigoHotel, LocalDate fechaDesde, LocalDate fechaHasta, Boolean pagada, int pagina,
            int tamanio);

    /**
     * @throws EntidadNoEncontradaException si no existe la reserva
     * @throws OperacionNoPermitidaException si es de otro grupo familiar
     */
    Reserva encontrarVisiblePara(Usuario actor, Integer codigo);

    /** Marca la reserva como pagada. Si ya estaba pagada no cambia nada. */
    Reserva confirmarPago(Usuario actor, Integer codigo);

    /** Totales de reservas pagadas y pendientes. Requiere el permiso VER_BALANCE. */
    BalanceVentas balance(Usuario actor);

    /** @throws OperacionNoPermitidaException si la reserva ya esta pagada */
    Reserva modificar(Usuario actor, Integer codigoReserva, Integer codigoTurista, Integer numeroVuelo,
            Integer codigoHotel, ClaseVuelo claseVuelo, TipoHospedaje tipoHospedaje, LocalDate fechaLlegada,
            LocalDate fechaPartida);

    /**
     * Baja logica: la reserva libera su lugar en el vuelo y en el hotel.
     *
     * @throws OperacionNoPermitidaException si la reserva ya esta pagada
     */
    void darDeBaja(Usuario actor, Integer codigoReserva);

    /** Reservas dadas de baja, de la mas reciente a la mas antigua. Requiere ADMINISTRAR_RESERVAS. */
    List<Reserva> listarBajasPara(Usuario actor);

    /**
     * @throws OperacionNoPermitidaException si el turista, el vuelo o el hotel estan dados de baja
     * @throws CapacidadExcedidaException si ya no hay lugar en el vuelo o en el hotel
     * @throws EntidadDuplicadaException si el turista ya tiene otra reserva activa en ese vuelo
     */
    Reserva reactivar(Usuario actor, Integer codigoReserva);
}
