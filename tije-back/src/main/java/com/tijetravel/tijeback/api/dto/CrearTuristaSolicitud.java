package com.tijetravel.tijeback.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CrearTuristaSolicitud(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255, message = "El nombre no puede superar 255 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 255, message = "El apellido no puede superar 255 caracteres")
        String apellido,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(max = 255, message = "La direccion no puede superar 255 caracteres")
        String direccion,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        @Size(max = 255, message = "El email no puede superar 255 caracteres")
        String email,

        @NotBlank(message = "El telefono fijo es obligatorio")
        @Size(max = 255, message = "El telefono fijo no puede superar 255 caracteres")
        String telefonoFijo,

        @NotBlank(message = "El telefono celular es obligatorio")
        @Size(max = 255, message = "El telefono celular no puede superar 255 caracteres")
        String telefonoCelular,

        @Positive(message = "El codigo de sucursal debe ser positivo")
        Integer codigoSucursal,

        @Positive(message = "El codigo de titular debe ser positivo")
        Integer codigoTitular) {
}
