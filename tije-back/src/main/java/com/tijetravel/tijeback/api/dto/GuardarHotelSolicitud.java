package com.tijetravel.tijeback.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record GuardarHotelSolicitud(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
        String nombre,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(max = 255, message = "La direccion no puede superar 255 caracteres")
        String direccion,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 255, message = "La ciudad no puede superar 255 caracteres")
        String ciudad,

        @NotBlank(message = "El telefono es obligatorio")
        @Size(max = 255, message = "El telefono no puede superar 255 caracteres")
        String telefono,

        @NotNull(message = "Las plazas disponibles son obligatorias")
        @PositiveOrZero(message = "Las plazas disponibles no pueden ser negativas")
        Integer plazasDisponibles) {
}
