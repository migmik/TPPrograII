package com.tijetravel.tijeback.api.dto;

public record CsrfRespuesta(
        String nombreEncabezado,
        String nombreParametro,
        String token) {
}
