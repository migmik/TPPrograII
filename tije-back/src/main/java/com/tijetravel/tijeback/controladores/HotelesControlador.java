package com.tijetravel.tijeback.controladores;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.excepciones.CapacidadExcedidaException;
import com.tijetravel.tijeback.excepciones.EntidadDuplicadaException;
import com.tijetravel.tijeback.excepciones.EntidadNoEncontradaException;
import com.tijetravel.tijeback.excepciones.OperacionNoPermitidaException;
import com.tijetravel.tijeback.modelos.Hotel;
import com.tijetravel.tijeback.modelos.Reserva;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.repositorios.HotelRepositorio;
import com.tijetravel.tijeback.repositorios.ReservaRepositorio;

@Service
@Transactional(readOnly = true)
public class HotelesControlador {
    private final HotelRepositorio hotelRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final AutorizacionControlador autorizacion;

    public HotelesControlador(
            HotelRepositorio hotelRepositorio,
            ReservaRepositorio reservaRepositorio,
            AutorizacionControlador autorizacion) {
        this.hotelRepositorio = hotelRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.autorizacion = autorizacion;
    }

    @Transactional
    public Hotel ingresar(
            Usuario actor,
            String nombre,
            String direccion,
            String ciudad,
            String telefono,
            int plazasDisponibles) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_HOTELES);
        Hotel hotel = new Hotel(nombre, direccion, ciudad, telefono, plazasDisponibles);
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

    @Transactional
    public Hotel modificar(
            Usuario actor,
            Integer codigo,
            String nombre,
            String direccion,
            String ciudad,
            String telefono,
            int plazasDisponibles) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_HOTELES);
        Hotel propuesta = new Hotel(nombre, direccion, ciudad, telefono, plazasDisponibles);
        if (hotelRepositorio.existsByNombreIgnoreCaseAndCiudadIgnoreCaseAndCodigoNot(
                propuesta.getNombre(), propuesta.getCiudad(), codigo)) {
            throw new EntidadDuplicadaException("Ya existe ese hotel en la ciudad indicada");
        }

        int ocupacionMaxima = calcularOcupacionMaxima(reservaRepositorio.findByHotelCodigo(codigo));
        if (plazasDisponibles < ocupacionMaxima) {
            throw new CapacidadExcedidaException(
                    "La capacidad no puede ser menor que la ocupacion maxima registrada: " + ocupacionMaxima);
        }

        Hotel hotel = encontrarPorId(codigo);
        hotel.actualizarDatos(nombre, direccion, ciudad, telefono, plazasDisponibles);
        return hotelRepositorio.save(hotel);
    }

    @Transactional
    public void eliminar(Usuario actor, Integer codigo) {
        autorizacion.verificarPermiso(actor, Permiso.ADMINISTRAR_HOTELES);
        Hotel hotel = encontrarPorId(codigo);
        if (reservaRepositorio.existsByHotelCodigo(codigo)) {
            throw new OperacionNoPermitidaException("No se puede eliminar un hotel que tiene reservas");
        }
        hotelRepositorio.delete(hotel);
    }

    private int calcularOcupacionMaxima(List<Reserva> reservas) {
        Map<java.time.LocalDate, Integer> variaciones = new TreeMap<>();
        for (Reserva reserva : reservas) {
            variaciones.merge(reserva.getFechaLlegada(), 1, Integer::sum);
            variaciones.merge(reserva.getFechaPartida(), -1, Integer::sum);
        }

        int ocupacion = 0;
        int maxima = 0;
        for (int variacion : variaciones.values()) {
            ocupacion += variacion;
            maxima = Math.max(maxima, ocupacion);
        }
        return maxima;
    }
}
