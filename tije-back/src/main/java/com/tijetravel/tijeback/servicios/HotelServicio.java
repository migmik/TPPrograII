package com.tijetravel.tijeback.servicios;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.CapacidadExcedidaException;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.modelos.ValidacionModelo;
import com.tijetravel.tijeback.repositorios.HotelRepositorio;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;

@Service
@Transactional(readOnly = true)
public class HotelServicio {
    private final BloqueoEscrituras bloqueoEscrituras;
    private final HotelRepositorio hotelRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final AutorizacionServicio autorizacion;

    public HotelServicio(
            HotelRepositorio hotelRepositorio,
            ReservaRepositorio reservaRepositorio,
            AutorizacionServicio autorizacion,
            BloqueoEscrituras bloqueoEscrituras) {
        this.bloqueoEscrituras = bloqueoEscrituras;
        this.hotelRepositorio = hotelRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.autorizacion = autorizacion;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Hotel crear(
            Usuario actor,
            String nombre,
            String direccion,
            String ciudad,
            String telefono,
            int capacidadTotal) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_HOTELES);
        bloqueoEscrituras.adquirir();
        Hotel hotel = new Hotel(nombre, direccion, ciudad, telefono, capacidadTotal);
        if (hotelRepositorio.existsByNombreIgnoreCaseAndCiudadIgnoreCase(
                hotel.getNombre(), hotel.getCiudad())) {
            throw new EntidadDuplicadaException("Ya existe ese hotel en la ciudad indicada");
        }
        return hotelRepositorio.save(hotel);
    }

    public List<Hotel> listar() {
        return hotelRepositorio.findAll();
    }

    public Hotel encontrarPorId(Integer codigo) {
        return hotelRepositorio.findById(codigo)
                .orElseThrow(() -> new EntidadNoEncontradaException("No se encontro el hotel " + codigo));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Hotel modificar(
            Usuario actor,
            Integer codigo,
            String nombre,
            String direccion,
            String ciudad,
            String telefono,
            int capacidadTotal) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_HOTELES);
        bloqueoEscrituras.adquirir();
        String nombreNormalizado = ValidacionModelo.textoObligatorio(nombre, "nombre");
        String ciudadNormalizada = ValidacionModelo.textoObligatorio(ciudad, "ciudad");
        if (hotelRepositorio.existsByNombreIgnoreCaseAndCiudadIgnoreCaseAndCodigoNot(
                nombreNormalizado, ciudadNormalizada, codigo)) {
            throw new EntidadDuplicadaException("Ya existe ese hotel en la ciudad indicada");
        }

        List<Reserva> reservas = reservaRepositorio.findByHotelCodigo(codigo);
        if (reservas.stream().anyMatch(reserva ->
                !reserva.getVuelo().getDestino().equalsIgnoreCase(ciudadNormalizada))) {
            throw new OperacionNoPermitidaException("La ciudad dejaría reservas incompatibles con su vuelo");
        }
        int ocupacionMaxima = OcupacionHotel.maxima(reservas, null, null, null);
        if (capacidadTotal < ocupacionMaxima) {
            throw new CapacidadExcedidaException(
                    "La capacidad no puede ser menor que la ocupacion maxima registrada: " + ocupacionMaxima);
        }

        Hotel hotel = encontrarPorId(codigo);
        hotel.actualizarDatos(nombreNormalizado, direccion, ciudadNormalizada, telefono, capacidadTotal);
        return hotel;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void eliminar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_HOTELES);
        bloqueoEscrituras.adquirir();
        Hotel hotel = encontrarPorId(codigo);
        if (reservaRepositorio.existsByHotelCodigo(codigo)) {
            throw new OperacionNoPermitidaException("No se puede eliminar un hotel que tiene reservas");
        }
        hotelRepositorio.delete(hotel);
    }

}
