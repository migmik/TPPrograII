package com.tijetravel.tijeback.modelos;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vuelos")
public class Vuelo {
    public static final int PRECIO_MINIMO = 150000;
    public static final int PRECIO_MAXIMO = 300000;
    @Id
    private Integer numero;

    @Column(name = "fecha_hora", nullable = false)
    @JdbcTypeCode(SqlTypes.LOCAL_DATE_TIME)
    private LocalDateTime fechaYHora;

    @Column(nullable = false)
    private String origen;

    @Column(nullable = false)
    private String destino;

    @Column(name = "total_plazas", nullable = false)
    private int totalPlazas;

    @Column(name = "plazas_turista", nullable = false)
    private int plazasTurista;

    @Column(name = "plazas_primera", nullable = false)
    private int plazasPrimera;

    @Column(nullable = false)
    private int precio;

    protected Vuelo() {
    }

    public Vuelo(
            int numero,
            LocalDateTime fechaYHora,
            String origen,
            String destino,
            int totalPlazas,
            int plazasTurista,
            int plazasPrimera) {
        this(numero, fechaYHora, origen, destino, totalPlazas, plazasTurista, plazasPrimera, PRECIO_MINIMO);
    }

    public Vuelo(int numero, LocalDateTime fechaYHora, String origen, String destino,
            int totalPlazas, int plazasTurista, int plazasPrimera, int precio) {
        this.numero = ValidacionModelo.enteroPositivo(numero, "numero");
        actualizarDatos(fechaYHora, origen, destino, totalPlazas, plazasTurista, plazasPrimera, precio);
    }

    public void actualizarDatos(LocalDateTime fechaYHora, String origen, String destino,
            int totalPlazas, int plazasTurista, int plazasPrimera, int precio) {
        ValidacionModelo.obligatorio(fechaYHora, "fechaYHora");
        ValidacionModelo.enteroPositivo(totalPlazas, "totalPlazas");
        ValidacionModelo.enteroNoNegativo(plazasTurista, "plazasTurista");
        ValidacionModelo.enteroNoNegativo(plazasPrimera, "plazasPrimera");
        if (plazasTurista + plazasPrimera > totalPlazas) {
            throw new IllegalArgumentException("Las plazas por clase no pueden superar el total del vuelo");
        }
        String origenValidado = ValidacionModelo.textoObligatorio(origen, "origen");
        String destinoValidado = ValidacionModelo.textoObligatorio(destino, "destino");
        if (precio < PRECIO_MINIMO || precio > PRECIO_MAXIMO || precio % 10000 != 0) {
            throw new IllegalArgumentException("El precio del vuelo debe estar entre " + PRECIO_MINIMO + " y "
                    + PRECIO_MAXIMO + " y ser multiplo de 10000");
        }

        this.fechaYHora = fechaYHora;
        this.origen = origenValidado;
        this.destino = destinoValidado;
        this.totalPlazas = totalPlazas;
        this.plazasTurista = plazasTurista;
        this.plazasPrimera = plazasPrimera;
        this.precio = precio;
    }

    public Integer getNumero() {
        return numero;
    }

    public LocalDateTime getFechaYHora() {
        return fechaYHora;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public int getTotalPlazas() {
        return totalPlazas;
    }

    public int getPlazasTurista() {
        return plazasTurista;
    }

    public int getPlazasPrimera() {
        return plazasPrimera;
    }

    public int getPrecio() {
        return precio;
    }

    @Override
    public String toString() {
        return "Vuelo | numero=" + numero + ", origen=" + origen + ", destino=" + destino
                + ", fecha=" + fechaYHora;
    }
}
