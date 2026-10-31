package com.tijetravel.tijeback.servicios;

import org.springframework.stereotype.Service;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;

/**
 * Centraliza los controles de acceso. Las reglas de cada tipo de usuario
 * estan en el modelo (tienePermiso y puedeVerTurista), aca solo se lanzan
 * las excepciones.
 */
@Service
public class AutorizacionServicio {

    public boolean tienePermiso(Usuario usuario, Permiso permiso) {
        return usuario != null && permiso != null && usuario.tienePermiso(permiso);
    }

    public void verificarPermiso(Usuario usuario, Permiso permiso) {
        if (!tienePermiso(usuario, permiso)) {
            throw new OperacionNoPermitidaException("El usuario no tiene permiso para realizar esta operacion");
        }
    }

    public void verificarAccesoATurista(Usuario usuario, Turista turista, String mensaje) {
        if (!usuario.puedeVerTurista(turista)) {
            throw new OperacionNoPermitidaException(mensaje);
        }
    }
}
