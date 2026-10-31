package com.tijetravel.tijeback.api.mapeadores;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.SesionRespuesta;
import com.tijetravel.tijeback.modelos.Usuario;

@Component
public class SesionMapeador {

    public SesionRespuesta aRespuesta(Usuario usuario) {
        return new SesionRespuesta(
                usuario.getCodigo(),
                usuario.getDni(),
                usuario.getNombreUsuario(),
                usuario.getRol(),
                usuario.getCodigoTurista());
    }
}
