package com.tijetravel.tijeback.api.dto;

import java.time.LocalDate;

public record DisponibilidadHotelRespuesta(
                Integer codigoHotel,
                LocalDate fechaLlegada,
                LocalDate fechaPartida,
                int plazasDisponibles) {
}
