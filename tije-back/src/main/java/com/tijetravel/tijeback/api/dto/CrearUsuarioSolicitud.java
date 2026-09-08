package com.tijetravel.tijeback.api.dto;

import com.tijetravel.tijeback.enums.RolUsuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearUsuarioSolicitud(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(max = 255, message = "El nombre de usuario no puede superar 255 caracteres")
        String nombreUsuario,

        @NotBlank(message = "La contrasenia es obligatoria")
        @Size(max = 72, message = "La contrasenia no puede superar 72 caracteres")
        String contrasenia,

        @NotNull(message = "El rol es obligatorio")
        RolUsuario rol,

        @Positive(message = "El codigo de turista debe ser positivo")
        Integer codigoTurista) {
}
