package com.tijetravel.tijeback.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IniciarSesionSolicitud(
                @NotBlank(message = "El nombre de usuario es obligatorio") @Size(max = 255, message = "El nombre de usuario no puede superar 255 caracteres") String nombreUsuario,

                @NotBlank(message = "La contrasenia es obligatoria") @Size(max = 72, message = "La contrasenia no puede superar 72 caracteres") String contrasenia) {
}
