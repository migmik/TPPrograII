package com.tijetravel.tijeback.api.dto;

import com.tijetravel.tijeback.enums.RolUsuario;

public record UsuarioRespuesta(
                Integer codigo,
                String nombreUsuario,
                RolUsuario rol,
                Integer codigoTurista) {
}
