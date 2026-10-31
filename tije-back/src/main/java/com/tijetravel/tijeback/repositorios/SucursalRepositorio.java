package com.tijetravel.tijeback.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import com.tijetravel.tijeback.modelos.Sucursal;

public interface SucursalRepositorio extends JpaRepository<Sucursal, Integer> {

    List<Sucursal> findByCiudadIgnoreCaseOrderByCodigo(String ciudad);

    @Query("select distinct s.ciudad from Sucursal s order by s.ciudad")
    List<String> listarCiudades();

    boolean existsByDireccionIgnoreCase(String direccion);

    boolean existsByDireccionIgnoreCaseAndCodigoNot(String direccion, Integer codigo);
}
