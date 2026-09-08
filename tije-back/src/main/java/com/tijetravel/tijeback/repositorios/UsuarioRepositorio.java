package com.tijetravel.tijeback.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.Usuario;

public interface UsuarioRepositorio extends GenericoRepositorio<Usuario, Integer> {

    Optional<Usuario> findByNombreUsuarioIgnoreCase(String nombreUsuario);

    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);

    boolean existsByNombreUsuarioIgnoreCaseAndCodigoNot(String nombreUsuario, Integer codigo);

    long countByRol(RolUsuario rol);

    @Query("select count(c) from Cliente c where c.turista.codigo = :codigoTurista")
    long contarClientesPorTurista(@Param("codigoTurista") Integer codigoTurista);
}
