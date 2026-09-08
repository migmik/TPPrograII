package com.tijetravel.tijeback.api.dto;

public record TuristaRespuesta(
        Integer codigo,
        String nombre,
        String apellido,
        String direccion,
        String email,
        String telefonoFijo,
        String telefonoCelular,
        Integer codigoSucursal,
        Integer codigoTitular,
        boolean titular) {
}
