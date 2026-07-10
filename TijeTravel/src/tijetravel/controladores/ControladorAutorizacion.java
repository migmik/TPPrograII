package tijetravel.controladores;

import tijetravel.modelos.Permiso;
import tijetravel.modelos.Usuario;

public class ControladorAutorizacion {
    public boolean tienePermiso(Usuario usuario, Permiso permiso) {
        if (usuario == null || usuario.getRol() == null || permiso == null) return false;

        switch (usuario.getRol()) {
            case CLIENTE:
                return permiso == Permiso.CONSULTAR;
            case VENDEDOR:
                return permiso == Permiso.CONSULTAR
                        || permiso == Permiso.ADMINISTRAR_CLIENTES
                        || permiso == Permiso.ADMINISTRAR_RESERVAS;
            case ADMINISTRADOR:
                return true;
            default:
                return false;
        }
    }
}
