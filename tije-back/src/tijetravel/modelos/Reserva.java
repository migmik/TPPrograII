package tijetravel.modelos;

import java.time.LocalDate;

public class Reserva {
    private int codigo;
    private Turista turista;
    private Sucursal sucursal;
    private Vuelo vuelo;
    private Hotel hotel;
    private ClaseVuelo claseVuelo;
    private TipoHospedaje tipoHospedaje;
    private LocalDate fechaLlegada;
    private LocalDate fechaPartida;

    public Reserva(int codigo, Turista turista, Sucursal sucursal, Vuelo vuelo, Hotel hotel, ClaseVuelo claseVuelo,
            TipoHospedaje tipoHospedaje, LocalDate fechaLlegada, LocalDate fechaPartida) {
        this.codigo = codigo;
        this.turista = turista;
        this.sucursal = sucursal;
        this.vuelo = vuelo;
        this.hotel = hotel;
        this.claseVuelo = claseVuelo;
        this.tipoHospedaje = tipoHospedaje;
        this.fechaLlegada = fechaLlegada;
        this.fechaPartida = fechaPartida;
    }

    public boolean actualizarDatos(Turista turista, Sucursal sucursal, Vuelo vuelo, Hotel hotel,
            ClaseVuelo claseVuelo, TipoHospedaje tipoHospedaje, LocalDate fechaLlegada, LocalDate fechaPartida) {
        if (turista == null || sucursal == null || vuelo == null || hotel == null
                || claseVuelo == null || tipoHospedaje == null || fechaLlegada == null || fechaPartida == null) {
            return false;
        }

        this.turista = turista;
        this.sucursal = sucursal;
        this.vuelo = vuelo;
        this.hotel = hotel;
        this.claseVuelo = claseVuelo;
        this.tipoHospedaje = tipoHospedaje;
        this.fechaLlegada = fechaLlegada;
        this.fechaPartida = fechaPartida;
        return true;
    }

    public int getCodigo() {
        return codigo;
    }

    private void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Turista getTurista() {
        return turista;
    }

    private void setTurista(Turista turista) {
        this.turista = turista;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    private void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Vuelo getVuelo() {
        return vuelo;
    }

    private void setVuelo(Vuelo vuelo) {
        this.vuelo = vuelo;
    }

    public Hotel getHotel() {
        return hotel;
    }

    private void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public ClaseVuelo getClaseVuelo() {
        return claseVuelo;
    }

    private void setClaseVuelo(ClaseVuelo claseVuelo) {
        this.claseVuelo = claseVuelo;
    }

    public TipoHospedaje getTipoHospedaje() {
        return tipoHospedaje;
    }

    private void setTipoHospedaje(TipoHospedaje tipoHospedaje) {
        this.tipoHospedaje = tipoHospedaje;
    }

    public LocalDate getFechaLlegada() {
        return fechaLlegada;
    }

    private void setFechaLlegada(LocalDate fechaLlegada) {
        this.fechaLlegada = fechaLlegada;
    }

    public LocalDate getFechaPartida() {
        return fechaPartida;
    }

    private void setFechaPartida(LocalDate fechaPartida) {
        this.fechaPartida = fechaPartida;
    }

}
