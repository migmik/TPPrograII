package com.tijetravel.tijeback.modelos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@NamedEntityGraph(name = "Reserva.completa", attributeNodes = {
        @NamedAttributeNode(value = "turista", subgraph = "turistaConTitular"),
        @NamedAttributeNode("sucursalContratacion"),
        @NamedAttributeNode("vuelo"),
        @NamedAttributeNode("hotel")
}, subgraphs = @NamedSubgraph(name = "turistaConTitular", attributeNodes = @NamedAttributeNode("titular")))
@Table(name = "reservas", uniqueConstraints = @UniqueConstraint(name = "uk_reserva_turista_vuelo", columnNames = {
        "turista_codigo", "vuelo_numero" }))
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turista_codigo", nullable = false)
    private Turista turista;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sucursal_codigo", nullable = false, updatable = false)
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

    @Column(name = "precio_vuelo", nullable = false)
    private int precioVuelo;

    @Column(name = "precio_hotel", nullable = false)
    private int precioHotel;

    @Column(nullable = false)
    private int total;

    @Column(nullable = false)
    private boolean pagada;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

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
        if (pagada) {
            throw new IllegalStateException("Una reserva pagada no se puede modificar");
        }
        validarFechas(fechaLlegada, fechaPartida);
        ValidacionModelo.obligatorio(turista, "turista");
        ValidacionModelo.obligatorio(vuelo, "vuelo");
        ValidacionModelo.obligatorio(hotel, "hotel");
        ValidacionModelo.obligatorio(claseVuelo, "claseVuelo");
        ValidacionModelo.obligatorio(tipoHospedaje, "tipoHospedaje");
        if (this.sucursalContratacion == null) {
            ValidacionModelo.obligatorio(turista.getSucursalContratacion(), "sucursalContratacion");
        }

        // El precio se calcula antes de asignar para no dejar la reserva a medio modificar.
        long nuevoPrecioVuelo = aplicarRecargo(vuelo.getPrecio(), claseVuelo.getPorcentajeRecargo());
        long noches = ChronoUnit.DAYS.between(fechaLlegada, fechaPartida);
        long nuevoPrecioHotel = aplicarRecargo(noches * hotel.getPrecioNochePersona(),
                tipoHospedaje.getPorcentajeRecargo());
        if (nuevoPrecioHotel > Integer.MAX_VALUE - nuevoPrecioVuelo) {
            throw new IllegalArgumentException("La estadía excede el importe máximo permitido");
        }

        if (this.sucursalContratacion == null) {
            this.sucursalContratacion = turista.getSucursalContratacion();
        }
        this.turista = turista;
        this.vuelo = vuelo;
        this.hotel = hotel;
        this.claseVuelo = claseVuelo;
        this.tipoHospedaje = tipoHospedaje;
        this.fechaLlegada = fechaLlegada;
        this.fechaPartida = fechaPartida;
        this.precioVuelo = (int) nuevoPrecioVuelo;
        this.precioHotel = (int) nuevoPrecioHotel;
        this.total = precioVuelo + precioHotel;
    }

    private static long aplicarRecargo(long importe, int porcentaje) {
        return importe * (100 + porcentaje) / 100;
    }

    public void confirmarPago() {
        if (!pagada) {
            pagada = true;
            fechaPago = LocalDateTime.now();
        }
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

    public int getPrecioVuelo() { return precioVuelo; }
    public int getPrecioHotel() { return precioHotel; }
    public int getTotal() { return total; }
    public boolean isPagada() { return pagada; }
    public LocalDateTime getFechaPago() { return fechaPago; }

    @Override
    public String toString() {
        return "Reserva | codigo=" + codigo + ", turista=" + turista.getCodigo()
                + ", vuelo=" + vuelo.getNumero() + ", hotel=" + hotel.getCodigo();
    }
}
