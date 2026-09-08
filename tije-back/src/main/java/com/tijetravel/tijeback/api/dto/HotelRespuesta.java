package com.tijetravel.tijeback.api.dto;

public record HotelRespuesta(
        Integer codigo,
        String nombre,
        String direccion,
        String ciudad,
        String telefono,
        int capacidadTotal) {
}
