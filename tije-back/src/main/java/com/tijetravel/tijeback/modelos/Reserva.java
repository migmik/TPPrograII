package com.tijetravel.tijeback.modelos;

import java.time.LocalDate;

import com.tijetravel.tijeback.enums.ClaseVuelo;
import com.tijetravel.tijeback.enums.TipoHospedaje;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "reservas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reserva_turista_vuelo",
                columnNames = {"turista_codigo", "vuelo_numero"}))
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turista_codigo", nullable = false)
    private Turista turista;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sucursal_codigo", nullable = false)
    private Sucursal sucursalContratacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vuelo_numero", nullable = false)
    private Vuelo vuelo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_codigo", nullable = false)
    private Hotel hotel;

    @Enumerated(EnumType.STRING)
    @Column(name = "clase_vuelo", nullable = false)
    private ClaseVuelo claseVuelo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_hospedaje", nullable = false)
    private TipoHospedaje tipoHospedaje;

    @Column(name = "fecha_llegada", nullable = false)
    private LocalDate fechaLlegada;

    @Column(name = "fecha_partida", nullable = false)
    private LocalDate fechaPartida;

    protected Reserva() {
    }

    public Reserva(
            Turista turista,
            Vuelo vuelo,
            Hotel hotel,
            ClaseVuelo claseVuelo,
            TipoHospedaje tipoHospedaje,
            LocalDate fechaLlegada,
            LocalDate fechaPartida) {
        actualizarDatos(
                turista,
                vuelo,
                hotel,
                claseVuelo,
                tipoHospedaje,
                fechaLlegada,
                fechaPartida);
    }

    public void actualizarDatos(
            Turista turista,
            Vuelo vuelo,
            Hotel hotel,
            ClaseVuelo claseVuelo,
            TipoHospedaje tipoHospedaje,
            LocalDate fechaLlegada,
            LocalDate fechaPartida) {
        validarFechas(fechaLlegada, fechaPartida);
        this.turista = ValidacionModelo.obligatorio(turista, "turista");
        this.sucursalContratacion = ValidacionModelo.obligatorio(
                turista.getSucursalContratacion(), "sucursalContratacion");
        this.vuelo = ValidacionModelo.obligatorio(vuelo, "vuelo");
        this.hotel = ValidacionModelo.obligatorio(hotel, "hotel");
        this.claseVuelo = ValidacionModelo.obligatorio(claseVuelo, "claseVuelo");
        this.tipoHospedaje = ValidacionModelo.obligatorio(tipoHospedaje, "tipoHospedaje");
        this.fechaLlegada = fechaLlegada;
        this.fechaPartida = fechaPartida;
    }

    private void validarFechas(LocalDate fechaLlegada, LocalDate fechaPartida) {
        ValidacionModelo.obligatorio(fechaLlegada, "fechaLlegada");
        ValidacionModelo.obligatorio(fechaPartida, "fechaPartida");
        if (!fechaLlegada.isBefore(fechaPartida)) {
            throw new IllegalArgumentException("La fecha de llegada debe ser anterior a la fecha de partida");
        }
    }

    public Integer getCodigo() {
        return codigo;
    }

    public Turista getTurista() {
        return turista;
    }

    public Sucursal getSucursalContratacion() {
        return sucursalContratacion;
    }

    public Vuelo getVuelo() {
        return vuelo;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public ClaseVuelo getClaseVuelo() {
        return claseVuelo;
    }

    public TipoHospedaje getTipoHospedaje() {
        return tipoHospedaje;
    }

    public LocalDate getFechaLlegada() {
        return fechaLlegada;
    }

    public LocalDate getFechaPartida() {
        return fechaPartida;
    }

    @Override
    public String toString() {
        return "Reserva | codigo=" + codigo + ", turista=" + turista.getCodigo()
                + ", vuelo=" + vuelo.getNumero() + ", hotel=" + hotel.getCodigo();
    }
}
