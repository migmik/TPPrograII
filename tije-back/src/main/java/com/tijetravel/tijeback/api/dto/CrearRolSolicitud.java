package com.tijetravel.tijeback.api.dto;

import java.util.List;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.TipoUsuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearRolSolicitud(
                @NotBlank(message = "El nombre es obligatorio") @Size(max = 60, message = "El nombre no puede superar 60 caracteres") String nombre,
                @NotNull(message = "El tipo de usuario es obligatorio") TipoUsuario tipoUsuario,
                @NotNull(message = "Los permisos son obligatorios") List<Permiso> permisos) {
}
