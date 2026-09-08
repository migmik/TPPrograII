package com.tijetravel.tijeback.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.modelos.Reserva;

public interface ReservaRepositorio extends JpaRepository<Reserva, Integer> {

    @Override
    @EntityGraph(attributePaths = {"turista", "turista.titular", "sucursalContratacion", "vuelo", "hotel"})
    List<Reserva> findAll();

    @Override
    @EntityGraph(attributePaths = {"turista", "turista.titular", "sucursalContratacion", "vuelo", "hotel"})
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

    @EntityGraph(attributePaths = {"turista", "turista.titular", "sucursalContratacion", "vuelo", "hotel"})
    List<Reserva> findByTuristaCodigo(Integer codigoTurista);

    List<Reserva> findByVueloNumero(Integer numeroVuelo);

    List<Reserva> findByHotelCodigo(Integer codigoHotel);

    @EntityGraph(attributePaths = {"turista", "turista.titular", "sucursalContratacion", "vuelo", "hotel"})
    @Query("""
            select r
            from Reserva r
            where r.turista.codigo = :codigoTitular
               or r.turista.titular.codigo = :codigoTitular
            """)
    List<Reserva> listarPorTitularYFamiliares(@Param("codigoTitular") Integer codigoTitular);
}
