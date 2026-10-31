package com.tijetravel.tijeback.api.dto;

import java.time.LocalDateTime;

public record SucursalRespuesta(
                Integer codigo,
                String direccion,
                String telefono,
                String ciudad,
                LocalDateTime fechaCreacion,
                LocalDateTime fechaBaja) {
}
