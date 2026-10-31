package com.tijetravel.tijefront.dto;

public class BalanceRespuesta {
    private long reservasPagadas;
    private long ingresosVuelos;
    private long ingresosHoteles;
    private long ingresosTotales;
    private long reservasPendientes;
    private long importePendiente;

    public long getReservasPendientes() { return reservasPendientes; }
    public void setReservasPendientes(long reservasPendientes) { this.reservasPendientes = reservasPendientes; }
    public long getImportePendiente() { return importePendiente; }
    public void setImportePendiente(long importePendiente) { this.importePendiente = importePendiente; }

    public long getReservasPagadas() { return reservasPagadas; }
    public void setReservasPagadas(long reservasPagadas) { this.reservasPagadas = reservasPagadas; }
    public long getIngresosVuelos() { return ingresosVuelos; }
    public void setIngresosVuelos(long ingresosVuelos) { this.ingresosVuelos = ingresosVuelos; }
    public long getIngresosHoteles() { return ingresosHoteles; }
    public void setIngresosHoteles(long ingresosHoteles) { this.ingresosHoteles = ingresosHoteles; }
    public long getIngresosTotales() { return ingresosTotales; }
    public void setIngresosTotales(long ingresosTotales) { this.ingresosTotales = ingresosTotales; }
}
