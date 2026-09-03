package com.tijetravel.tije_back.controladores;

import com.tijetravel.tije_back.modelos.Permiso;
import com.tijetravel.tije_back.modelos.Usuario;

public class ControladorAutorizacion {

    public boolean tienePermiso(Usuario usuario, Permiso permiso) {
        if (usuario == null || permiso == null)
            return false;

        return usuario.tienePermiso(permiso);
    }
}
