package com.tijetravel.tijeback.api.dto;

import java.time.LocalDateTime;

public record TuristaRespuesta(
                Integer codigo,
                String dni,
                String nombre,
                String apellido,
                String direccion,
                String email,
                String telefonoFijo,
                String telefonoCelular,
                Integer codigoSucursal,
                Integer codigoTitular,
                boolean titular,
                LocalDateTime fechaCreacion,
                LocalDateTime fechaBaja) {
}
