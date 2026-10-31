package com.tijetravel.tijefront.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Un vuelo y un hotel que sirven para la misma busqueda, con el precio por
 * persona de los dos. El hotel va desde el dia del vuelo hasta la partida.
 */
public class CombinacionViaje {
    private final VueloRespuesta vuelo;
    private final HotelRespuesta hotel;
    private final LocalDate fechaLlegada;
    private final LocalDate fechaPartida;
    private final long noches;
    private final long precioPorPersona;

    public CombinacionViaje(VueloRespuesta vuelo, HotelRespuesta hotel, LocalDate fechaLlegada,
            LocalDate fechaPartida) {
        this.vuelo = vuelo;
        this.hotel = hotel;
        this.fechaLlegada = fechaLlegada;
        this.fechaPartida = fechaPartida;
        this.noches = ChronoUnit.DAYS.between(fechaLlegada, fechaPartida);
        this.precioPorPersona = vuelo.getPrecio() + hotel.getPrecioNochePersona() * noches;
    }

    public VueloRespuesta getVuelo() {
        return vuelo;
    }

    public HotelRespuesta getHotel() {
        return hotel;
    }

    public LocalDate getFechaLlegada() {
        return fechaLlegada;
    }

    public LocalDate getFechaPartida() {
        return fechaPartida;
    }

    public long getNoches() {
        return noches;
    }

    public long getPrecioPorPersona() {
        return precioPorPersona;
    }
}
