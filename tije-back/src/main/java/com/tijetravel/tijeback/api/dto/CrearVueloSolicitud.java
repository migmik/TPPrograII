package com.tijetravel.tijeback.api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CrearVueloSolicitud(
        @NotNull(message = "El numero es obligatorio")
        @Positive(message = "El numero debe ser positivo")
        Integer numero,

        @NotNull(message = "La fecha y hora son obligatorias")
        LocalDateTime fechaYHora,

        @NotBlank(message = "El origen es obligatorio")
        @Size(max = 255, message = "El origen no puede superar 255 caracteres")
        String origen,

        @NotBlank(message = "El destino es obligatorio")
        @Size(max = 255, message = "El destino no puede superar 255 caracteres")
        String destino,

        @NotNull(message = "El total de plazas es obligatorio")
        @Positive(message = "El total de plazas debe ser positivo")
        Integer totalPlazas,

        @NotNull(message = "Las plazas turista son obligatorias")
        @PositiveOrZero(message = "Las plazas turista no pueden ser negativas")
        Integer plazasTurista,

        @NotNull(message = "Las plazas de primera son obligatorias")
        @PositiveOrZero(message = "Las plazas de primera no pueden ser negativas")
        Integer plazasPrimera) {
}
