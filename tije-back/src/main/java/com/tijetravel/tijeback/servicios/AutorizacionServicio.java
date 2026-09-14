package com.tijetravel.tijeback.servicios;

import org.springframework.stereotype.Service;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Usuario;

@Service
public class AutorizacionServicio {

    public Integer codigoTitular(Usuario actor) {
        Integer codigo = actor.getCodigoTurista();
        if (codigo == null) {
            throw new OperacionNoPermitidaException("El cliente no tiene un turista titular asociado");
        }
        return codigo;
    }

    public boolean perteneceAlGrupoFamiliar(Usuario actor,
            com.tijetravel.tijeback.modelos.Turista turista) {
        Integer codigo = codigoTitular(actor);
        return codigo.equals(turista.getCodigo()) || codigo.equals(turista.getCodigoTitular());
    }

    public boolean tienePermiso(Usuario usuario, Permiso permiso) {
        return usuario != null && permiso != null && usuario.tienePermiso(permiso);
    }

    public void verificarPermiso(Usuario usuario, Permiso permiso) {
        if (!tienePermiso(usuario, permiso)) {
            throw new OperacionNoPermitidaException("El usuario no tiene permiso para realizar esta operacion");
        }
    }
}
