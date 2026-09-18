package com.tijetravel.tijeback.api.dto;

import com.tijetravel.tijeback.enums.ClaseVuelo;

public record DisponibilidadVueloRespuesta(
                Integer numeroVuelo,
                ClaseVuelo claseVuelo,
                int plazasDisponibles) {
}
