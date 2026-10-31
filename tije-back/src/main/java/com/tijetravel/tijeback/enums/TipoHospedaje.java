package com.tijetravel.tijeback.enums;

public enum TipoHospedaje {
    MEDIA_PENSION(0),
    PENSION_COMPLETA(5);

    private final int porcentajeRecargo;

    TipoHospedaje(int porcentajeRecargo) {
        this.porcentajeRecargo = porcentajeRecargo;
    }

    /** Porcentaje que se suma al precio base del hotel. */
    public int getPorcentajeRecargo() {
        return porcentajeRecargo;
    }
}
