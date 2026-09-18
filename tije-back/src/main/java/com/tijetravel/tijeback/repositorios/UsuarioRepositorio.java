package com.tijetravel.tijeback.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.Usuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {

        @Override
        @Query("select u from Usuario u left join fetch treat(u as Cliente).turista")
        List<Usuario> findAll();

        @Override
        @Query("""
                        select u from Usuario u
                        left join fetch treat(u as Cliente).turista
                        where u.codigo = :codigo
                        """)
        Optional<Usuario> findById(@Param("codigo") Integer codigo);

        @Query("""
                        select u from Usuario u
                        left join fetch treat(u as Cliente).turista
                        where upper(u.nombreUsuario) = upper(:nombreUsuario)
                        """)
        Optional<Usuario> findByNombreUsuarioIgnoreCase(@Param("nombreUsuario") String nombreUsuario);

        boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);

        boolean existsByNombreUsuarioIgnoreCaseAndCodigoNot(String nombreUsuario, Integer codigo);

        long countByRol(RolUsuario rol);

        @Query("select count(c) from Cliente c where c.turista.codigo = :codigoTurista")
        long contarClientesPorTurista(@Param("codigoTurista") Integer codigoTurista);
}
