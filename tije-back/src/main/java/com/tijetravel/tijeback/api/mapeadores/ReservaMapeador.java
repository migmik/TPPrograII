package com.tijetravel.tijeback.api.mapeadores;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.ReservaRespuesta;
import com.tijetravel.tijeback.modelos.Reserva;

@Component
public class ReservaMapeador extends MapeadorBase<Reserva, ReservaRespuesta> {

    @Override
    public ReservaRespuesta aRespuesta(Reserva reserva) {
        return new ReservaRespuesta(
                reserva.getCodigo(),
                reserva.getTurista().getCodigo(),
                reserva.getSucursalContratacion().getCodigo(),
                reserva.getVuelo().getNumero(),
                reserva.getHotel().getCodigo(),
                reserva.getClaseVuelo(),
                reserva.getTipoHospedaje(),
                reserva.getFechaLlegada(),
                reserva.getFechaPartida(),
                reserva.getPrecioVuelo(),
                reserva.getPrecioHotel(),
                reserva.getTotal(),
                reserva.isPagada(),
                reserva.getFechaPago(),
                reserva.getFechaCreacion(),
                reserva.getFechaBaja());
    }
}
