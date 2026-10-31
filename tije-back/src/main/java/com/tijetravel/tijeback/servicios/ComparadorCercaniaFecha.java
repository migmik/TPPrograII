package com.tijetravel.tijeback.servicios;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

import com.tijetravel.tijeback.modelos.Vuelo;

/**
 * Ordena los vuelos segun cuantos dias los separan de la fecha buscada. Si
 * dos vuelos estan a la misma distancia, va primero el que sale antes.
 */
class ComparadorCercaniaFecha implements Comparator<Vuelo> {
    private final LocalDate fechaBuscada;

    ComparadorCercaniaFecha(LocalDate fechaBuscada) {
        this.fechaBuscada = fechaBuscada;
    }

    @Override
    public int compare(Vuelo primero, Vuelo segundo) {
        int resultado = Long.compare(distancia(primero), distancia(segundo));
        if (resultado != 0) {
            return resultado;
        }
        return primero.getFechaYHora().compareTo(segundo.getFechaYHora());
    }

    private long distancia(Vuelo vuelo) {
        return Math.abs(ChronoUnit.DAYS.between(fechaBuscada, vuelo.getFechaYHora().toLocalDate()));
    }
}
