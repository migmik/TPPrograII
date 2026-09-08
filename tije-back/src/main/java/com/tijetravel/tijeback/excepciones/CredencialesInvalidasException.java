package com.tijetravel.tijeback.excepciones;

public class CredencialesInvalidasException extends ReglaNegocioException {

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
