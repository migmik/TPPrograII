package com.tijetravel.tijeback.api.dto;

import java.time.LocalDate;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.TipoHospedaje;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GuardarReservaSolicitud(
        @NotNull(message = "El codigo de turista es obligatorio")
        @Positive(message = "El codigo de turista debe ser positivo")
        Integer codigoTurista,

        @NotNull(message = "El numero de vuelo es obligatorio")
        @Positive(message = "El numero de vuelo debe ser positivo")
        Integer numeroVuelo,

        @NotNull(message = "El codigo de hotel es obligatorio")
        @Positive(message = "El codigo de hotel debe ser positivo")
        Integer codigoHotel,

        @NotNull(message = "La clase de vuelo es obligatoria")
        ClaseVuelo claseVuelo,

        @NotNull(message = "El tipo de hospedaje es obligatorio")
        TipoHospedaje tipoHospedaje,

        @NotNull(message = "La fecha de llegada es obligatoria")
        LocalDate fechaLlegada,

        @NotNull(message = "La fecha de partida es obligatoria")
        LocalDate fechaPartida) {
}
