package tijetravel.controladores;

import java.time.LocalDate;
import java.util.ArrayList;

import tijetravel.modelos.Agencia;
import tijetravel.modelos.ClaseVuelo;
import tijetravel.modelos.Hotel;
import tijetravel.modelos.Permiso;
import tijetravel.modelos.Reserva;
import tijetravel.modelos.Sucursal;
import tijetravel.modelos.TipoHospedaje;
import tijetravel.modelos.Turista;
import tijetravel.modelos.Vuelo;
import tijetravel.modelos.Usuario;

public class ControladorReservas {
    private Agencia agencia;
    private ControladorAutorizacion autorizacion;

    public ControladorReservas(Agencia agencia) {
        this.agencia = agencia;
        this.autorizacion = new ControladorAutorizacion();
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

        return fechaLlegada.isBefore(fechaPartida);
    }

    public int contarReservasVuelo(int numeroVuelo, ClaseVuelo claseVuelo) {
        return contarReservasVuelo(numeroVuelo, claseVuelo, null);
    }

    private int contarReservasVuelo(int numeroVuelo, ClaseVuelo claseVuelo, Integer codigoReservaIgnorada) {
        int cantidad = 0;
        for (Reserva reserva : agencia.getReservas()) {
            if (codigoReservaIgnorada != null && reserva.getCodigo() == codigoReservaIgnorada) {
                continue;
            }
            if (reserva.getVuelo().getNumero() == numeroVuelo
                    && reserva.getClaseVuelo() == claseVuelo) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public int plazasDisponiblesVuelo(Vuelo vuelo, ClaseVuelo claseVuelo) {
        return plazasDisponiblesVuelo(vuelo, claseVuelo, null);
    }

    private int plazasDisponiblesVuelo(Vuelo vuelo, ClaseVuelo claseVuelo, Integer codigoReservaIgnorada) {
        if (vuelo == null || claseVuelo == null)
            return 0;
        int capacidad = claseVuelo == ClaseVuelo.TURISTA
                ? vuelo.getPlazasTurista()
                : vuelo.getPlazasPrimera();
        return Math.max(0, capacidad - contarReservasVuelo(vuelo.getNumero(), claseVuelo, codigoReservaIgnorada));
    }

    public int contarReservasHotelSuperpuestas(Hotel hotel, LocalDate llegada, LocalDate partida) {
        return contarReservasHotelSuperpuestas(hotel, llegada, partida, null);
    }

    private int contarReservasHotelSuperpuestas(Hotel hotel, LocalDate llegada, LocalDate partida,
            Integer codigoReservaIgnorada) {
        if (hotel == null || !fechasValidas(llegada, partida))
            return 0;
        int cantidad = 0;
        for (Reserva reserva : agencia.getReservas()) {
            if (codigoReservaIgnorada != null && reserva.getCodigo() == codigoReservaIgnorada) {
                continue;
            }
            if (reserva.getHotel().getCodigo() == hotel.getCodigo()
                    && llegada.isBefore(reserva.getFechaPartida())
                    && partida.isAfter(reserva.getFechaLlegada())) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public int plazasDisponiblesHotel(Hotel hotel, LocalDate llegada, LocalDate partida) {
        return plazasDisponiblesHotel(hotel, llegada, partida, null);
    }

    private int plazasDisponiblesHotel(Hotel hotel, LocalDate llegada, LocalDate partida,
            Integer codigoReservaIgnorada) {
        if (hotel == null)
            return 0;
        return Math.max(0, hotel.getPlazasDisponibles()
                - contarReservasHotelSuperpuestas(hotel, llegada, partida, codigoReservaIgnorada));
    }

    public int ocupacionMaximaHotel(int codigoHotel) {
        Hotel hotel = agencia.buscarHotelPorCodigo(codigoHotel);
        if (hotel == null)
            return 0;
        int maxima = 0;
        for (Reserva reserva : agencia.getReservas()) {
            if (reserva.getHotel().getCodigo() == codigoHotel) {
                maxima = Math.max(maxima, contarReservasHotelSuperpuestas(hotel,
                        reserva.getFechaLlegada(), reserva.getFechaPartida()));
            }
        }
        return maxima;
    }

    public boolean turistaYaTieneVuelo(int codigoTurista, int numeroVuelo) {
        return turistaYaTieneVuelo(codigoTurista, numeroVuelo, null);
    }

    private boolean turistaYaTieneVuelo(int codigoTurista, int numeroVuelo, Integer codigoReservaIgnorada) {
        for (Reserva reserva : agencia.getReservas()) {
            if (codigoReservaIgnorada != null && reserva.getCodigo() == codigoReservaIgnorada) {
                continue;
            }
            if (reserva.getTurista().getCodigo() == codigoTurista
                    && reserva.getVuelo().getNumero() == numeroVuelo)
                return true;
        }
        return false;
    }

    private boolean vueloYHotelCompatibles(Vuelo vuelo, Hotel hotel, LocalDate fechaLlegada) {
        return fechaLlegada.equals(vuelo.getFechaYHora().toLocalDate())
                && hotel.getCiudad().trim().equalsIgnoreCase(vuelo.getDestino().trim());
    }

    public Reserva crearReserva(Usuario usuario, int codigoTurista, int codigoSucursal, int numeroVuelo,
            int codigoHotel,
            ClaseVuelo claseVuelo, TipoHospedaje tipoHospedaje, LocalDate fechaLlegada, LocalDate fechaPartida) {
        if (!autorizacion.tienePermiso(usuario, Permiso.ADMINISTRAR_RESERVAS))
            return null;

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

        if (turistaYaTieneVuelo(codigoTurista, numeroVuelo)) {
            return null;
        }
        if (!vueloYHotelCompatibles(vuelo, hotel, fechaLlegada)) {
            return null;
        }

        if (plazasDisponiblesVuelo(vuelo, claseVuelo) <= 0
                || plazasDisponiblesHotel(hotel, fechaLlegada, fechaPartida) <= 0)
            return null;

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

        if (!agencia.agregarReserva(reserva))
            return null;

        return reserva;
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

    public boolean cancelarReserva(Usuario usuario, int codigoReserva) {
        if (!autorizacion.tienePermiso(usuario, Permiso.ADMINISTRAR_RESERVAS))
            return false;
        Reserva reserva = agencia.buscarReservaPorCodigo(codigoReserva);

        if (reserva == null) {
            return false;
        }

        return agencia.eliminarReserva(codigoReserva);
    }

    public boolean modificarReserva(Usuario usuario, int codigoReserva, int codigoTurista, int codigoSucursal,
            int numeroVuelo, int codigoHotel, ClaseVuelo claseVuelo, TipoHospedaje tipoHospedaje,
            LocalDate fechaLlegada, LocalDate fechaPartida) {
        if (!autorizacion.tienePermiso(usuario, Permiso.ADMINISTRAR_RESERVAS))
            return false;

        Reserva reserva = agencia.buscarReservaPorCodigo(codigoReserva);
        Turista turista = agencia.buscarTuristaPorCodigo(codigoTurista);
        Sucursal sucursal = agencia.buscarSucursalPorCodigo(codigoSucursal);
        Vuelo vuelo = agencia.buscarVueloPorNumero(numeroVuelo);
        Hotel hotel = agencia.buscarHotelPorCodigo(codigoHotel);

        if (reserva == null || turista == null || sucursal == null || vuelo == null || hotel == null
                || claseVuelo == null || tipoHospedaje == null || !fechasValidas(fechaLlegada, fechaPartida)) {
            return false;
        }

        if (turistaYaTieneVuelo(codigoTurista, numeroVuelo, codigoReserva)) {
            return false;
        }

        if (!vueloYHotelCompatibles(vuelo, hotel, fechaLlegada)) {
            return false;
        }

        if (plazasDisponiblesVuelo(vuelo, claseVuelo, codigoReserva) <= 0
                || plazasDisponiblesHotel(hotel, fechaLlegada, fechaPartida, codigoReserva) <= 0) {
            return false;
        }

        return reserva.actualizarDatos(turista, sucursal, vuelo, hotel, claseVuelo, tipoHospedaje,
                fechaLlegada, fechaPartida);
    }
}
