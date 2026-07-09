package tijetravel.models;

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

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public Turista getTurista() {
        return turista;
    }

    public void setTurista(Turista turista) {
        this.turista = turista;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public Vuelo getVuelo() {
        return vuelo;
    }

    public void setVuelo(Vuelo vuelo) {
        this.vuelo = vuelo;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public ClaseVuelo getClaseVuelo() {
        return claseVuelo;
    }

    public void setClaseVuelo(ClaseVuelo claseVuelo) {
        this.claseVuelo = claseVuelo;
    }

    public TipoHospedaje getTipoHospedaje() {
        return tipoHospedaje;
    }

    public void setTipoHospedaje(TipoHospedaje tipoHospedaje) {
        this.tipoHospedaje = tipoHospedaje;
    }

    public LocalDate getFechaLlegada() {
        return fechaLlegada;
    }

    public void setFechaLlegada(LocalDate fechaLlegada) {
        this.fechaLlegada = fechaLlegada;
    }

    public LocalDate getFechaPartida() {
        return fechaPartida;
    }

    public void setFechaPartida(LocalDate fechaPartida) {
        this.fechaPartida = fechaPartida;
    }
    
}
