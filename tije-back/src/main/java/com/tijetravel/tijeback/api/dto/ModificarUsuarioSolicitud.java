package com.tijetravel.tijeback.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ModificarUsuarioSolicitud(
                @Pattern(regexp = "[0-9]{7,8}", message = "El DNI debe tener 7 u 8 digitos") String dni,
                @NotBlank(message = "El nombre de usuario es obligatorio") @Size(max = 255, message = "El nombre de usuario no puede superar 255 caracteres") String nombreUsuario,

                @NotBlank(message = "La contrasenia es obligatoria") @Size(max = 72, message = "La contrasenia no puede superar 72 caracteres") String contrasenia) {
}
