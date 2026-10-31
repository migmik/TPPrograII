package com.tijetravel.tijeback;

import java.util.ArrayList;
import java.util.List;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.TipoUsuario;
import com.tijetravel.tijeback.modelos.Rol;

/**
 * Roles para las pruebas unitarias, con los mismos permisos que los roles
 * iniciales de la migracion V11. No se guardan en la base.
 */
public final class RolesPrueba {
    private RolesPrueba() {
    }

    public static Rol administrador() {
        List<Permiso> todos = new ArrayList<>();
        for (Permiso permiso : Permiso.values()) {
            todos.add(permiso);
        }
        return new Rol("Administrador", TipoUsuario.EMPLEADO, todos);
    }

    public static Rol vendedor() {
        return new Rol("Vendedor", TipoUsuario.EMPLEADO, List.of(
                Permiso.CONSULTAR,
                Permiso.CREAR_RESERVAS,
                Permiso.VER_CATALOGO_COMPLETO,
                Permiso.ADMINISTRAR_TURISTAS,
                Permiso.ADMINISTRAR_RESERVAS));
    }

    public static Rol cliente() {
        return new Rol("Cliente", TipoUsuario.CLIENTE, List.of(Permiso.CONSULTAR, Permiso.CREAR_RESERVAS));
    }
}
