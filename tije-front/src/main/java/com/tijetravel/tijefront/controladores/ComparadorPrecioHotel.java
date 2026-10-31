package com.tijetravel.tijefront.controladores;

import java.util.Comparator;

import com.tijetravel.tijefront.dto.HotelRespuesta;

/** Ordena los hoteles por precio por noche, de menor a mayor o de mayor a menor. */
class ComparadorPrecioHotel implements Comparator<HotelRespuesta> {
    private final boolean descendente;

    ComparadorPrecioHotel(boolean descendente) {
        this.descendente = descendente;
    }

    @Override
    public int compare(HotelRespuesta primero, HotelRespuesta segundo) {
        int resultado = Integer.compare(primero.getPrecioNochePersona(), segundo.getPrecioNochePersona());
        if (descendente) {
            return -resultado;
        }
        return resultado;
    }
}
