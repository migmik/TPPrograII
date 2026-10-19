package com.tijetravel.tijefront.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReservaRespuesta {
    private Integer codigo;
    private Integer codigoTurista;
    private Integer codigoSucursalContratacion;
    private Integer numeroVuelo;
    private Integer codigoHotel;
    private String claseVuelo;
    private String tipoHospedaje;
    private LocalDate fechaLlegada;
    private LocalDate fechaPartida;
    private int precioVuelo;
    private int precioHotel;
    private int total;
    private boolean pagada;
    private java.time.LocalDateTime fechaPago;

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigoTurista() {
        return codigoTurista;
    }

    public void setCodigoTurista(Integer codigoTurista) {
        this.codigoTurista = codigoTurista;
    }

    public Integer getCodigoSucursalContratacion() {
        return codigoSucursalContratacion;
    }

    public void setCodigoSucursalContratacion(Integer codigoSucursalContratacion) {
        this.codigoSucursalContratacion = codigoSucursalContratacion;
    }

    public Integer getNumeroVuelo() {
        return numeroVuelo;
    }

    public void setNumeroVuelo(Integer numeroVuelo) {
        this.numeroVuelo = numeroVuelo;
    }

    public Integer getCodigoHotel() {
        return codigoHotel;
    }

    public void setCodigoHotel(Integer codigoHotel) {
        this.codigoHotel = codigoHotel;
    }

    public String getClaseVuelo() {
        return claseVuelo;
    }

    public void setClaseVuelo(String claseVuelo) {
        this.claseVuelo = claseVuelo;
    }

    public String getTipoHospedaje() {
        return tipoHospedaje;
    }

    public void setTipoHospedaje(String tipoHospedaje) {
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

    public int getPrecioVuelo() { return precioVuelo; }
    public void setPrecioVuelo(int precioVuelo) { this.precioVuelo = precioVuelo; }
    public int getPrecioHotel() { return precioHotel; }
    public void setPrecioHotel(int precioHotel) { this.precioHotel = precioHotel; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public boolean isPagada() { return pagada; }
    public void setPagada(boolean pagada) { this.pagada = pagada; }
    public java.time.LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(java.time.LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public void setFechaPartida(LocalDate fechaPartida) {
        this.fechaPartida = fechaPartida;
    }

    public String getFechaLlegadaFormateada() {
        return fechaLlegada == null ? "" : fechaLlegada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getFechaPartidaFormateada() {
        return fechaPartida == null ? "" : fechaPartida.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
