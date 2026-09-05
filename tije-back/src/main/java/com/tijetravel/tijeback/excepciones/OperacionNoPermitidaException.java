package com.tijetravel.tijeback.excepciones;

public class OperacionNoPermitidaException extends ReglaNegocioException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
