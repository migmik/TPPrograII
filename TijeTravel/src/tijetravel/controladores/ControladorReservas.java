package tijetravel.controladores;

import java.time.LocalDate;
import java.util.ArrayList;

import tijetravel.modelos.Agencia;
import tijetravel.modelos.ClaseVuelo;
import tijetravel.modelos.Hotel;
import tijetravel.modelos.Reserva;
import tijetravel.modelos.Sucursal;
import tijetravel.modelos.TipoHospedaje;
import tijetravel.modelos.Turista;
import tijetravel.modelos.Vuelo;

public class ControladorReservas {
    private Agencia agencia;

    public ControladorReservas(Agencia agencia) {
        this.agencia = agencia;
    }

    public int generarCodigoReserva() {
        int mayorCodigo = 0;

        for (Reserva reserva : agencia.getReservas()) {
            if (reserva.getCodigo() > mayorCodigo) {
                mayorCodigo = reserva.getCodigo();
            }
        }

        return mayorCodigo + 1;
    }

    public boolean fechasValidas(LocalDate fechaLlegada, LocalDate fechaPartida) {
        if (fechaLlegada == null || fechaPartida == null) {
            return false;
        }

        return fechaPartida.isAfter(fechaLlegada);
    }

    public Reserva crearReserva(int codigoTurista, int codigoSucursal, int numeroVuelo, int codigoHotel,
            ClaseVuelo claseVuelo, TipoHospedaje tipoHospedaje, LocalDate fechaLlegada, LocalDate fechaPartida) {

        Turista turista = agencia.buscarTuristaPorCodigo(codigoTurista);
        Sucursal sucursal = agencia.buscarSucursalPorCodigo(codigoSucursal);
        Vuelo vuelo = agencia.buscarVueloPorNumero(numeroVuelo);
        Hotel hotel = agencia.buscarHotelPorCodigo(codigoHotel);

        if (turista == null || sucursal == null || vuelo == null || hotel == null) {
            return null;
        }

        if (claseVuelo == null || tipoHospedaje == null) {
            return null;
        }

        if (!fechasValidas(fechaLlegada, fechaPartida)) {
            return null;
        }

        if (!hotel.tienePlazasDisponibles()) {
            return null;
        }

        if (!vuelo.tienePlazasDisponibles(claseVuelo)) {
            return null;
        }

        boolean hotelReservado = hotel.reservarPlaza();
        boolean vueloReservado = vuelo.reservarPlaza(claseVuelo);

        if (!hotelReservado || !vueloReservado) {
            if (hotelReservado) {
                hotel.liberarPlaza();
            }

            if (vueloReservado) {
                vuelo.liberarPlaza(claseVuelo);
            }

            return null;
        }

        Reserva reserva = new Reserva(
                generarCodigoReserva(),
                turista,
                sucursal,
                vuelo,
                hotel,
                claseVuelo,
                tipoHospedaje,
                fechaLlegada,
                fechaPartida);

        if (!agencia.agregarReserva(reserva)) {
            hotel.liberarPlaza();
            vuelo.liberarPlaza(claseVuelo);
            return null;
        }

        return reserva;
    }

    public Reserva crearReservaCliente(int codigoTitular, int codigoTurista, int codigoSucursal, int numeroVuelo,
            int codigoHotel, ClaseVuelo claseVuelo, TipoHospedaje tipoHospedaje,
            LocalDate fechaLlegada, LocalDate fechaPartida) {

        if (!agencia.turistaPerteneceATitular(codigoTurista, codigoTitular)) {
            return null;
        }

        return crearReserva(
                codigoTurista,
                codigoSucursal,
                numeroVuelo,
                codigoHotel,
                claseVuelo,
                tipoHospedaje,
                fechaLlegada,
                fechaPartida);
    }

    public ArrayList<Reserva> buscarReservasDeTurista(int codigoTurista) {
        ArrayList<Reserva> reservasEncontradas = new ArrayList<>();

        for (Reserva reserva : agencia.getReservas()) {
            if (reserva.getTurista().getCodigo() == codigoTurista) {
                reservasEncontradas.add(reserva);
            }
        }

        return reservasEncontradas;
    }

    public ArrayList<Reserva> buscarReservasDeTitularYFamiliares(int codigoTitular) {
        ArrayList<Reserva> reservasEncontradas = new ArrayList<>();

        for (Reserva reserva : agencia.getReservas()) {
            int codigoTurista = reserva.getTurista().getCodigo();

            if (agencia.turistaPerteneceATitular(codigoTurista, codigoTitular)) {
                reservasEncontradas.add(reserva);
            }
        }

        return reservasEncontradas;
    }

    public boolean cancelarReserva(int codigoReserva) {
        Reserva reserva = agencia.buscarReservaPorCodigo(codigoReserva);

        if (reserva == null) {
            return false;
        }

        boolean eliminada = agencia.getReservas().remove(reserva);

        if (eliminada) {
            reserva.getHotel().liberarPlaza();
            reserva.getVuelo().liberarPlaza(reserva.getClaseVuelo());
        }

        return eliminada;
    }
}
