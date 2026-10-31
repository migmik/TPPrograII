package com.tijetravel.tijeback.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.Usuario;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {
    @Query(value = """
            select u from Usuario u left join fetch treat(u as Cliente).turista t
            where (:rol is null or u.rol = :rol)
              and (:codigo is null or u.codigo = :codigo)
              and (:nombre is null or lower(u.nombreUsuario) like lower(concat('%', :nombre, '%')))
              and (:dni is null or u.dni = :dni or t.dni = :dni)
            """, countQuery = """
            select count(u) from Usuario u left join treat(u as Cliente).turista t
            where (:rol is null or u.rol = :rol)
              and (:codigo is null or u.codigo = :codigo)
              and (:nombre is null or lower(u.nombreUsuario) like lower(concat('%', :nombre, '%')))
              and (:dni is null or u.dni = :dni or t.dni = :dni)
            """)
    Page<Usuario> buscarPagina(@Param("rol") RolUsuario rol, @Param("codigo") Integer codigo,
            @Param("nombre") String nombre,
            @Param("dni") String dni,
            Pageable pagina);
    boolean existsByDni(String dni);

    boolean existsByDniAndCodigoNot(String dni, Integer codigo);


        @Override
        @Query("select u from Usuario u left join fetch treat(u as Cliente).turista order by u.codigo")
        List<Usuario> findAll();

        @Query("""
                        select u from Usuario u left join fetch treat(u as Cliente).turista
                        where u.rol = :rol order by u.codigo
                        """)
        List<Usuario> findByRol(@Param("rol") RolUsuario rol);

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
