package com.tijetravel.tijeback.api.dto;

public record BalanceRespuesta(long reservasPagadas, long ingresosVuelos,
        long ingresosHoteles, long ingresosTotales) {
}
