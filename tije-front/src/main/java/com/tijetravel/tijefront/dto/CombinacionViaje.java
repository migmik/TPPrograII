package com.tijetravel.tijefront.dto;

/** Un vuelo y un hotel que sirven para la misma busqueda, con el precio por persona de los dos. */
public class CombinacionViaje {
    private final VueloRespuesta vuelo;
    private final HotelRespuesta hotel;
    private final long precioPorPersona;

    public CombinacionViaje(VueloRespuesta vuelo, HotelRespuesta hotel, long noches) {
        this.vuelo = vuelo;
        this.hotel = hotel;
        this.precioPorPersona = vuelo.getPrecio() + hotel.getPrecioNochePersona() * noches;
    }

    public VueloRespuesta getVuelo() {
        return vuelo;
    }

    public HotelRespuesta getHotel() {
        return hotel;
    }

    public long getPrecioPorPersona() {
        return precioPorPersona;
    }
}
