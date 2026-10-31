package com.tijetravel.tijeback.api.dto;

import java.util.List;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.TipoUsuario;

/** Datos del usuario logueado. El front usa los permisos para mostrar u ocultar opciones. */
public record SesionRespuesta(
                Integer codigo,
                String dni,
                String nombreUsuario,
                String rol,
                TipoUsuario tipoUsuario,
                Integer codigoTurista,
                List<Permiso> permisos) {
}
