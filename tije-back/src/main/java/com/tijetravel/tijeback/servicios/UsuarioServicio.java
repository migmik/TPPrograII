package com.tijetravel.tijeback.servicios;

import java.util.List;

import org.springframework.data.domain.Page;

import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.Usuario;

/**
 * Operaciones sobre las cuentas de usuario. Salvo el registro publico de
 * clientes, todas requieren el permiso ADMINISTRAR_USUARIOS.
 */
public interface UsuarioServicio {

    /**
     * Crea un empleado (con DNI propio) o un cliente asociado a un turista titular.
     *
     * @throws EntidadDuplicadaException si el nombre de usuario o el DNI ya existen
     */
    Usuario crear(Usuario actor, String nombreUsuario, String contrasenia, RolUsuario rol, Integer codigoTurista,
            String dni);

    Page<Usuario> buscarPaginaPara(Usuario actor, RolUsuario rol, Integer codigo, String nombre, String dni,
            int pagina, int tamanio);

    /** Todos los usuarios, o solo los del rol indicado, ordenados por codigo. */
    List<Usuario> listarPara(Usuario actor, RolUsuario rol);

    /** Registro publico: crea el turista titular y su usuario cliente en una sola operacion. */
    Usuario registrarCliente(String nombreUsuario, String contrasenia, String dni, String nombre, String apellido,
            String direccion, String email, String telefonoFijo, String telefonoCelular, Integer codigoSucursal);

    /** @throws EntidadNoEncontradaException si no existe el usuario */
    Usuario encontrarVisiblePara(Usuario actor, Integer codigo);

    Usuario modificarCredenciales(Usuario actor, Integer codigo, String nombreUsuario, String contrasenia,
            String dni);

    /** @throws OperacionNoPermitidaException si es el propio actor o el ultimo administrador */
    void eliminar(Usuario actor, Integer codigo);
}
