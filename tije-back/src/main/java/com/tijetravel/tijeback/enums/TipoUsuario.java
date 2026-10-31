package com.tijetravel.tijeback.enums;

/**
 * Tipo de cuenta. Un empleado tiene DNI propio y puede recibir cualquier
 * permiso; un cliente esta asociado a un turista titular y solo puede tener
 * los permisos pensados para el publico.
 */
public enum TipoUsuario {
    EMPLEADO,
    CLIENTE;

    public boolean admite(Permiso permiso) {
        if (permiso == null) {
            return false;
        }
        if (this == EMPLEADO) {
            return true;
        }
        return permiso == Permiso.CONSULTAR || permiso == Permiso.CREAR_RESERVAS;
    }
}
