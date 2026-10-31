package com.tijetravel.tijeback.api.dto;

import java.time.LocalDateTime;

import com.tijetravel.tijeback.enums.TipoUsuario;

public record UsuarioRespuesta(
                Integer codigo,
                String dni,
                String nombreUsuario,
                Integer codigoRol,
                String rol,
                TipoUsuario tipoUsuario,
                Integer codigoTurista,
                LocalDateTime fechaCreacion,
                LocalDateTime fechaBaja) {
}
