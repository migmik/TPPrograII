package com.tijetravel.tijeback.repositorios;

import java.util.List;
import java.util.Optional;

import com.tijetravel.tijeback.modelos.Turista;

public interface TuristaRepositorio extends GenericoRepositorio<Turista, Integer> {

    Optional<Turista> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndCodigoNot(String email, Integer codigo);

    List<Turista> findByTitularCodigo(Integer codigoTitular);

    boolean existsByTitularCodigo(Integer codigoTitular);

    boolean existsBySucursalContratacionCodigo(Integer codigoSucursal);
}
