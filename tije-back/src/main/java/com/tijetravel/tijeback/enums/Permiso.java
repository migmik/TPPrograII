package com.tijetravel.tijeback.enums;

/**
 * Acciones que se pueden habilitar en un rol. Hay un permiso por modulo; el
 * administrador decide en cada rol cuales tilda.
 */
public enum Permiso {
    CONSULTAR("Consultar turistas y reservas"),
    CREAR_RESERVAS("Crear y pagar reservas"),
    VER_CATALOGO_COMPLETO("Ver el catálogo completo de hoteles y vuelos"),
    VER_BALANCE("Ver el balance de ventas"),
    ADMINISTRAR_TURISTAS("Administrar turistas y familiares"),
    ADMINISTRAR_RESERVAS("Administrar reservas"),
    ADMINISTRAR_SUCURSALES("Administrar sucursales"),
    ADMINISTRAR_HOTELES("Administrar hoteles"),
    ADMINISTRAR_VUELOS("Administrar vuelos"),
    ADMINISTRAR_USUARIOS("Administrar usuarios y roles");

    private final String descripcion;

    Permiso(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
