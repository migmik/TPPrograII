package com.tijetravel.tijeback.api.mapeadores;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.SesionRespuesta;
import com.tijetravel.tijeback.seguridad.UsuarioAutenticado;

@Component
public class SesionMapeador {

    public SesionRespuesta aRespuesta(UsuarioAutenticado usuario) {
        return new SesionRespuesta(
                usuario.getCodigo(),
                usuario.getUsername(),
                usuario.getRol(),
                usuario.getCodigoTurista());
    }
}
