package com.tijetravel.tijeback.api.mapeadores;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.SesionRespuesta;
import com.tijetravel.tijeback.seguridad.UsuarioAutenticado;

@Component
public class SesionMapeador {
    private final com.tijetravel.tijeback.repositorios.UsuarioRepositorio usuarios;

    public SesionMapeador(com.tijetravel.tijeback.repositorios.UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }


    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public SesionRespuesta aRespuesta(UsuarioAutenticado usuario) {
        var actual = usuarios.findById(usuario.getCodigo()).orElseThrow(() ->
                new org.springframework.security.authentication.BadCredentialsException("La cuenta ya no existe"));
        return new SesionRespuesta(
                usuario.getCodigo(),
                actual.getDni(),
                usuario.getUsername(),
                usuario.getRol(),
                usuario.getCodigoTurista());
    }
}
