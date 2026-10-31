package com.tijetravel.tijeback.api.dto;

import java.time.LocalDateTime;

public record HotelRespuesta(
                Integer codigo,
                String nombre,
                String direccion,
                String ciudad,
                String telefono,
                int capacidadTotal,
                int precioNochePersona,
                LocalDateTime fechaCreacion,
                LocalDateTime fechaBaja) {
}
