package com.tijetravel.tijeback.api.dto;

/** Un permiso con su texto para mostrar y si solo se le puede dar a empleados. */
public record PermisoRespuesta(
                String nombre,
                String descripcion,
                boolean soloEmpleados) {
}
