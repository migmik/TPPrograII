package com.tijetravel.tijeback.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.modelos.Reserva;

public interface ReservaRepositorio extends JpaRepository<Reserva, Integer> {

    @Override
    @EntityGraph("Reserva.completa")
    List<Reserva> findAll();

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

    @EntityGraph(attributePaths = "hotel")
    List<Reserva> findByVueloNumero(Integer numeroVuelo);

    @EntityGraph(attributePaths = "vuelo")
    List<Reserva> findByHotelCodigo(Integer codigoHotel);

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
            """)
    List<Reserva> listarPorTitularYFamiliares(@Param("codigoTitular") Integer codigoTitular);
}
