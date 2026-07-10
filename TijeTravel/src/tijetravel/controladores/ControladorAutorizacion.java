package tijetravel.controladores;

import tijetravel.modelos.Permiso;
import tijetravel.modelos.Usuario;

public class ControladorAutorizacion {

    public boolean tienePermiso(Usuario usuario, Permiso permiso) {
        if (usuario == null || permiso == null)
            return false;

        return usuario.tienePermiso(permiso);
    }
}
