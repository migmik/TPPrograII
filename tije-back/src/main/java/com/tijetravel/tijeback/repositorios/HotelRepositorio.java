package com.tijetravel.tijeback.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.tijetravel.tijeback.modelos.Hotel;

public interface HotelRepositorio extends JpaRepository<Hotel, Integer> {

    @Query("select distinct h.ciudad from Hotel h where h.ciudad is not null order by h.ciudad")
    List<String> listarCiudades();

    @Query("select h from Hotel h where lower(h.ciudad) in :ciudades order by h.codigo")
    List<Hotel> buscarPorCiudad(@Param("ciudades") List<String> ciudades);

    boolean existsByNombreIgnoreCaseAndCiudadIgnoreCase(String nombre, String ciudad);

    boolean existsByNombreIgnoreCaseAndCiudadIgnoreCaseAndCodigoNot(
            String nombre,
            String ciudad,
            Integer codigo);
}
