package com.tijetravel.tijeback.api.dto;

public record BalanceRespuesta(long reservasPagadas, long ingresosVuelos,
        long ingresosHoteles, long ingresosTotales, long reservasPendientes,
        long importePendiente) {
    public BalanceRespuesta(long reservasPagadas, long ingresosVuelos,
            long ingresosHoteles, long ingresosTotales) {
        this(reservasPagadas, ingresosVuelos, ingresosHoteles, ingresosTotales, 0, 0);
    }
}
