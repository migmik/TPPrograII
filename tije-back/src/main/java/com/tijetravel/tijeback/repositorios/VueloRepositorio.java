package com.tijetravel.tijeback.repositorios;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.modelos.Vuelo;

public interface VueloRepositorio extends JpaRepository<Vuelo, Integer> {
        @Query("select distinct v.origen from Vuelo v where v.origen is not null order by v.origen")
        List<String> listarOrigenes();

        @Query("select distinct v.destino from Vuelo v where v.destino is not null order by v.destino")
        List<String> listarDestinos();

    @Query("""
            select v from Vuelo v
            where lower(v.origen) in :origenes and lower(v.destino) in :destinos
              and v.fechaYHora >= :desde and v.fechaYHora < :hasta
              and v.plazasTurista - (select count(r) from Reserva r
                  where r.vuelo = v and r.claseVuelo = :clase) >= :personas
            order by v.fechaYHora
            """)
    List<Vuelo> buscarEnFecha(@Param("origenes") List<String> origenes,
            @Param("destinos") List<String> destinos, @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta, @Param("clase") ClaseVuelo clase,
            @Param("personas") int personas);

    @Query("""
            select v from Vuelo v
            where lower(v.origen) in :origenes and lower(v.destino) in :destinos
              and v.fechaYHora >= :desde
              and (v.fechaYHora < :excluirDesde or v.fechaYHora >= :excluirHasta)
              and v.plazasTurista - (select count(r) from Reserva r
                  where r.vuelo = v and r.claseVuelo = :clase) >= :personas
            """)
    List<Vuelo> buscarOtrosDias(@Param("origenes") List<String> origenes,
            @Param("destinos") List<String> destinos, @Param("desde") LocalDateTime desde,
            @Param("excluirDesde") LocalDateTime excluirDesde,
            @Param("excluirHasta") LocalDateTime excluirHasta,
            @Param("clase") ClaseVuelo clase, @Param("personas") int personas);
}
