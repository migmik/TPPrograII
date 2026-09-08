package com.tijetravel.tijeback.repositorios;

import com.tijetravel.tijeback.modelos.Sucursal;

public interface SucursalRepositorio extends GenericoRepositorio<Sucursal, Integer> {

    boolean existsByDireccionIgnoreCase(String direccion);

    boolean existsByDireccionIgnoreCaseAndCodigoNot(String direccion, Integer codigo);
}
