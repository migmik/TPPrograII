package com.tijetravel.tijeback.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;

import com.tijetravel.tijeback.modelos.Turista;

public interface TuristaRepositorio extends JpaRepository<Turista, Integer> {

    @Override
    @EntityGraph(attributePaths = {"sucursalContratacion", "titular"})
    List<Turista> findAll();

    @Override
    @EntityGraph(attributePaths = {"sucursalContratacion", "titular"})
    Optional<Turista> findById(Integer codigo);

    Optional<Turista> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndCodigoNot(String email, Integer codigo);

    @EntityGraph(attributePaths = {"sucursalContratacion", "titular"})
    List<Turista> findByTitularCodigo(Integer codigoTitular);

    boolean existsByTitularCodigo(Integer codigoTitular);

    boolean existsBySucursalContratacionCodigo(Integer codigoSucursal);
}
