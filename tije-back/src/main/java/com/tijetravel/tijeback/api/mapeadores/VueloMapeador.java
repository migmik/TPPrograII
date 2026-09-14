package com.tijetravel.tijeback.api.mapeadores;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.VueloRespuesta;
import com.tijetravel.tijeback.modelos.Vuelo;

@Component
public class VueloMapeador {

    public VueloRespuesta aRespuesta(Vuelo vuelo) {
        return new VueloRespuesta(
                vuelo.getNumero(),
                vuelo.getFechaYHora(),
                vuelo.getOrigen(),
                vuelo.getDestino(),
                vuelo.getTotalPlazas(),
                vuelo.getPlazasTurista(),
                vuelo.getPlazasPrimera());
    }
}
