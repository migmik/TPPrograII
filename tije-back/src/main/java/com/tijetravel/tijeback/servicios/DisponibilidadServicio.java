package com.tijetravel.tijeback.servicios;

import java.time.LocalDate;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Vuelo;

/** Calcula los lugares libres en vuelos y hoteles. */
public interface DisponibilidadServicio {

    int plazasDisponiblesVuelo(Integer numeroVuelo, ClaseVuelo claseVuelo);

    int plazasDisponiblesHotel(Integer codigoHotel, LocalDate fechaLlegada, LocalDate fechaPartida);

    int plazasDisponiblesHotel(Hotel hotel, LocalDate fechaLlegada, LocalDate fechaPartida);

    /**
     * @param codigoReservaIgnorada reserva que no se cuenta (la que se esta modificando), puede ser null
     * @throws CapacidadExcedidaException si no quedan plazas en esa clase
     */
    void verificarCapacidadVuelo(Vuelo vuelo, ClaseVuelo claseVuelo, Integer codigoReservaIgnorada);

    /**
     * @param codigoReservaIgnorada reserva que no se cuenta (la que se esta modificando), puede ser null
     * @throws CapacidadExcedidaException si el hotel esta completo en alguna noche del periodo
     */
    void verificarCapacidadHotel(Hotel hotel, LocalDate fechaLlegada, LocalDate fechaPartida,
            Integer codigoReservaIgnorada);

    /** @throws IllegalArgumentException si falta una fecha o la llegada no es anterior a la partida */
    void validarFechas(LocalDate fechaLlegada, LocalDate fechaPartida);
}
