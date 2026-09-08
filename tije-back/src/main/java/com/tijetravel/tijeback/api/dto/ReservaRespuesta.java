package com.tijetravel.tijeback.api.dto;

import java.time.LocalDate;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.TipoHospedaje;

public record ReservaRespuesta(
        Integer codigo,
        Integer codigoTurista,
        Integer codigoSucursalContratacion,
        Integer numeroVuelo,
        Integer codigoHotel,
        ClaseVuelo claseVuelo,
        TipoHospedaje tipoHospedaje,
        LocalDate fechaLlegada,
        LocalDate fechaPartida) {
}
