package com.tijetravel.tijeback.repositorios;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.modelos.BalanceVentas;
import com.tijetravel.tijeback.modelos.Reserva;

public interface ReservaRepositorio extends JpaRepository<Reserva, Integer> {

        @Query("""
                select new com.tijetravel.tijeback.modelos.BalanceVentas(
                    coalesce(sum(case when r.pagada = true then 1 else 0 end), 0),
                    coalesce(sum(case when r.pagada = true then r.precioVuelo else 0 end), 0),
                    coalesce(sum(case when r.pagada = true then r.precioHotel else 0 end), 0),
                    coalesce(sum(case when r.pagada = true then r.total else 0 end), 0),
                    coalesce(sum(case when r.pagada = false then 1 else 0 end), 0),
                    coalesce(sum(case when r.pagada = false then r.total else 0 end), 0))
                from Reserva r
                """)
        BalanceVentas balancePagadas();

        @EntityGraph("Reserva.completa")
        @Query("""
                select r from Reserva r where
                    (:codigoTitular is null or r.turista.codigo = :codigoTitular
                        or r.turista.titular.codigo = :codigoTitular)
                    and (:codigoTurista is null or r.turista.codigo = :codigoTurista)
                    and (:dni is null or r.turista.dni = :dni)
                    and (:numeroVuelo is null or r.vuelo.numero = :numeroVuelo)
                    and (:codigoHotel is null or r.hotel.codigo = :codigoHotel)
                    and (:desde is null or r.fechaLlegada >= :desde)
                    and (:hasta is null or r.fechaLlegada <= :hasta)
                    and (:pagada is null or r.pagada = :pagada)
                """)
        Page<Reserva> buscarPagina(@Param("codigoTitular") Integer codigoTitular,
                @Param("codigoTurista") Integer codigoTurista,
                @Param("dni") String dni, @Param("numeroVuelo") Integer numeroVuelo,
                @Param("codigoHotel") Integer codigoHotel, @Param("desde") LocalDate desde,
                @Param("hasta") LocalDate hasta, @Param("pagada") Boolean pagada, Pageable pagina);

        @Override
        @EntityGraph("Reserva.completa")
        List<Reserva> findAll();

        @Override
        @EntityGraph("Reserva.completa")
        List<Reserva> findAll(Sort orden);

        @Override
        @EntityGraph("Reserva.completa")
        Page<Reserva> findAll(Pageable pageable);

        @Override
        @EntityGraph("Reserva.completa")
        Optional<Reserva> findById(Integer codigo);

        long countByVueloNumeroAndClaseVuelo(Integer numeroVuelo, ClaseVuelo claseVuelo);

        long countByVueloNumeroAndClaseVueloAndCodigoNot(
                        Integer numeroVuelo,
                        ClaseVuelo claseVuelo,
                        Integer codigoReserva);

        boolean existsByTuristaCodigoAndVueloNumero(Integer codigoTurista, Integer numeroVuelo);

        boolean existsByTuristaCodigoAndVueloNumeroAndCodigoNot(
                        Integer codigoTurista,
                        Integer numeroVuelo,
                        Integer codigoReserva);

        boolean existsByTuristaCodigo(Integer codigoTurista);

        boolean existsBySucursalContratacionCodigo(Integer codigoSucursal);

        boolean existsByHotelCodigo(Integer codigoHotel);

        boolean existsByVueloNumero(Integer numeroVuelo);

        @EntityGraph("Reserva.completa")
        List<Reserva> findByTuristaCodigo(Integer codigoTurista);

        @EntityGraph("Reserva.completa")
        Page<Reserva> findByTuristaCodigo(Integer codigoTurista, Pageable pageable);

        @EntityGraph(attributePaths = "hotel")
        List<Reserva> findByVueloNumero(Integer numeroVuelo);

        @EntityGraph("Reserva.completa")
        Page<Reserva> findByVueloNumero(Integer numeroVuelo, Pageable pageable);

        @EntityGraph(attributePaths = "vuelo")
        List<Reserva> findByHotelCodigo(Integer codigoHotel);

        @EntityGraph("Reserva.completa")
        Page<Reserva> findByHotelCodigo(Integer codigoHotel, Pageable pageable);

        @EntityGraph("Reserva.completa")
        Page<Reserva> findByFechaLlegadaBetween(LocalDate desde, LocalDate hasta, Pageable pageable);

        @Query("""
                        select r from Reserva r
                        where r.hotel.codigo = :codigoHotel
                          and r.fechaLlegada < :hasta
                          and r.fechaPartida > :desde
                        """)
        List<Reserva> buscarSuperpuestasEnHotel(
                        @Param("codigoHotel") Integer codigoHotel,
                        @Param("desde") LocalDate desde,
                        @Param("hasta") LocalDate hasta);

        @EntityGraph("Reserva.completa")
        @Query("""
                        select r
                        from Reserva r
                        where r.turista.codigo = :codigoTitular
                           or r.turista.titular.codigo = :codigoTitular
                        order by r.codigo
                        """)
        List<Reserva> listarPorTitularYFamiliares(@Param("codigoTitular") Integer codigoTitular);
}
