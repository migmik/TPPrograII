package com.tijetravel.tijeback.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuardarSucursalSolicitud(
                @NotBlank(message = "La direccion es obligatoria") @Size(max = 255, message = "La direccion no puede superar 255 caracteres") String direccion,

                @NotBlank(message = "El telefono es obligatorio") @Size(max = 255, message = "El telefono no puede superar 255 caracteres") String telefono) {
}
