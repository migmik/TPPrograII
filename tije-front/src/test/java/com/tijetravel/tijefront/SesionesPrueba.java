package com.tijetravel.tijefront;

import java.util.ArrayList;
import java.util.List;

import com.tijetravel.tijefront.dto.RolRespuesta;
import com.tijetravel.tijefront.dto.SesionRespuesta;

/**
 * Datos de prueba con los tres roles iniciales del backend (migracion V11):
 * Administrador (codigo 1), Vendedor (2) y Cliente (3).
 */
public final class SesionesPrueba {
    private static final List<String> TODOS = List.of("CONSULTAR", "CREAR_RESERVAS", "VER_CATALOGO_COMPLETO",
            "VER_BALANCE", "ADMINISTRAR_TURISTAS", "ADMINISTRAR_RESERVAS", "ADMINISTRAR_SUCURSALES",
            "ADMINISTRAR_HOTELES", "ADMINISTRAR_VUELOS", "ADMINISTRAR_USUARIOS");
    private static final List<String> VENDEDOR = List.of("CONSULTAR", "CREAR_RESERVAS", "VER_CATALOGO_COMPLETO",
            "ADMINISTRAR_TURISTAS", "ADMINISTRAR_RESERVAS");
    private static final List<String> CLIENTE = List.of("CONSULTAR", "CREAR_RESERVAS");

    private SesionesPrueba() {
    }

    /** Completa rol, tipo y permisos segun el rol de siempre: ADMINISTRADOR, VENDEDOR o CLIENTE. */
    public static void asignar(SesionRespuesta usuario, String rol) {
        if ("CLIENTE".equals(rol)) {
            usuario.setRol("Cliente");
            usuario.setTipoUsuario("CLIENTE");
            usuario.setPermisos(new ArrayList<String>(CLIENTE));
        } else if ("VENDEDOR".equals(rol)) {
            usuario.setRol("Vendedor");
            usuario.setTipoUsuario("EMPLEADO");
            usuario.setPermisos(new ArrayList<String>(VENDEDOR));
        } else {
            usuario.setRol("Administrador");
            usuario.setTipoUsuario("EMPLEADO");
            usuario.setPermisos(new ArrayList<String>(TODOS));
        }
    }

    public static List<RolRespuesta> roles() {
        List<RolRespuesta> roles = new ArrayList<>();
        roles.add(rol(1, "Administrador", "EMPLEADO", TODOS));
        roles.add(rol(2, "Vendedor", "EMPLEADO", VENDEDOR));
        roles.add(rol(3, "Cliente", "CLIENTE", CLIENTE));
        return roles;
    }

    /** Codigo del rol inicial con ese nombre de siempre, o 99 si no existe. */
    public static int codigoRol(String rol) {
        if ("ADMINISTRADOR".equals(rol)) {
            return 1;
        }
        if ("VENDEDOR".equals(rol)) {
            return 2;
        }
        if ("CLIENTE".equals(rol)) {
            return 3;
        }
        return 99;
    }

    private static RolRespuesta rol(int codigo, String nombre, String tipo, List<String> permisos) {
        RolRespuesta rol = new RolRespuesta();
        rol.setCodigo(codigo);
        rol.setNombre(nombre);
        rol.setTipoUsuario(tipo);
        rol.setPermisos(new ArrayList<String>(permisos));
        return rol;
    }
}
