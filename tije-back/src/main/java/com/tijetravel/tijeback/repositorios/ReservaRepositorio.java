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

/**
 * Las consultas que usa la aplicacion cuentan solo reservas activas: una
 * reserva dada de baja no ocupa plazas ni suma en el balance. findById y
 * findByTuristaCodigo traen tambien las dadas de baja para poder reactivarlas.
 */
public interface ReservaRepositorio extends JpaRepository<Reserva, Integer> {

        @Query("""
                select new com.tijetravel.tijeback.modelos.BalanceVentas(
                    coalesce(sum(case when r.pagada = true then 1 else 0 end), 0),
                    coalesce(sum(case when r.pagada = true then r.precioVuelo else 0 end), 0),
                    coalesce(sum(case when r.pagada = true then r.precioHotel else 0 end), 0),
                    coalesce(sum(case when r.pagada = true then r.total else 0 end), 0),
                    coalesce(sum(case when r.pagada = false then 1 else 0 end), 0),
                    coalesce(sum(case when r.pagada = false then r.total else 0 end), 0))
                from Reserva r where r.fechaBaja is null
                """)
        BalanceVentas balancePagadas();

        @EntityGraph("Reserva.completa")
        @Query("""
                select r from Reserva r where r.fechaBaja is null
                    and (:codigoTitular is null or r.turista.codigo = :codigoTitular
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
        Optional<Reserva> findById(Integer codigo);

        @EntityGraph("Reserva.completa")
        List<Reserva> findByFechaBajaIsNull(Sort orden);

        @EntityGraph("Reserva.completa")
        List<Reserva> findByFechaBajaIsNotNullOrderByFechaBajaDesc();

        long countByVueloNumeroAndClaseVueloAndFechaBajaIsNull(Integer numeroVuelo, ClaseVuelo claseVuelo);

        long countByVueloNumeroAndClaseVueloAndFechaBajaIsNullAndCodigoNot(
                        Integer numeroVuelo,
                        ClaseVuelo claseVuelo,
                        Integer codigoReserva);

        boolean existsByTuristaCodigoAndVueloNumeroAndFechaBajaIsNull(Integer codigoTurista, Integer numeroVuelo);

        boolean existsByTuristaCodigoAndVueloNumeroAndFechaBajaIsNullAndCodigoNot(
                        Integer codigoTurista,
                        Integer numeroVuelo,
                        Integer codigoReserva);

        /** Reservas activas del hotel que todavia no terminaron. */
        boolean existsByHotelCodigoAndFechaBajaIsNullAndFechaPartidaAfter(Integer codigoHotel, LocalDate fecha);

        /** Reservas activas de un vuelo que todavia no salio. */
        boolean existsByVueloNumeroAndFechaBajaIsNullAndFechaLlegadaGreaterThanEqual(
                        Integer numeroVuelo, LocalDate fecha);

        /** Todas las reservas del turista, activas o dadas de baja. */
        @EntityGraph("Reserva.completa")
        List<Reserva> findByTuristaCodigo(Integer codigoTurista);

        @EntityGraph("Reserva.completa")
        List<Reserva> findByTuristaCodigoAndPagadaFalseAndFechaBajaIsNull(Integer codigoTurista);

        @EntityGraph(attributePaths = "hotel")
        List<Reserva> findByVueloNumeroAndFechaBajaIsNull(Integer numeroVuelo);

        @EntityGraph(attributePaths = "vuelo")
        List<Reserva> findByHotelCodigoAndFechaBajaIsNull(Integer codigoHotel);

        @Query("""
                        select r from Reserva r
                        where r.hotel.codigo = :codigoHotel
                          and r.fechaBaja is null
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
                        where (r.turista.codigo = :codigoTitular
                           or r.turista.titular.codigo = :codigoTitular)
                          and r.fechaBaja is null
                        order by r.codigo
                        """)
        List<Reserva> listarPorTitularYFamiliares(@Param("codigoTitular") Integer codigoTitular);
}
