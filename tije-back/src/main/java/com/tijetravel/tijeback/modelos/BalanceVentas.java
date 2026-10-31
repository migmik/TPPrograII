package com.tijetravel.tijeback.modelos;

/**
 * Resumen de ventas calculado por la base de datos. No es una entidad: solo
 * agrupa los totales de las reservas pagadas y pendientes.
 */
public class BalanceVentas {
    private final long reservasPagadas;
    private final long ingresosVuelos;
    private final long ingresosHoteles;
    private final long ingresosTotales;
    private final long reservasPendientes;
    private final long importePendiente;

    public BalanceVentas(long reservasPagadas, long ingresosVuelos, long ingresosHoteles,
            long ingresosTotales, long reservasPendientes, long importePendiente) {
        this.reservasPagadas = reservasPagadas;
        this.ingresosVuelos = ingresosVuelos;
        this.ingresosHoteles = ingresosHoteles;
        this.ingresosTotales = ingresosTotales;
        this.reservasPendientes = reservasPendientes;
        this.importePendiente = importePendiente;
    }

    public long getReservasPagadas() {
        return reservasPagadas;
    }

    public long getIngresosVuelos() {
        return ingresosVuelos;
    }

    public long getIngresosHoteles() {
        return ingresosHoteles;
    }

    public long getIngresosTotales() {
        return ingresosTotales;
    }

    public long getReservasPendientes() {
        return reservasPendientes;
    }

    public long getImportePendiente() {
        return importePendiente;
    }
}
