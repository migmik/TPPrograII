package com.tijetravel.tijeback.api.mapeadores;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.api.dto.PermisoRespuesta;
import com.tijetravel.tijeback.api.dto.RolRespuesta;
import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.TipoUsuario;
import com.tijetravel.tijeback.modelos.Rol;

@Component
public class RolMapeador extends MapeadorBase<Rol, RolRespuesta> {

    @Override
    public RolRespuesta aRespuesta(Rol rol) {
        return new RolRespuesta(
                rol.getCodigo(),
                rol.getNombre(),
                rol.getTipoUsuario(),
                rol.getPermisos(),
                rol.getFechaCreacion(),
                rol.getFechaBaja());
    }

    public List<PermisoRespuesta> aPermisos(List<Permiso> permisos) {
        List<PermisoRespuesta> respuestas = new ArrayList<>();
        for (Permiso permiso : permisos) {
            respuestas.add(new PermisoRespuesta(
                    permiso.name(),
                    permiso.getDescripcion(),
                    !TipoUsuario.CLIENTE.admite(permiso)));
        }
        return respuestas;
    }
}
