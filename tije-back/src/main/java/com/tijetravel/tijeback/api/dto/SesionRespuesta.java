package com.tijetravel.tijeback.api.dto;

import com.tijetravel.tijeback.enums.RolUsuario;

public record SesionRespuesta(
        Integer codigo,
        String nombreUsuario,
        RolUsuario rol,
        Integer codigoTurista) {
}
