package com.tijetravel.tijeback.api.dto;

import java.time.LocalDateTime;

import com.tijetravel.tijeback.enums.RolUsuario;

public record UsuarioRespuesta(
                Integer codigo,
                String dni,
                String nombreUsuario,
                RolUsuario rol,
                Integer codigoTurista,
                LocalDateTime fechaCreacion,
                LocalDateTime fechaBaja) {
}
