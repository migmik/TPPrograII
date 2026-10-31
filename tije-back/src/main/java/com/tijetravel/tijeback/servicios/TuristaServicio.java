package com.tijetravel.tijeback.servicios;

import java.util.List;

import org.springframework.data.domain.Page;

import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;

/**
 * Operaciones sobre los turistas. Un turista titular pertenece a una sucursal
 * y sus familiares heredan esa sucursal.
 *
 * Las altas, bajas y modificaciones requieren el permiso ADMINISTRAR_TURISTAS.
 * Un cliente solo puede consultar a los turistas de su grupo familiar.
 */
public interface TuristaServicio {

    /**
     * Crea un titular si codigoTitular es null, o un familiar de ese titular.
     *
     * @throws EntidadDuplicadaException si el DNI o el email ya estan registrados
     */
    Turista crear(Usuario actor, String dni, String nombre, String apellido, String direccion, String email,
            String telefonoFijo, String telefonoCelular, Integer codigoSucursal, Integer codigoTitular);

    Turista crearTitular(Usuario actor, String dni, String nombre, String apellido, String direccion,
            String email, String telefonoFijo, String telefonoCelular, Integer codigoSucursal);

    /** @throws OperacionNoPermitidaException si el turista indicado no es titular */
    Turista crearFamiliar(Usuario actor, Integer codigoTitular, String dni, String nombre, String apellido,
            String direccion, String email, String telefonoFijo, String telefonoCelular);

    Page<Turista> buscarPaginaPara(Usuario actor, String dni, Boolean titular, int pagina, int tamanio);

    /** Titulares que todavia no tienen un usuario cliente asociado. */
    List<Turista> listarTitularesSinCuentaPara(Usuario actor);

    /** Turistas visibles para el actor, filtrados por DNI y por tipo (titular o familiar). */
    List<Turista> listarPara(Usuario actor, String dni, Boolean titular);

    /** Turistas pedidos por codigo; al cliente solo se le devuelven los de su grupo familiar. */
    List<Turista> listarPorCodigosPara(Usuario actor, List<Integer> codigos);

    /**
     * @throws EntidadNoEncontradaException si no existe el turista
     * @throws OperacionNoPermitidaException si el actor no puede verlo
     */
    Turista encontrarVisiblePara(Usuario actor, Integer codigo);

    /** Si el turista es titular, sus familiares pasan tambien a la nueva sucursal. */
    Turista modificar(Usuario actor, Integer codigo, String dni, String nombre, String apellido, String direccion,
            String email, String telefonoFijo, String telefonoCelular, Integer codigoSucursal);

    /** @throws OperacionNoPermitidaException si tiene reservas, familiares o un usuario asociado */
    void eliminar(Usuario actor, Integer codigo);
}
