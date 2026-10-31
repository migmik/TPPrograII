package com.tijetravel.tijeback.servicios;

import java.util.List;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.TipoUsuario;
import com.tijetravel.tijeback.modelos.Rol;
import com.tijetravel.tijeback.modelos.Usuario;

/**
 * Operaciones sobre los roles. Un rol agrupa los permisos que se le dan a los
 * usuarios; al cambiarlo, cambia lo que pueden hacer todos los que lo tienen.
 * Todas las operaciones requieren el permiso ADMINISTRAR_USUARIOS.
 */
public interface RolServicio {

    /** Roles activos ordenados por codigo. */
    List<Rol> listarPara(Usuario actor);

    /** Todos los permisos que existen, en el orden del enum, para armar el formulario. */
    List<Permiso> listarPermisos();

    /** @throws EntidadNoEncontradaException si no existe el rol o esta dado de baja */
    Rol encontrarPara(Usuario actor, Integer codigo);

    /**
     * @throws EntidadDuplicadaException si ya hay un rol activo con ese nombre
     * @throws IllegalArgumentException si un rol de clientes recibe permisos de empleado
     */
    Rol crear(Usuario actor, String nombre, TipoUsuario tipoUsuario, List<Permiso> permisos);

    /**
     * Cambia el nombre y los permisos. Los usuarios con este rol ven el cambio en su
     * siguiente pedido.
     *
     * @throws OperacionNoPermitidaException si el actor se quita a si mismo ADMINISTRAR_USUARIOS
     */
    Rol modificar(Usuario actor, Integer codigo, String nombre, List<Permiso> permisos);

    /** @throws OperacionNoPermitidaException si el rol tiene usuarios activos o es el ultimo rol de clientes */
    void darDeBaja(Usuario actor, Integer codigo);

    List<Rol> listarBajasPara(Usuario actor);

    /** @throws EntidadDuplicadaException si ya hay otro rol activo con ese nombre */
    Rol reactivar(Usuario actor, Integer codigo);
}
