package com.tijetravel.tijeback.servicios;

import java.util.List;

import org.springframework.data.domain.Page;

import com.tijetravel.tijeback.modelos.Usuario;

/**
 * Operaciones sobre las cuentas de usuario. Salvo el registro publico de
 * clientes, todas requieren el permiso ADMINISTRAR_USUARIOS.
 */
public interface UsuarioServicio {

    /**
     * Crea un empleado (con DNI propio) o un cliente asociado a un turista titular,
     * segun el tipo de usuario del rol elegido.
     *
     * @throws EntidadDuplicadaException si el nombre de usuario o el DNI ya existen
     */
    Usuario crear(Usuario actor, String nombreUsuario, String contrasenia, Integer codigoRol,
            Integer codigoTurista, String dni);

    Page<Usuario> buscarPaginaPara(Usuario actor, Integer codigoRol, Integer codigo, String nombre, String dni,
            int pagina, int tamanio);

    /** Usuarios activos, o solo los del rol indicado, ordenados por codigo. */
    List<Usuario> listarPara(Usuario actor, Integer codigoRol);

    /** Registro publico: crea el turista titular y su usuario cliente en una sola operacion. */
    Usuario registrarCliente(String nombreUsuario, String contrasenia, String dni, String nombre, String apellido,
            String direccion, String email, String telefonoFijo, String telefonoCelular, Integer codigoSucursal);

    /** @throws EntidadNoEncontradaException si no existe el usuario o esta dado de baja */
    Usuario encontrarVisiblePara(Usuario actor, Integer codigo);

    /**
     * Cambia nombre, DNI, rol y, si se indica, la contrasenia. El rol nuevo tiene que
     * ser del mismo tipo de usuario.
     *
     * @throws OperacionNoPermitidaException si el actor se quita a si mismo ADMINISTRAR_USUARIOS
     */
    Usuario modificar(Usuario actor, Integer codigo, String nombreUsuario, String contrasenia, String dni,
            Integer codigoRol);

    /**
     * Baja logica: el usuario ya no puede iniciar sesion.
     *
     * @throws OperacionNoPermitidaException si es el propio actor o el ultimo que administra usuarios
     */
    void darDeBaja(Usuario actor, Integer codigo);

    /** Usuarios dados de baja, del mas reciente al mas antiguo. */
    List<Usuario> listarBajasPara(Usuario actor);

    /**
     * @throws EntidadDuplicadaException si el nombre de usuario o el DNI ya los usa otro usuario activo
     * @throws OperacionNoPermitidaException si su rol o, en un cliente, su turista estan dados de baja
     */
    Usuario reactivar(Usuario actor, Integer codigo);
}
