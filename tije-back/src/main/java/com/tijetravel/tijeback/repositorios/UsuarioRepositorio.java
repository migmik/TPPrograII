package com.tijetravel.tijeback.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.modelos.Cliente;
import com.tijetravel.tijeback.modelos.Usuario;

/**
 * Las consultas devuelven solo usuarios activos, salvo findById, las que dicen
 * "Baja" y buscarClientesDeTurista (que se usan para reactivar). Las que
 * devuelven usuarios traen tambien el rol con sus permisos en el mismo select.
 */
public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {
        @Query(value = """
                        select u from Usuario u join fetch u.rol left join fetch treat(u as Cliente).turista t
                        where u.fechaBaja is null
                          and (:codigoRol is null or u.rol.codigo = :codigoRol)
                          and (:codigo is null or u.codigo = :codigo)
                          and (:nombre is null or lower(u.nombreUsuario) like lower(concat('%', :nombre, '%')))
                          and (:dni is null or u.dni = :dni or t.dni = :dni)
                        """, countQuery = """
                        select count(u) from Usuario u left join treat(u as Cliente).turista t
                        where u.fechaBaja is null
                          and (:codigoRol is null or u.rol.codigo = :codigoRol)
                          and (:codigo is null or u.codigo = :codigo)
                          and (:nombre is null or lower(u.nombreUsuario) like lower(concat('%', :nombre, '%')))
                          and (:dni is null or u.dni = :dni or t.dni = :dni)
                        """)
        Page<Usuario> buscarPagina(@Param("codigoRol") Integer codigoRol, @Param("codigo") Integer codigo,
                        @Param("nombre") String nombre,
                        @Param("dni") String dni,
                        Pageable pagina);

        @Query("""
                        select u from Usuario u join fetch u.rol r left join fetch r.permisos
                        left join fetch treat(u as Cliente).turista
                        where u.fechaBaja is null order by u.codigo
                        """)
        List<Usuario> listarActivos();

        @Query("""
                        select u from Usuario u join fetch u.rol r left join fetch r.permisos
                        left join fetch treat(u as Cliente).turista
                        where u.fechaBaja is null and r.codigo = :codigoRol order by u.codigo
                        """)
        List<Usuario> listarActivosPorRol(@Param("codigoRol") Integer codigoRol);

        @Query("""
                        select u from Usuario u join fetch u.rol r left join fetch r.permisos
                        left join fetch treat(u as Cliente).turista
                        where u.fechaBaja is not null order by u.fechaBaja desc
                        """)
        List<Usuario> listarDadosDeBaja();

        @Override
        @Query("""
                        select u from Usuario u join fetch u.rol r left join fetch r.permisos
                        left join fetch treat(u as Cliente).turista
                        where u.codigo = :codigo
                        """)
        Optional<Usuario> findById(@Param("codigo") Integer codigo);

        /** Usuario activo con ese nombre; lo usa el inicio de sesion. */
        @Query("""
                        select u from Usuario u join fetch u.rol r left join fetch r.permisos
                        left join fetch treat(u as Cliente).turista
                        where upper(u.nombreUsuario) = upper(:nombreUsuario) and u.fechaBaja is null
                        """)
        Usuario buscarActivoPorNombre(@Param("nombreUsuario") String nombreUsuario);

        boolean existsByNombreUsuarioIgnoreCaseAndFechaBajaIsNull(String nombreUsuario);

        boolean existsByNombreUsuarioIgnoreCaseAndFechaBajaIsNullAndCodigoNot(String nombreUsuario, Integer codigo);

        boolean existsByDniAndFechaBajaIsNull(String dni);

        boolean existsByDniAndFechaBajaIsNullAndCodigoNot(String dni, Integer codigo);

        long countByRolCodigoAndFechaBajaIsNull(Integer codigoRol);

        /** Usuarios activos cuyo rol (tambien activo) tiene el permiso indicado. */
        @Query("""
                        select count(u) from Usuario u join u.rol r join r.permisos p
                        where p = :permiso and u.fechaBaja is null and r.fechaBaja is null
                        """)
        long contarActivosConPermiso(@Param("permiso") Permiso permiso);

        @Query("select count(c) from Cliente c where c.turista.codigo = :codigoTurista and c.fechaBaja is null")
        long contarClientesPorTurista(@Param("codigoTurista") Integer codigoTurista);

        /** Cuentas de cliente del turista, activas o dadas de baja. */
        @Query("select c from Cliente c where c.turista.codigo = :codigoTurista")
        List<Cliente> buscarClientesDeTurista(@Param("codigoTurista") Integer codigoTurista);
}
