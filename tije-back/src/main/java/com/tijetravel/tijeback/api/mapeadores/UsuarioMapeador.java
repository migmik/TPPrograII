package com.tijetravel.tijeback.api.mapeadores;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.UsuarioRespuesta;
import com.tijetravel.tijeback.modelos.Usuario;

@Component
public class UsuarioMapeador {

    public UsuarioRespuesta aRespuesta(Usuario usuario) {
        return new UsuarioRespuesta(
                usuario.getCodigo(),
                usuario.getNombreUsuario(),
                usuario.getRol(),
                usuario.getCodigoTurista());
    }
}
