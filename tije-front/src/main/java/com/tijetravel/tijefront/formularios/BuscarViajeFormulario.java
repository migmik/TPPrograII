package com.tijetravel.tijefront.formularios;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BuscarViajeFormulario {
    @NotBlank(message = "Elegí una ciudad de origen.")
    private String origen;

    @NotBlank(message = "Elegí una ciudad de destino.")
    private String destino;

    @NotNull(message = "Elegí la fecha de llegada.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaLlegada;

    @NotNull(message = "Elegí la fecha de partida.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaPartida;

    @Min(value = 1, message = "Debe viajar al menos una persona.")
    @Max(value = 20, message = "Buscá para un máximo de 20 personas.")
    private int personas = 2;

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public LocalDate getFechaLlegada() { return fechaLlegada; }
    public void setFechaLlegada(LocalDate fechaLlegada) { this.fechaLlegada = fechaLlegada; }
    public LocalDate getFechaPartida() { return fechaPartida; }
    public void setFechaPartida(LocalDate fechaPartida) { this.fechaPartida = fechaPartida; }
    public int getPersonas() { return personas; }
    public void setPersonas(int personas) { this.personas = personas; }
}
