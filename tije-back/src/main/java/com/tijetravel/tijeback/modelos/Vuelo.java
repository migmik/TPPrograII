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
        this(numero, fechaYHora, origen, destino, totalPlazas, plazasTurista, plazasPrimera, 150000);
    }

    public Vuelo(int numero, LocalDateTime fechaYHora, String origen, String destino,
            int totalPlazas, int plazasTurista, int plazasPrimera, int precio) {
        this.numero = ValidacionModelo.enteroPositivo(numero, "numero");
        actualizarDatos(fechaYHora, origen, destino, totalPlazas, plazasTurista, plazasPrimera, precio);
    }

    public void actualizarDatos(
            LocalDateTime fechaYHora,
            String origen,
            String destino,
            int totalPlazas,
            int plazasTurista,
            int plazasPrimera) {
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

        this.fechaYHora = fechaYHora;
        this.origen = ValidacionModelo.textoObligatorio(origen, "origen");
        this.destino = ValidacionModelo.textoObligatorio(destino, "destino");
        this.totalPlazas = totalPlazas;
        this.plazasTurista = plazasTurista;
        this.plazasPrimera = plazasPrimera;
        if (precio < 150000 || precio > 300000 || precio % 10000 != 0) {
            throw new IllegalArgumentException("El precio del vuelo debe estar entre 150000 y 300000 y ser multiplo de 10000");
        }
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
