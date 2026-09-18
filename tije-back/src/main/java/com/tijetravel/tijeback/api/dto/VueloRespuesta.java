package com.tijetravel.tijeback.api.dto;

import java.time.LocalDateTime;

public record VueloRespuesta(
                Integer numero,
                LocalDateTime fechaYHora,
                String origen,
                String destino,
                int totalPlazas,
                int plazasTurista,
                int plazasPrimera) {
}
