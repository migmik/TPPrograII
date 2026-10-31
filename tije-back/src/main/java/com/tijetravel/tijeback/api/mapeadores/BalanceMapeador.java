package com.tijetravel.tijeback.api.mapeadores;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.BalanceRespuesta;
import com.tijetravel.tijeback.modelos.BalanceVentas;

@Component
public class BalanceMapeador {

    public BalanceRespuesta aRespuesta(BalanceVentas balance) {
        return new BalanceRespuesta(
                balance.getReservasPagadas(),
                balance.getIngresosVuelos(),
                balance.getIngresosHoteles(),
                balance.getIngresosTotales(),
                balance.getReservasPendientes(),
                balance.getImportePendiente());
    }
}
