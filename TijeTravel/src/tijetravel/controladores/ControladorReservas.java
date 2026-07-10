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

// controlador de reservas de la agencia
//valida permisos, disponibilidad, crea y cancela reservas
public class ControladorReservas {
    private Agencia agencia;
    private ControladorAutorizacion autorizacion;

    public ControladorReservas(Agencia agencia) {
        this.agencia = agencia;
        this.autorizacion = new ControladorAutorizacion();
    }

    // genera un codigo unico para reservas
    public int generarCodigoReserva() {
        int mayorCodigo = 0;

        for (Reserva reserva : agencia.getReservas()) {
            if (reserva.getCodigo() > mayorCodigo) {
                mayorCodigo = reserva.getCodigo();
            }
        }

        return mayorCodigo + 1;
    }

    // valida fechas de partida y llegada
    public boolean fechasValidas(LocalDate fechaLlegada, LocalDate fechaPartida) {
        if (fechaLlegada == null || fechaPartida == null) {
            return false;
        }

        return fechaLlegada.isBefore(fechaPartida);
    }

    // cuenta reservas de un vuelo en una clase especifica
    public int contarReservasVuelo(int numeroVuelo, ClaseVuelo claseVuelo) {
        int cantidad = 0;
        for (Reserva reserva : agencia.getReservas()) {
            if (reserva.getVuelo().getNumero() == numeroVuelo
                    && reserva.getClaseVuelo() == claseVuelo) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public int plazasDisponiblesVuelo(Vuelo vuelo, ClaseVuelo claseVuelo) {
        if (vuelo == null || claseVuelo == null)
            return 0;
        int capacidad = claseVuelo == ClaseVuelo.TURISTA
                ? vuelo.getPlazasTurista()
                : vuelo.getPlazasPrimera();
        return Math.max(0, capacidad - contarReservasVuelo(vuelo.getNumero(), claseVuelo));
    }

    // Cuenta reservas del hotel que se superponen con el rango de fechas
    // solicitado.
    public int contarReservasHotelSuperpuestas(Hotel hotel, LocalDate llegada, LocalDate partida) {
        if (hotel == null || !fechasValidas(llegada, partida))
            return 0;
        int cantidad = 0;
        for (Reserva reserva : agencia.getReservas()) {
            if (reserva.getHotel().getCodigo() == hotel.getCodigo()
                    && llegada.isBefore(reserva.getFechaPartida())
                    && partida.isAfter(reserva.getFechaLlegada())) {
                cantidad++;
            }
        }
        return cantidad;
    }

    public int plazasDisponiblesHotel(Hotel hotel, LocalDate llegada, LocalDate partida) {
        if (hotel == null)
            return 0;
        return Math.max(0, hotel.getPlazasDisponibles()
                - contarReservasHotelSuperpuestas(hotel, llegada, partida));
    }

    // determina cuantas plazas minimas debe conservar el hotel
    // segun las reservas ya existentes en cualquier periodo
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

    // esto verifica si un turista ya reservo el mismo vuelo
    public boolean turistaYaTieneVuelo(int codigoTurista, int numeroVuelo) {
        for (Reserva reserva : agencia.getReservas()) {
            if (reserva.getTurista().getCodigo() == codigoTurista
                    && reserva.getVuelo().getNumero() == numeroVuelo)
                return true;
        }
        return false;
    }

    // Comprueba que el hotel y el vuelo sean compatibles en fecha y destino.
    private boolean vueloYHotelCompatibles(Vuelo vuelo, Hotel hotel, LocalDate fechaLlegada) {
        return !fechaLlegada.isBefore(vuelo.getFechaYHora().toLocalDate())
                && hotel.getCiudad().trim().equalsIgnoreCase(vuelo.getDestino().trim());
    }

    // crea una nueva reserva si el usuario tiene permiso y si todos los datos son
    // validos:
    /*
     * 1 permiso
     * 2 turista, sucursa, vuelo, hotel validos
     * 3 clase y hospedaje
     * 4 fechas
     * 5 que no sea mismo vuelo
     * 6 vuelo y hotel compatibles
     * 7 plazas disponible en vuelo y hotel
     */
    public Reserva crearReserva(Usuario usuario, int codigoTurista, int codigoSucursal, int numeroVuelo,
            int codigoHotel,
            ClaseVuelo claseVuelo, TipoHospedaje tipoHospedaje, LocalDate fechaLlegada, LocalDate fechaPartida) {
        // permiso
        if (!autorizacion.tienePermiso(usuario, Permiso.ADMINISTRAR_RESERVAS))
            return null;

        Turista turista = agencia.buscarTuristaPorCodigo(codigoTurista);
        Sucursal sucursal = agencia.buscarSucursalPorCodigo(codigoSucursal);
        Vuelo vuelo = agencia.buscarVueloPorNumero(numeroVuelo);
        Hotel hotel = agencia.buscarHotelPorCodigo(codigoHotel);
        // rechaza reserva si falta alguna
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
        // asegura que el vuelo y hotel coincidan
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

    // devuelve todas las reservas de un turista
    public ArrayList<Reserva> buscarReservasDeTurista(int codigoTurista) {
        ArrayList<Reserva> reservasEncontradas = new ArrayList<>();

        for (Reserva reserva : agencia.getReservas()) {
            if (reserva.getTurista().getCodigo() == codigoTurista) {
                reservasEncontradas.add(reserva);
            }
        }

        return reservasEncontradas;
    }

    // busca las reservas del titular y familiares
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

    // cancela una reserva si el usuario tiene permisos
    public boolean cancelarReserva(Usuario usuario, int codigoReserva) {
        if (!autorizacion.tienePermiso(usuario, Permiso.ADMINISTRAR_RESERVAS))
            return false;
        Reserva reserva = agencia.buscarReservaPorCodigo(codigoReserva);

        if (reserva == null) {
            return false;
        }

        return agencia.eliminarReserva(codigoReserva);
    }
}
