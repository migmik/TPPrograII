package com.tijetravel.tijeback.servicios;

import java.util.List;

import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Usuario;

/**
 * Operaciones sobre las sucursales de la agencia.
 *
 * Las altas, bajas, modificaciones y reactivaciones requieren el permiso
 * ADMINISTRAR_SUCURSALES y lanzan OperacionNoPermitidaException si el actor
 * no lo tiene. Las consultas publicas solo devuelven sucursales activas.
 */
public interface SucursalServicio {

    /** @throws EntidadDuplicadaException si ya hay una sucursal activa en esa direccion */
    Sucursal crear(Usuario actor, String direccion, String telefono, String ciudad);

    /** Sucursales activas, o solo las de la ciudad indicada, ordenadas por codigo. */
    List<Sucursal> listar(String ciudad);

    List<String> listarCiudades();

    /** @throws EntidadNoEncontradaException si no existe la sucursal o esta dada de baja */
    Sucursal encontrarPorId(Integer codigo);

    /** @throws EntidadDuplicadaException si ya hay otra sucursal activa en esa direccion */
    Sucursal modificar(Usuario actor, Integer codigo, String direccion, String telefono, String ciudad);

    /**
     * Baja logica. Las reservas que se hicieron en la sucursal la siguen mostrando.
     *
     * @throws OperacionNoPermitidaException si la sucursal tiene turistas activos
     */
    void darDeBaja(Usuario actor, Integer codigo);

    /** Sucursales dadas de baja, de la mas reciente a la mas antigua. */
    List<Sucursal> listarBajasPara(Usuario actor);

    /** @throws EntidadDuplicadaException si otra sucursal activa ya usa esa direccion */
    Sucursal reactivar(Usuario actor, Integer codigo);
}
