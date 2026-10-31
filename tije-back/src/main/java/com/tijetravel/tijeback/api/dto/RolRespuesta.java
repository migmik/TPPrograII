package com.tijetravel.tijeback.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.TipoUsuario;

public record RolRespuesta(
                Integer codigo,
                String nombre,
                TipoUsuario tipoUsuario,
                List<Permiso> permisos,
                LocalDateTime fechaCreacion,
                LocalDateTime fechaBaja) {
}
