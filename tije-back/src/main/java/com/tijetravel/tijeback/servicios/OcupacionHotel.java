package com.tijetravel.tijeback.servicios;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.tijetravel.tijeback.modelos.Reserva;

/**
 * Calcula la ocupacion maxima de un hotel en un periodo. Cada reserva ocupa
 * el intervalo [llegada, partida): salir y entrar el mismo dia no ocupa dos
 * plazas.
 */
final class OcupacionHotel {
    private OcupacionHotel() {
    }

    static int maxima(List<Reserva> reservas, LocalDate desde, LocalDate hasta, Integer ignorada) {
        // Por cada dia se guarda cuantas personas entran (+1) o salen (-1).
        Map<LocalDate, Integer> variaciones = new TreeMap<>();
        for (Reserva reserva : reservas) {
            if (ignorada != null && ignorada.equals(reserva.getCodigo())) {
                continue;
            }
            LocalDate inicio = reserva.getFechaLlegada();
            LocalDate fin = reserva.getFechaPartida();
            if (desde != null && inicio.isBefore(desde)) {
                inicio = desde;
            }
            if (hasta != null && fin.isAfter(hasta)) {
                fin = hasta;
            }
            if (!inicio.isBefore(fin)) {
                continue;
            }
            sumar(variaciones, inicio, 1);
            sumar(variaciones, fin, -1);
        }
        // Se recorren los dias en orden acumulando la ocupacion.
        int ocupadas = 0;
        int maxima = 0;
        for (int variacion : variaciones.values()) {
            ocupadas += variacion;
            if (ocupadas > maxima) {
                maxima = ocupadas;
            }
        }
        return maxima;
    }

    private static void sumar(Map<LocalDate, Integer> variaciones, LocalDate dia, int cantidad) {
        Integer actual = variaciones.get(dia);
        if (actual == null) {
            actual = 0;
        }
        variaciones.put(dia, actual + cantidad);
    }
}
