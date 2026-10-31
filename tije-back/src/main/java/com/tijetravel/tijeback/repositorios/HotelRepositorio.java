package com.tijetravel.tijeback.repositorios;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tijetravel.tijeback.modelos.Hotel;

/** Las consultas devuelven solo hoteles activos, salvo las que dicen "Baja". */
public interface HotelRepositorio extends JpaRepository<Hotel, Integer> {
    @Query("""
            select h from Hotel h where h.fechaBaja is null
            and (:ciudad is null or lower(h.ciudad) like lower(concat('%', :ciudad, '%')))
            and (:nombre is null or lower(h.nombre) like lower(concat('%', :nombre, '%')))
            """)
    Page<Hotel> buscarPagina(@Param("ciudad") String ciudad, @Param("nombre") String nombre, Pageable pagina);

    List<Hotel> findByFechaBajaIsNull(Sort orden);

    List<Hotel> findByFechaBajaIsNotNullOrderByFechaBajaDesc();

    @Query("select distinct h.ciudad from Hotel h where h.fechaBaja is null order by h.ciudad")
    List<String> listarCiudades();

    @Query("select distinct h.nombre from Hotel h where h.fechaBaja is null order by h.nombre")
    List<String> listarNombres();

    @Query("select h from Hotel h where h.fechaBaja is null and lower(h.ciudad) in :ciudades order by h.codigo")
    List<Hotel> buscarPorCiudad(@Param("ciudades") List<String> ciudades);

    boolean existsByNombreIgnoreCaseAndCiudadIgnoreCaseAndFechaBajaIsNull(String nombre, String ciudad);

    boolean existsByNombreIgnoreCaseAndCiudadIgnoreCaseAndFechaBajaIsNullAndCodigoNot(
            String nombre,
            String ciudad,
            Integer codigo);
}
