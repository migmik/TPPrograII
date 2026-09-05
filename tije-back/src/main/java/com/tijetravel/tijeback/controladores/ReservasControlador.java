package com.tijetravel.tijeback.controladores;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.TipoHospedaje;
import com.tijetravel.tijeback.excepciones.CapacidadExcedidaException;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Sucursal;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.repositorios.HotelRepositorio;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.SucursalRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;

@Service
@Transactional(readOnly = true)
public class ReservasControlador {
    private final ReservaRepositorio reservaRepositorio;
    private final TuristaRepositorio turistaRepositorio;
    private final SucursalRepositorio sucursalRepositorio;
    private final VueloRepositorio vueloRepositorio;
    private final HotelRepositorio hotelRepositorio;
    private final AutorizacionControlador autorizacion;

    public ReservasControlador(
            ReservaRepositorio reservaRepositorio,
            TuristaRepositorio turistaRepositorio,
            SucursalRepositorio sucursalRepositorio,
            VueloRepositorio vueloRepositorio,
            HotelRepositorio hotelRepositorio,
            AutorizacionControlador autorizacion) {
        this.reservaRepositorio = reservaRepositorio;
        this.turistaRepositorio = turistaRepositorio;
        this.sucursalRepositorio = sucursalRepositorio;
        this.vueloRepositorio = vueloRepositorio;
        this.hotelRepositorio = hotelRepositorio;
        this.autorizacion = autorizacion;
    }

    @Transactional
    public Reserva ingresar(
            Usuario actor,
            Integer codigoTurista,
            Integer codigoSucursal,
            Integer numeroVuelo,
            Integer codigoHotel,
            ClaseVuelo claseVuelo,
            TipoHospedaje tipoHospedaje,
            LocalDate fechaLlegada,
            LocalDate fechaPartida) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_RESERVAS);
        validarFechas(fechaLlegada, fechaPartida);

        Turista turista = encontrarTurista(codigoTurista);
        Sucursal sucursal = encontrarSucursal(codigoSucursal);
        Vuelo vuelo = encontrarVuelo(numeroVuelo);
        Hotel hotel = encontrarHotel(codigoHotel);

        if (reservaRepositorio.existsByTuristaCodigoAndVueloNumero(codigoTurista, numeroVuelo)) {
            throw new EntidadDuplicadaException("El turista ya tiene una reserva para ese vuelo");
        }
        validarCompatibilidad(vuelo, hotel, fechaLlegada);
        verificarCapacidadVuelo(vuelo, claseVuelo, null);
        verificarCapacidadHotel(hotel, fechaLlegada, fechaPartida, null);

        Reserva reserva = new Reserva(
                turista,
                sucursal,
                vuelo,
                hotel,
                claseVuelo,
                tipoHospedaje,
                fechaLlegada,
                fechaPartida);
        return reservaRepositorio.save(reserva);
    }

    public List<Reserva> listar() {
        return reservaRepositorio.findAll();
    }

    public Reserva encontrarPorId(Integer codigo) {
        return reservaRepositorio.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro la reserva " + codigo));
    }

    public List<Reserva> listarPorTurista(Integer codigoTurista) {
        encontrarTurista(codigoTurista);
        return reservaRepositorio.findByTuristaCodigo(codigoTurista);
    }

    public List<Reserva> listarPorTitularYFamiliares(Integer codigoTitular) {
        Turista titular = encontrarTurista(codigoTitular);
        if (!titular.isTitular()) {
            throw new OperacionNoPermitidaException("El turista indicado no es titular");
        }
        return reservaRepositorio.listarPorTitularYFamiliares(codigoTitular);
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
        long ocupadas = reservaRepositorio
                .countByHotelCodigoAndFechaLlegadaLessThanAndFechaPartidaGreaterThan(
                        codigoHotel, fechaPartida, fechaLlegada);
        return Math.max(0, hotel.getPlazasDisponibles() - Math.toIntExact(ocupadas));
    }

    @Transactional
    public Reserva modificar(
            Usuario actor,
            Integer codigoReserva,
            Integer codigoTurista,
            Integer codigoSucursal,
            Integer numeroVuelo,
            Integer codigoHotel,
            ClaseVuelo claseVuelo,
            TipoHospedaje tipoHospedaje,
            LocalDate fechaLlegada,
            LocalDate fechaPartida) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_RESERVAS);
        validarFechas(fechaLlegada, fechaPartida);

        Reserva reserva = encontrarPorId(codigoReserva);
        Turista turista = encontrarTurista(codigoTurista);
        Sucursal sucursal = encontrarSucursal(codigoSucursal);
        Vuelo vuelo = encontrarVuelo(numeroVuelo);
        Hotel hotel = encontrarHotel(codigoHotel);

        if (reservaRepositorio.existsByTuristaCodigoAndVueloNumeroAndCodigoNot(
                codigoTurista, numeroVuelo, codigoReserva)) {
            throw new EntidadDuplicadaException("El turista ya tiene una reserva para ese vuelo");
        }
        validarCompatibilidad(vuelo, hotel, fechaLlegada);
        verificarCapacidadVuelo(vuelo, claseVuelo, codigoReserva);
        verificarCapacidadHotel(hotel, fechaLlegada, fechaPartida, codigoReserva);

        reserva.actualizarDatos(
                turista,
                sucursal,
                vuelo,
                hotel,
                claseVuelo,
                tipoHospedaje,
                fechaLlegada,
                fechaPartida);
        return reservaRepositorio.save(reserva);
    }

    @Transactional
    public void eliminar(Usuario actor, Integer codigoReserva) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_RESERVAS);
        Reserva reserva = encontrarPorId(codigoReserva);
        reservaRepositorio.delete(reserva);
    }

    private void verificarCapacidadVuelo(
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

    private void verificarCapacidadHotel(
            Hotel hotel,
            LocalDate fechaLlegada,
            LocalDate fechaPartida,
            Integer codigoReservaIgnorada) {
        long ocupadas = codigoReservaIgnorada == null
                ? reservaRepositorio.countByHotelCodigoAndFechaLlegadaLessThanAndFechaPartidaGreaterThan(
                        hotel.getCodigo(), fechaPartida, fechaLlegada)
                : reservaRepositorio.countByHotelCodigoAndCodigoNotAndFechaLlegadaLessThanAndFechaPartidaGreaterThan(
                        hotel.getCodigo(), codigoReservaIgnorada, fechaPartida, fechaLlegada);
        if (ocupadas >= hotel.getPlazasDisponibles()) {
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

    private void validarCompatibilidad(Vuelo vuelo, Hotel hotel, LocalDate fechaLlegada) {
        boolean coincideFecha = fechaLlegada.equals(vuelo.getFechaYHora().toLocalDate());
        boolean coincideDestino = hotel.getCiudad().trim().equalsIgnoreCase(vuelo.getDestino().trim());
        if (!coincideFecha || !coincideDestino) {
            throw new OperacionNoPermitidaException(
                    "El hotel, el destino del vuelo y la fecha de llegada no son compatibles");
        }
    }

    private void validarFechas(LocalDate fechaLlegada, LocalDate fechaPartida) {
        if (fechaLlegada == null || fechaPartida == null || !fechaLlegada.isBefore(fechaPartida)) {
            throw new IllegalArgumentException(
                    "La fecha de llegada debe ser anterior a la fecha de partida");
        }
    }

    private Turista encontrarTurista(Integer codigo) {
        return turistaRepositorio.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro el turista " + codigo));
    }

    private Sucursal encontrarSucursal(Integer codigo) {
        return sucursalRepositorio.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro la sucursal " + codigo));
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
