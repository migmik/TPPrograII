package com.tijetravel.tijeback.api.mapeadores;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.HotelRespuesta;
import com.tijetravel.tijeback.modelos.Hotel;

@Component
public class HotelMapeador {

    public HotelRespuesta aRespuesta(Hotel hotel) {
        return new HotelRespuesta(
                hotel.getCodigo(),
                hotel.getNombre(),
                hotel.getDireccion(),
                hotel.getCiudad(),
                hotel.getTelefono(),
                hotel.getCapacidadTotal());
    }
}
