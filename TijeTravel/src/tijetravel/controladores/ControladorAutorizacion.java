package tijetravel.controladores;

import tijetravel.modelos.Permiso;
import tijetravel.modelos.Usuario;

// controlador para autorizacion de usuarios
// verifica permisos de usuario segun rol
public class ControladorAutorizacion {

    // controla si el usuario tiene el permiso que solicita
    // retorna falso si no tiene permisos o info incompleta
    public boolean tienePermiso(Usuario usuario, Permiso permiso) {
        // si el usuario, rol o permiso no estan definidos, no autoriza
        if (usuario == null || usuario.getRol() == null || permiso == null)
            return false;
        // autorizacion segun rol
        switch (usuario.getRol()) {
            case CLIENTE: // consultas
                return permiso == Permiso.CONSULTAR;
            case VENDEDOR://consultas y gestion clientes/reservas
                return permiso == Permiso.CONSULTAR
                        || permiso == Permiso.ADMINISTRAR_CLIENTES
                        || permiso == Permiso.ADMINISTRAR_RESERVAS;
            case ADMINISTRADOR: // todos los permisos
                return true;
            default:
                return false;
        }
    }
}
