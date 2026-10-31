package com.tijetravel.tijeback.servicios.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.CapacidadExcedidaException;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.EntidadBase;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.ValidacionModelo;
import com.tijetravel.tijeback.repositorios.HotelRepositorio;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;
import com.tijetravel.tijeback.servicios.AutorizacionServicio;
import com.tijetravel.tijeback.servicios.BloqueoEscrituras;
import com.tijetravel.tijeback.servicios.DisponibilidadServicio;
import com.tijetravel.tijeback.servicios.HotelServicio;

@Service
@Transactional(readOnly = true)
public class HotelServicioImpl implements HotelServicio {
    private final BloqueoEscrituras bloqueoEscrituras;
    private final HotelRepositorio hotelRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final AutorizacionServicio autorizacion;
    private final DisponibilidadServicio disponibilidadServicio;

    public HotelServicioImpl(
            HotelRepositorio hotelRepositorio,
            ReservaRepositorio reservaRepositorio,
            AutorizacionServicio autorizacion,
            DisponibilidadServicio disponibilidadServicio,
            BloqueoEscrituras bloqueoEscrituras) {
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.hotelRepositorio = hotelRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.autorizacion = autorizacion;
        this.disponibilidadServicio = disponibilidadServicio;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Hotel crear(Usuario actor, String nombre, String direccion, String ciudad, String telefono,
            int capacidadTotal, int precioNochePersona) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_HOTELES);
        bloqueoEscrituras.adquirir();
        Hotel hotel = new Hotel(nombre, direccion, ciudad, telefono, capacidadTotal, precioNochePersona);
        verificarNombreDisponible(hotel.getNombre(), hotel.getCiudad(), null);
        return hotelRepositorio.save(hotel);
    }

    @Override
    public List<Hotel> listarPara(Usuario actor, String orden) {
        autorizacion.verificarPermiso(actor, Permiso.VER_CATALOGO_COMPLETO,
                "Solo el personal puede consultar el catalogo completo");
        return hotelRepositorio.findByFechaBajaIsNull(ordenar(orden));
    }

    @Override
    public List<String> listarCiudades() {
        return hotelRepositorio.listarCiudades();
    }

    @Override
    public List<String> listarNombres() {
        return hotelRepositorio.listarNombres();
    }

    @Override
    public Page<Hotel> buscarPagina(Usuario actor, String ciudad, String nombre, String orden,
            int pagina, int tamanio) {
        String ciudadBuscada = FiltrosConsulta.texto(ciudad);
        String nombreBuscado = FiltrosConsulta.texto(nombre);
        if (ciudadBuscada == null && nombreBuscado == null) {
            autorizacion.verificarPermiso(actor, Permiso.VER_CATALOGO_COMPLETO,
                    "Solo el personal puede consultar el catalogo completo");
        }
        Pageable paginado = FiltrosConsulta.pagina(pagina, tamanio, ordenar(orden));
        return hotelRepositorio.buscarPagina(ciudadBuscada, nombreBuscado, paginado);
    }

    @Override
    public List<Hotel> buscarDisponibles(String ciudad, LocalDate fechaLlegada, LocalDate fechaPartida,
            int personas) {
        disponibilidadServicio.validarFechas(fechaLlegada, fechaPartida);
        if (personas < 1 || personas > 20) {
            throw new IllegalArgumentException("La cantidad de personas debe estar entre 1 y 20");
        }
        List<Hotel> disponibles = new ArrayList<>();
        for (Hotel hotel : hotelRepositorio.buscarPorCiudad(CiudadConsulta.nombres(ciudad))) {
            if (disponibilidadServicio.plazasDisponiblesHotel(hotel, fechaLlegada, fechaPartida) >= personas) {
                disponibles.add(hotel);
            }
        }
        return disponibles;
    }

    @Override
    public Hotel encontrarPorId(Integer codigo) {
        Hotel hotel = hotelRepositorio.findById(codigo).orElse(null);
        if (hotel == null || !hotel.estaActivo()) {
            throw new EntidadNoEncontradaException("No se encontro el hotel " + codigo);
        }
        return hotel;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Hotel modificar(Usuario actor, Integer codigo, String nombre, String direccion, String ciudad,
            String telefono, int capacidadTotal, int precioNochePersona) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_HOTELES);
        bloqueoEscrituras.adquirir();
        String nombreNormalizado = ValidacionModelo.textoObligatorio(nombre, "nombre");
        String ciudadNormalizada = ValidacionModelo.textoObligatorio(ciudad, "ciudad");
        verificarNombreDisponible(nombreNormalizado, ciudadNormalizada, codigo);
        Hotel hotel = encontrarPorId(codigo);

        List<Reserva> reservas = reservaRepositorio.findByHotelCodigoAndFechaBajaIsNull(codigo);
        for (Reserva reserva : reservas) {
            if (!CiudadConsulta.misma(reserva.getVuelo().getDestino(), ciudadNormalizada)) {
                throw new OperacionNoPermitidaException("La ciudad dejaría reservas incompatibles con su vuelo");
            }
        }
        int ocupacionMaxima = OcupacionHotel.maxima(reservas, null, null, null);
        if (capacidadTotal < ocupacionMaxima) {
            throw new CapacidadExcedidaException(
                    "La capacidad no puede ser menor que la ocupacion maxima registrada: " + ocupacionMaxima);
        }

        hotel.actualizarDatos(nombreNormalizado, direccion, ciudadNormalizada, telefono, capacidadTotal,
                precioNochePersona);
        return hotel;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void darDeBaja(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_HOTELES);
        bloqueoEscrituras.adquirir();
        Hotel hotel = encontrarPorId(codigo);
        if (reservaRepositorio.existsByHotelCodigoAndFechaBajaIsNullAndFechaPartidaAfter(codigo, LocalDate.now())) {
            throw new OperacionNoPermitidaException(
                    "No se puede dar de baja un hotel con reservas que todavia no terminaron");
        }
        hotel.darDeBaja(EntidadBase.ahora());
    }

    @Override
    public List<Hotel> listarBajasPara(Usuario actor) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_HOTELES);
        return hotelRepositorio.findByFechaBajaIsNotNullOrderByFechaBajaDesc();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Hotel reactivar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_HOTELES);
        bloqueoEscrituras.adquirir();
        Hotel hotel = hotelRepositorio.findById(codigo).orElse(null);
        if (hotel == null || hotel.estaActivo()) {
            throw new EntidadNoEncontradaException("No hay un hotel dado de baja con el codigo " + codigo);
        }
        verificarNombreDisponible(hotel.getNombre(), hotel.getCiudad(), codigo);
        hotel.reactivar();
        return hotel;
    }

    private void verificarNombreDisponible(String nombre, String ciudad, Integer codigoPropio) {
        boolean ocupado;
        if (codigoPropio == null) {
            ocupado = hotelRepositorio.existsByNombreIgnoreCaseAndCiudadIgnoreCaseAndFechaBajaIsNull(nombre, ciudad);
        } else {
            ocupado = hotelRepositorio.existsByNombreIgnoreCaseAndCiudadIgnoreCaseAndFechaBajaIsNullAndCodigoNot(
                    nombre, ciudad, codigoPropio);
        }
        if (ocupado) {
            throw new EntidadDuplicadaException("Ya existe ese hotel en la ciudad indicada");
        }
    }

    private Sort ordenar(String orden) {
        return FiltrosConsulta.ordenCatalogo(orden, "codigo", "precioNochePersona");
    }
}
