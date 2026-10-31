package com.tijetravel.tijefront.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Cuerpo de error que devuelve la API; se usa para mostrar su mensaje. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorApiRespuesta {
    private String mensaje;

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
