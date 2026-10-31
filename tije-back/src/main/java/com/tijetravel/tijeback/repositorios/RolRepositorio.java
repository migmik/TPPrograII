package com.tijetravel.tijeback.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tijetravel.tijeback.enums.TipoUsuario;
import com.tijetravel.tijeback.modelos.Rol;

/** Las consultas devuelven solo roles activos, salvo findById y las que dicen "Baja". */
public interface RolRepositorio extends JpaRepository<Rol, Integer> {

    List<Rol> findByFechaBajaIsNullOrderByCodigo();

    List<Rol> findByFechaBajaIsNotNullOrderByFechaBajaDesc();

    Rol findByNombreIgnoreCaseAndFechaBajaIsNull(String nombre);

    /** Rol que se le asigna a quien se registra solo: el primer rol de clientes activo. */
    Rol findFirstByTipoUsuarioAndFechaBajaIsNullOrderByCodigo(TipoUsuario tipoUsuario);

    long countByTipoUsuarioAndFechaBajaIsNull(TipoUsuario tipoUsuario);

    boolean existsByNombreIgnoreCaseAndFechaBajaIsNull(String nombre);

    boolean existsByNombreIgnoreCaseAndFechaBajaIsNullAndCodigoNot(String nombre, Integer codigo);
}
