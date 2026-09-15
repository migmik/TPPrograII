package com.tijetravel.tijeback.servicios;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.excepciones.CapacidadExcedidaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.repositorios.HotelRepositorio;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;

@Service
@Transactional(readOnly = true)
public class DisponibilidadServicio {
    private final ReservaRepositorio reservaRepositorio;
    private final VueloRepositorio vueloRepositorio;
    private final HotelRepositorio hotelRepositorio;

    public DisponibilidadServicio(
            ReservaRepositorio reservaRepositorio,
            VueloRepositorio vueloRepositorio,
            HotelRepositorio hotelRepositorio) {
        this.reservaRepositorio = reservaRepositorio;
        this.vueloRepositorio = vueloRepositorio;
        this.hotelRepositorio = hotelRepositorio;
    }

    public int plazasDisponiblesVuelo(Integer numeroVuelo, ClaseVuelo claseVuelo) {
        Vuelo vuelo = encontrarVuelo(numeroVuelo);
        int capacidad = capacidadDeClase(vuelo, claseVuelo);
        long ocupadas = reservaRepositorio.countByVueloNumeroAndClaseVuelo(numeroVuelo, claseVuelo);
        return Math.max(0, capacidad - Math.toIntExact(ocupadas));
    }

    public int plazasDisponiblesHotel(
            Integer codigoHotel,
            LocalDate fechaLlegada,
            LocalDate fechaPartida) {
        validarFechas(fechaLlegada, fechaPartida);
        Hotel hotel = encontrarHotel(codigoHotel);
        int ocupadas = OcupacionHotel.maxima(
                reservaRepositorio.buscarSuperpuestasEnHotel(codigoHotel, fechaLlegada, fechaPartida),
                fechaLlegada, fechaPartida, null);
        return Math.max(0, hotel.getCapacidadTotal() - ocupadas);
    }

    public void verificarCapacidadVuelo(
            Vuelo vuelo,
            ClaseVuelo claseVuelo,
            Integer codigoReservaIgnorada) {
        if (claseVuelo == null) {
            throw new IllegalArgumentException("El campo claseVuelo es obligatorio");
        }
        long ocupadas = codigoReservaIgnorada == null
                ? reservaRepositorio.countByVueloNumeroAndClaseVuelo(vuelo.getNumero(), claseVuelo)
                : reservaRepositorio.countByVueloNumeroAndClaseVueloAndCodigoNot(
                        vuelo.getNumero(), claseVuelo, codigoReservaIgnorada);
        if (ocupadas >= capacidadDeClase(vuelo, claseVuelo)) {
            throw new CapacidadExcedidaException("No quedan plazas disponibles en la clase seleccionada");
        }
    }

    public void verificarCapacidadHotel(
            Hotel hotel,
            LocalDate fechaLlegada,
            LocalDate fechaPartida,
            Integer codigoReservaIgnorada) {
        validarFechas(fechaLlegada, fechaPartida);
        int ocupadas = OcupacionHotel.maxima(
                reservaRepositorio.buscarSuperpuestasEnHotel(hotel.getCodigo(), fechaLlegada, fechaPartida),
                fechaLlegada, fechaPartida, codigoReservaIgnorada);
        if (ocupadas >= hotel.getCapacidadTotal()) {
            throw new CapacidadExcedidaException("No quedan plazas disponibles en el hotel para esas fechas");
        }
    }

    private int capacidadDeClase(Vuelo vuelo, ClaseVuelo claseVuelo) {
        if (claseVuelo == null) {
            throw new IllegalArgumentException("El campo claseVuelo es obligatorio");
        }
        return claseVuelo == ClaseVuelo.TURISTA
                ? vuelo.getPlazasTurista()
                : vuelo.getPlazasPrimera();
    }

    public void validarFechas(LocalDate fechaLlegada, LocalDate fechaPartida) {
        if (fechaLlegada == null || fechaPartida == null || !fechaLlegada.isBefore(fechaPartida)) {
            throw new IllegalArgumentException(
                    "La fecha de llegada debe ser anterior a la fecha de partida");
        }
    }

    private Vuelo encontrarVuelo(Integer numero) {
        return vueloRepositorio.findById(numero)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro el vuelo " + numero));
    }

    private Hotel encontrarHotel(Integer codigo) {
        return hotelRepositorio.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro el hotel " + codigo));
    }

}
