package com.tijetravel.tijeback.api.dto;

import java.util.List;

import com.tijetravel.tijeback.enums.Permiso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** El tipo de usuario no se cambia: los usuarios del rol ya son de ese tipo. */
public record ModificarRolSolicitud(
                @NotBlank(message = "El nombre es obligatorio") @Size(max = 60, message = "El nombre no puede superar 60 caracteres") String nombre,
                @NotNull(message = "Los permisos son obligatorios") List<Permiso> permisos) {
}
