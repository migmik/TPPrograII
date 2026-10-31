package com.tijetravel.tijeback.servicios.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.TipoHospedaje;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.BalanceVentas;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Turista;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.Vuelo;
import com.tijetravel.tijeback.repositorios.HotelRepositorio;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.repositorios.TuristaRepositorio;
import com.tijetravel.tijeback.repositorios.VueloRepositorio;
import com.tijetravel.tijeback.servicios.AutorizacionServicio;
import com.tijetravel.tijeback.servicios.BloqueoEscrituras;
import com.tijetravel.tijeback.servicios.ReservaServicio;
import com.tijetravel.tijeback.servicios.DisponibilidadServicio;

@Service
@Transactional(readOnly = true)
public class ReservaServicioImpl implements ReservaServicio {
    private final BloqueoEscrituras bloqueoEscrituras;
    private final ReservaRepositorio reservaRepositorio;
    private final TuristaRepositorio turistaRepositorio;
    private final VueloRepositorio vueloRepositorio;
    private final HotelRepositorio hotelRepositorio;
    private final AutorizacionServicio autorizacion;
    private final DisponibilidadServicio disponibilidadServicio;

    public ReservaServicioImpl(
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

    @Override
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
        autorizacion.verificarPermiso(actor, Permiso.CREAR_RESERVAS);
        bloqueoEscrituras.adquirir();
        disponibilidadServicio.validarFechas(fechaLlegada, fechaPartida);

        Turista turista = encontrarTurista(codigoTurista);
        autorizacion.verificarAccesoATurista(actor, turista,
                "El cliente solo puede reservar para su grupo familiar");
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

    @Override
    public List<Reserva> listarPara(Usuario actor) {
        autorizacion.verificarPermiso(actor, Permiso.CONSULTAR);
        Integer codigoGrupoFamiliar = actor.getCodigoGrupoFamiliar();
        if (codigoGrupoFamiliar != null) {
            return reservaRepositorio.listarPorTitularYFamiliares(codigoGrupoFamiliar);
        }
        return reservaRepositorio.findAll(Sort.by("codigo"));
    }

    @Override
    public Page<Reserva> paginaPara(
            Usuario actor,
            Integer codigoTurista,
            String dni,
            Integer numeroVuelo,
            Integer codigoHotel,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Boolean pagada,
            int pagina,
            int tamanio) {
        autorizacion.verificarPermiso(actor, Permiso.CONSULTAR);
        if (fechaDesde != null && fechaHasta != null && fechaHasta.isBefore(fechaDesde)) {
            throw new IllegalArgumentException("El rango de fechas debe estar en orden");
        }
        String dniBuscado = FiltrosConsulta.dni(dni);
        FiltrosConsulta.codigoPositivo(codigoTurista);
        FiltrosConsulta.codigoPositivo(numeroVuelo);
        FiltrosConsulta.codigoPositivo(codigoHotel);
        Pageable paginado = FiltrosConsulta.pagina(pagina, tamanio, Sort.by("codigo"));
        return reservaRepositorio.buscarPagina(actor.getCodigoGrupoFamiliar(), codigoTurista, dniBuscado,
                numeroVuelo, codigoHotel, fechaDesde, fechaHasta, pagada, paginado);
    }

    @Override
    public Reserva encontrarVisiblePara(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.CONSULTAR);
        Reserva reserva = encontrarPorId(codigo);
        autorizacion.verificarAccesoATurista(actor, reserva.getTurista(),
                "El cliente no puede consultar reservas de otro grupo familiar");
        return reserva;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reserva confirmarPago(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.CONSULTAR);
        bloqueoEscrituras.adquirir();
        Reserva reserva = encontrarVisiblePara(actor, codigo);
        reserva.confirmarPago();
        return reserva;
    }

    @Override
    public BalanceVentas balance(Usuario actor) {
        autorizacion.verificarPermiso(actor, Permiso.VER_BALANCE);
        return reservaRepositorio.balancePagadas();
    }

    @Override
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
        if (reserva.isPagada()) {
            throw new OperacionNoPermitidaException("Una reserva pagada no se puede modificar");
        }
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

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void eliminar(Usuario actor, Integer codigoReserva) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_RESERVAS);
        bloqueoEscrituras.adquirir();
        Reserva reserva = encontrarPorId(codigoReserva);
        if (reserva.isPagada()) {
            throw new OperacionNoPermitidaException("Una reserva pagada no se puede eliminar");
        }
        reservaRepositorio.delete(reserva);
    }

    private void validarCompatibilidad(Vuelo vuelo, Hotel hotel, LocalDate fechaLlegada) {
        boolean coincideFecha = fechaLlegada.equals(vuelo.getFechaYHora().toLocalDate());
        boolean coincideDestino = CiudadConsulta.misma(hotel.getCiudad(), vuelo.getDestino());
        if (!coincideFecha || !coincideDestino) {
            throw new OperacionNoPermitidaException(
                    "El hotel, el destino del vuelo y la fecha de llegada no son compatibles");
        }
    }

    private Reserva encontrarPorId(Integer codigo) {
        Reserva reserva = reservaRepositorio.findById(codigo).orElse(null);
        if (reserva == null) {
            throw new EntidadNoEncontradaException("No se encontro la reserva " + codigo);
        }
        return reserva;
    }

    private Turista encontrarTurista(Integer codigo) {
        Turista turista = turistaRepositorio.findById(codigo).orElse(null);
        if (turista == null) {
            throw new EntidadNoEncontradaException("No se encontro el turista " + codigo);
        }
        return turista;
    }

    private Vuelo encontrarVuelo(Integer numero) {
        Vuelo vuelo = vueloRepositorio.findById(numero).orElse(null);
        if (vuelo == null) {
            throw new EntidadNoEncontradaException("No se encontro el vuelo " + numero);
        }
        return vuelo;
    }

    private Hotel encontrarHotel(Integer codigo) {
        Hotel hotel = hotelRepositorio.findById(codigo).orElse(null);
        if (hotel == null) {
            throw new EntidadNoEncontradaException("No se encontro el hotel " + codigo);
        }
        return hotel;
    }
}
