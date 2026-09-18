package com.tijetravel.tijefront.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VueloRespuesta {
    private Integer numero;
    private LocalDateTime fechaYHora;
    private String origen;
    private String destino;
    private int totalPlazas;
    private int plazasTurista;
    private int plazasPrimera;

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public LocalDateTime getFechaYHora() {
        return fechaYHora;
    }

    public void setFechaYHora(LocalDateTime fechaYHora) {
        this.fechaYHora = fechaYHora;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public int getTotalPlazas() {
        return totalPlazas;
    }

    public void setTotalPlazas(int totalPlazas) {
        this.totalPlazas = totalPlazas;
    }

    public int getPlazasTurista() {
        return plazasTurista;
    }

    public void setPlazasTurista(int plazasTurista) {
        this.plazasTurista = plazasTurista;
    }

    public int getPlazasPrimera() {
        return plazasPrimera;
    }

    public void setPlazasPrimera(int plazasPrimera) {
        this.plazasPrimera = plazasPrimera;
    }

    public String getFechaYHoraFormateada() {
        return fechaYHora == null ? "" : fechaYHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
