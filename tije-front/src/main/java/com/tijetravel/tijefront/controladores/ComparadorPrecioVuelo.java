package com.tijetravel.tijefront.controladores;

import java.util.Comparator;

import com.tijetravel.tijefront.dto.VueloRespuesta;

/** Ordena los vuelos por precio, de menor a mayor o de mayor a menor. */
class ComparadorPrecioVuelo implements Comparator<VueloRespuesta> {
    private final boolean descendente;

    ComparadorPrecioVuelo(boolean descendente) {
        this.descendente = descendente;
    }

    @Override
    public int compare(VueloRespuesta primero, VueloRespuesta segundo) {
        int resultado = Integer.compare(primero.getPrecio(), segundo.getPrecio());
        if (descendente) {
            return -resultado;
        }
        return resultado;
    }
}
