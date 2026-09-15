package com.tijetravel.tijefront.formularios;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;

public class ConsultaDisponibilidad {
    @NotNull(message = "Ingresá la fecha de llegada.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaLlegada;

    @NotNull(message = "Ingresá la fecha de partida.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaPartida;

    public LocalDate getFechaLlegada() { return fechaLlegada; }
    public void setFechaLlegada(LocalDate fechaLlegada) { this.fechaLlegada = fechaLlegada; }

    public LocalDate getFechaPartida() { return fechaPartida; }
    public void setFechaPartida(LocalDate fechaPartida) { this.fechaPartida = fechaPartida; }
}
