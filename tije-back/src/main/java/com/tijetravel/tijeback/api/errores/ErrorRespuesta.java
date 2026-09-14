package com.tijetravel.tijeback.api.errores;

import java.time.Instant;
import java.util.Map;

public record ErrorRespuesta(
        Instant fechaHora,
        int estado,
        String error,
        String mensaje,
        String ruta,
        Map<String, String> detalles) {
}
