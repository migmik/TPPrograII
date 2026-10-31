package com.tijetravel.tijeback.servicios;

import java.util.List;

import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Usuario;

/**
 * Operaciones sobre las sucursales de la agencia.
 *
 * Las altas, bajas y modificaciones requieren el permiso ADMINISTRAR_SUCURSALES
 * y lanzan OperacionNoPermitidaException si el actor no lo tiene.
 */
public interface SucursalServicio {

    /** @throws EntidadDuplicadaException si ya hay una sucursal en esa direccion */
    Sucursal crear(Usuario actor, String direccion, String telefono, String ciudad);

    /** Todas las sucursales, o solo las de la ciudad indicada, ordenadas por codigo. */
    List<Sucursal> listar(String ciudad);

    List<String> listarCiudades();

    /** @throws EntidadNoEncontradaException si no existe la sucursal */
    Sucursal encontrarPorId(Integer codigo);

    /** @throws EntidadDuplicadaException si ya hay otra sucursal en esa direccion */
    Sucursal modificar(Usuario actor, Integer codigo, String direccion, String telefono, String ciudad);

    /** @throws OperacionNoPermitidaException si la sucursal tiene turistas o reservas */
    void eliminar(Usuario actor, Integer codigo);
}
