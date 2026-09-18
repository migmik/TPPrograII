package com.tijetravel.tijeback.servicios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.enums.TipoHospedaje;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.repositorios.HotelRepositorio;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;

@Service
@Transactional(readOnly = true)
public class ReservaServicio {
    private final BloqueoEscrituras bloqueoEscrituras;
    private final ReservaRepositorio reservaRepositorio;
    private final TuristaRepositorio turistaRepositorio;
    private final VueloRepositorio vueloRepositorio;
    private final HotelRepositorio hotelRepositorio;
    private final AutorizacionServicio autorizacion;
    private final DisponibilidadServicio disponibilidadServicio;

    public ReservaServicio(
            ReservaRepositorio reservaRepositorio,
            TuristaRepositorio turistaRepositorio,
            VueloRepositorio vueloRepositorio,
            HotelRepositorio hotelRepositorio,
            AutorizacionServicio autorizacion,
            DisponibilidadServicio disponibilidadServicio,
            BloqueoEscrituras bloqueoEscrituras) {
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.reservaRepositorio = reservaRepositorio;
        this.turistaRepositorio = turistaRepositorio;
        this.vueloRepositorio = vueloRepositorio;
        this.hotelRepositorio = hotelRepositorio;
        this.autorizacion = autorizacion;
        this.disponibilidadServicio = disponibilidadServicio;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reserva crear(
            Usuario actor,
            Integer codigoTurista,
            Integer numeroVuelo,
            Integer codigoHotel,
            ClaseVuelo claseVuelo,
            TipoHospedaje tipoHospedaje,
            LocalDate fechaLlegada,
            LocalDate fechaPartida) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_RESERVAS);
        bloqueoEscrituras.adquirir();
        disponibilidadServicio.validarFechas(fechaLlegada, fechaPartida);

        Turista turista = encontrarTurista(codigoTurista);
        Vuelo vuelo = encontrarVuelo(numeroVuelo);
        Hotel hotel = encontrarHotel(codigoHotel);

        if (reservaRepositorio.existsByTuristaCodigoAndVueloNumero(codigoTurista, numeroVuelo)) {
            throw new EntidadDuplicadaException("El turista ya tiene una reserva para ese vuelo");
        }
        validarCompatibilidad(vuelo, hotel, fechaLlegada);
        disponibilidadServicio.verificarCapacidadVuelo(vuelo, claseVuelo, null);
        disponibilidadServicio.verificarCapacidadHotel(hotel, fechaLlegada, fechaPartida, null);

        Reserva reserva = new Reserva(
                turista,
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

    public List<Reserva> listarPara(Usuario actor) {
        autorizacion.verificarPermiso(actor, Permiso.CONSULTAR);
        if (actor.getRol() == RolUsuario.CLIENTE) {
            return listarPorTitularYFamiliares(autorizacion.codigoTitular(actor));
        }
        return listar();
    }

    public Reserva encontrarPorId(Integer codigo) {
        return reservaRepositorio.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro la reserva " + codigo));
    }

    public Reserva encontrarVisiblePara(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.CONSULTAR);
        Reserva reserva = encontrarPorId(codigo);
        if (actor.getRol() == RolUsuario.CLIENTE
                && !autorizacion.perteneceAlGrupoFamiliar(actor, reserva.getTurista())) {
            throw new OperacionNoPermitidaException(
                    "El cliente no puede consultar reservas de otro grupo familiar");
        }
        return reserva;
    }

    public List<Reserva> listarPorTitularYFamiliares(Integer codigoTitular) {
        Turista titular = encontrarTurista(codigoTitular);
        if (!titular.isTitular()) {
            throw new OperacionNoPermitidaException("El turista indicado no es titular");
        }
        return reservaRepositorio.listarPorTitularYFamiliares(codigoTitular);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reserva modificar(
            Usuario actor,
            Integer codigoReserva,
            Integer codigoTurista,
            Integer numeroVuelo,
            Integer codigoHotel,
            ClaseVuelo claseVuelo,
            TipoHospedaje tipoHospedaje,
            LocalDate fechaLlegada,
            LocalDate fechaPartida) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_RESERVAS);
        bloqueoEscrituras.adquirir();
        disponibilidadServicio.validarFechas(fechaLlegada, fechaPartida);

        Reserva reserva = encontrarPorId(codigoReserva);
        Turista turista = encontrarTurista(codigoTurista);
        Vuelo vuelo = encontrarVuelo(numeroVuelo);
        Hotel hotel = encontrarHotel(codigoHotel);

        if (reservaRepositorio.existsByTuristaCodigoAndVueloNumeroAndCodigoNot(
                codigoTurista, numeroVuelo, codigoReserva)) {
            throw new EntidadDuplicadaException("El turista ya tiene una reserva para ese vuelo");
        }
        validarCompatibilidad(vuelo, hotel, fechaLlegada);
        disponibilidadServicio.verificarCapacidadVuelo(vuelo, claseVuelo, codigoReserva);
        disponibilidadServicio.verificarCapacidadHotel(hotel, fechaLlegada, fechaPartida, codigoReserva);

        reserva.actualizarDatos(
                turista,
                vuelo,
                hotel,
                claseVuelo,
                tipoHospedaje,
                fechaLlegada,
                fechaPartida);
        return reserva;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void eliminar(Usuario actor, Integer codigoReserva) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_RESERVAS);
        bloqueoEscrituras.adquirir();
        Reserva reserva = encontrarPorId(codigoReserva);
        reservaRepositorio.delete(reserva);
    }

    private void validarCompatibilidad(Vuelo vuelo, Hotel hotel, LocalDate fechaLlegada) {
        boolean coincideFecha = fechaLlegada.equals(vuelo.getFechaYHora().toLocalDate());
        boolean coincideDestino = hotel.getCiudad().trim().equalsIgnoreCase(vuelo.getDestino().trim());
        if (!coincideFecha || !coincideDestino) {
            throw new OperacionNoPermitidaException(
                    "El hotel, el destino del vuelo y la fecha de llegada no son compatibles");
        }
    }

    private Turista encontrarTurista(Integer codigo) {
        return turistaRepositorio.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro el turista " + codigo));
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
