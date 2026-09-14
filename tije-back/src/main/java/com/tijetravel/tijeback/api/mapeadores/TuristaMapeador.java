package com.tijetravel.tijeback.api.mapeadores;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.TuristaRespuesta;
import com.tijetravel.tijeback.modelos.Turista;

@Component
public class TuristaMapeador {

    public TuristaRespuesta aRespuesta(Turista turista) {
        return new TuristaRespuesta(
                turista.getCodigo(),
                turista.getNombre(),
                turista.getApellido(),
                turista.getDireccion(),
                turista.getEmail(),
                turista.getTelefonoFijo(),
                turista.getTelefonoCelular(),
                turista.getSucursalContratacion().getCodigo(),
                turista.getCodigoTitular(),
                turista.isTitular());
    }
}
