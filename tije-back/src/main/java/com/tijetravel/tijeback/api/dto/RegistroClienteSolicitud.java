package com.tijetravel.tijeback.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegistroClienteSolicitud(
        @NotBlank @Size(max = 255) String nombreUsuario,
        @NotBlank @Size(max = 72) String contrasenia,
        @NotBlank @Pattern(regexp = "[0-9]{7,8}") String dni,
        @NotBlank @Size(max = 255) String nombre,
        @NotBlank @Size(max = 255) String apellido,
        @NotBlank @Size(max = 255) String direccion,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 255) String telefonoFijo,
        @NotBlank @Size(max = 255) String telefonoCelular,
        @NotNull @Positive Integer codigoSucursal) {
}