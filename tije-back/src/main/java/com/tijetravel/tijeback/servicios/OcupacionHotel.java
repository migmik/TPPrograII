package com.tijetravel.tijeback.servicios;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.tijetravel.tijeback.modelos.Reserva;

/**
 * Intervalos [llegada, partida): salir y entrar el mismo dia no ocupa dos
 * plazas.
 */
final class OcupacionHotel {
    private OcupacionHotel() {
    }

    @SuppressWarnings("null") // merge invoca Integer::sum con valores no nulos
    static int maxima(List<Reserva> reservas, LocalDate desde, LocalDate hasta, Integer ignorada) {
        Map<LocalDate, Integer> variaciones = new TreeMap<>();
        for (Reserva reserva : reservas) {
            if (ignorada != null && ignorada.equals(reserva.getCodigo()))
                continue;
            LocalDate inicio = reserva.getFechaLlegada();
            LocalDate fin = reserva.getFechaPartida();
            if (desde != null && inicio.isBefore(desde))
                inicio = desde;
            if (hasta != null && fin.isAfter(hasta))
                fin = hasta;
            if (!inicio.isBefore(fin))
                continue;
            variaciones.merge(inicio, 1, Integer::sum);
            variaciones.merge(fin, -1, Integer::sum);
        }
        int ocupadas = 0;
        int maxima = 0;
        for (int variacion : variaciones.values()) {
            ocupadas += variacion;
            maxima = Math.max(maxima, ocupadas);
        }
        return maxima;
    }
}
