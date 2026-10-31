package com.tijetravel.tijeback.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tijetravel.tijeback.modelos.Sucursal;

/** Las consultas devuelven solo sucursales activas, salvo las que dicen "Baja". */
public interface SucursalRepositorio extends JpaRepository<Sucursal, Integer> {

    List<Sucursal> findByFechaBajaIsNullOrderByCodigo();

    List<Sucursal> findByCiudadIgnoreCaseAndFechaBajaIsNullOrderByCodigo(String ciudad);

    List<Sucursal> findByFechaBajaIsNotNullOrderByFechaBajaDesc();

    @Query("select distinct s.ciudad from Sucursal s where s.fechaBaja is null order by s.ciudad")
    List<String> listarCiudades();

    boolean existsByDireccionIgnoreCaseAndFechaBajaIsNull(String direccion);

    boolean existsByDireccionIgnoreCaseAndFechaBajaIsNullAndCodigoNot(String direccion, Integer codigo);
}
