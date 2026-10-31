package com.tijetravel.tijeback.repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tijetravel.tijeback.modelos.Turista;

/**
 * Las consultas que usa la aplicacion devuelven solo turistas activos (las que
 * terminan en FechaBajaIsNull o filtran por fechaBaja). findById y findByTitularCodigo
 * traen tambien los dados de baja: los usan las bajas y reactivaciones.
 */
public interface TuristaRepositorio extends JpaRepository<Turista, Integer> {

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    @Query("""
            select t from Turista t where t.fechaBaja is null and (:dni is null or t.dni = :dni)
            and (:titular is null or (:titular = true and t.titular is null)
                 or (:titular = false and t.titular is not null))
            """)
    Page<Turista> buscarPagina(@Param("dni") String dni, @Param("titular") Boolean titular,
            Pageable pagina);

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    @Query("""
            select t from Turista t where t.titular is null and t.dni is not null and t.fechaBaja is null
            and not exists (select c from Cliente c where c.turista = t and c.fechaBaja is null)
            order by t.codigo
            """)
    List<Turista> listarTitularesSinCuenta();

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    @Query("""
            select t from Turista t
            where (t.codigo = :codigoTitular or t.titular.codigo = :codigoTitular) and t.fechaBaja is null
            order by t.codigo
            """)
    List<Turista> listarGrupoFamiliar(@Param("codigoTitular") Integer codigoTitular);

    @Override
    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findAll(Sort orden);

    @Override
    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    Optional<Turista> findById(Integer codigo);

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByFechaBajaIsNull(Sort orden);

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByFechaBajaIsNotNullOrderByFechaBajaDesc();

    Turista findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByDniAndFechaBajaIsNull(String dni);

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByTitularIsNullAndFechaBajaIsNullOrderByCodigo();

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByTitularIsNotNullAndFechaBajaIsNullOrderByCodigo();

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByCodigoInAndFechaBajaIsNullOrderByCodigo(List<Integer> codigos);

    boolean existsByDniAndFechaBajaIsNull(String dni);

    boolean existsByDniAndFechaBajaIsNullAndCodigoNot(String dni, Integer codigo);

    boolean existsByEmailIgnoreCaseAndFechaBajaIsNull(String email);

    boolean existsByEmailIgnoreCaseAndFechaBajaIsNullAndCodigoNot(String email, Integer codigo);

    /** Todos los familiares del titular, activos o dados de baja. */
    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByTitularCodigo(Integer codigoTitular);

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByTitularCodigoAndFechaBajaIsNull(Integer codigoTitular);

    boolean existsBySucursalContratacionCodigoAndFechaBajaIsNull(Integer codigoSucursal);
}
