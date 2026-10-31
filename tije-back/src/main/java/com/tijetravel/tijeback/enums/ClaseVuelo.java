package com.tijetravel.tijeback.enums;

public enum ClaseVuelo {
    TURISTA(0),
    PRIMERA(10);

    private final int porcentajeRecargo;

    ClaseVuelo(int porcentajeRecargo) {
        this.porcentajeRecargo = porcentajeRecargo;
    }

    /** Porcentaje que se suma al precio base del vuelo. */
    public int getPorcentajeRecargo() {
        return porcentajeRecargo;
    }
}
