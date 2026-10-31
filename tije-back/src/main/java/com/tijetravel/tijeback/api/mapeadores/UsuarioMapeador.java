package com.tijetravel.tijeback.api.mapeadores;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.UsuarioRespuesta;
import com.tijetravel.tijeback.modelos.Usuario;

@Component
public class UsuarioMapeador extends MapeadorBase<Usuario, UsuarioRespuesta> {

    @Override
    public UsuarioRespuesta aRespuesta(Usuario usuario) {
        return new UsuarioRespuesta(
                usuario.getCodigo(),
                usuario.getDni(),
                usuario.getNombreUsuario(),
                usuario.getRol().getCodigo(),
                usuario.getRol().getNombre(),
                usuario.getTipoUsuario(),
                usuario.getCodigoTurista(),
                usuario.getFechaCreacion(),
                usuario.getFechaBaja());
    }
}
