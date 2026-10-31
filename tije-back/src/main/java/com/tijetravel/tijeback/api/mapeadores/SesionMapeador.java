package com.tijetravel.tijeback.api.mapeadores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.SesionRespuesta;
import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.modelos.Usuario;

@Component
public class SesionMapeador {

    public SesionRespuesta aRespuesta(Usuario usuario) {
        List<Permiso> permisos = new ArrayList<>();
        for (Permiso permiso : Permiso.values()) {
            if (usuario.tienePermiso(permiso)) {
                permisos.add(permiso);
            }
        }
        return new SesionRespuesta(
                usuario.getCodigo(),
                usuario.getDni(),
                usuario.getNombreUsuario(),
                usuario.getRol().getNombre(),
                usuario.getTipoUsuario(),
                usuario.getCodigoTurista(),
                permisos);
    }
}
