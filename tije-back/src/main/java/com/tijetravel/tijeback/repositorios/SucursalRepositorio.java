package com.tijetravel.tijeback.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import com.tijetravel.tijeback.modelos.Sucursal;

public interface SucursalRepositorio extends JpaRepository<Sucursal, Integer> {

    boolean existsByDireccionIgnoreCase(String direccion);

    boolean existsByDireccionIgnoreCaseAndCodigoNot(String direccion, Integer codigo);
}
