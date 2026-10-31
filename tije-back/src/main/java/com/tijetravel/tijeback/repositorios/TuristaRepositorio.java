package com.tijetravel.tijeback.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tijetravel.tijeback.modelos.Turista;

public interface TuristaRepositorio extends JpaRepository<Turista, Integer> {

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    @Query("""
            select t from Turista t where (:dni is null or t.dni = :dni)
            and (:titular is null or (:titular = true and t.titular is null)
                 or (:titular = false and t.titular is not null))
            """)
    Page<Turista> buscarPagina(@Param("dni") String dni, @Param("titular") Boolean titular,
            Pageable pagina);

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    @Query("""
            select t from Turista t where t.titular is null and t.dni is not null
            and not exists (select c from Cliente c where c.turista = t)
            order by t.codigo
            """)
    List<Turista> listarTitularesSinCuenta();

    @Override
    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findAll(Sort orden);

    @Override
    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    Optional<Turista> findById(Integer codigo);

    Turista findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByDni(String dni);

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByTitularIsNullOrderByCodigo();

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByTitularIsNotNullOrderByCodigo();

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByCodigoInOrderByCodigo(List<Integer> codigos);

    boolean existsByDni(String dni);

    boolean existsByDniAndCodigoNot(String dni, Integer codigo);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndCodigoNot(String email, Integer codigo);

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByTitularCodigo(Integer codigoTitular);

    @EntityGraph(attributePaths = { "sucursalContratacion", "titular" })
    List<Turista> findByCodigoOrTitularCodigoOrderByCodigo(Integer codigo, Integer codigoTitular);

    boolean existsByTitularCodigo(Integer codigoTitular);

    boolean existsBySucursalContratacionCodigo(Integer codigoSucursal);
}
