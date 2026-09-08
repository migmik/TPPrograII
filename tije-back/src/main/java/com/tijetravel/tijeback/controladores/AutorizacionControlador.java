package com.tijetravel.tijeback.controladores;

import org.springframework.stereotype.Component;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Usuario;

@Component
public class AutorizacionControlador {

    public boolean tienePermiso(Usuario usuario, Permiso permiso) {
        return usuario != null && permiso != null && usuario.tienePermiso(permiso);
    }

    public void verificarPermiso(Usuario usuario, Permiso permiso) {
        if (!tienePermiso(usuario, permiso)) {
            throw new OperacionNoPermitidaException("El usuario no tiene permiso para realizar esta operacion");
        }
    }
}
