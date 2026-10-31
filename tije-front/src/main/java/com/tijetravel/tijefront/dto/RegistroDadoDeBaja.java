package com.tijetravel.tijefront.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Cualquier registro dado de baja (sucursal, hotel, vuelo, turista, reserva,
 * usuario o rol). Tiene los campos de todos; cada modulo usa los suyos para
 * armar la descripcion y los demas quedan vacios.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistroDadoDeBaja {
    private Integer codigo;
    private Integer numero;
    private String nombre;
    private String apellido;
    private String dni;
    private String direccion;
    private String ciudad;
    private String origen;
    private String destino;
    private LocalDateTime fechaYHora;
    private String nombreUsuario;
    private String rol;
    private Integer codigoTurista;
    private Integer numeroVuelo;
    private Integer codigoHotel;
    private LocalDate fechaLlegada;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaBaja;

    /** Los vuelos se identifican por numero; el resto, por codigo. */
    public Integer getIdentificador() {
        return codigo != null ? codigo : numero;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
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

    public LocalDateTime getFechaYHora() {
        return fechaYHora;
    }

    public void setFechaYHora(LocalDateTime fechaYHora) {
        this.fechaYHora = fechaYHora;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Integer getCodigoTurista() {
        return codigoTurista;
    }

    public void setCodigoTurista(Integer codigoTurista) {
        this.codigoTurista = codigoTurista;
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

    public LocalDate getFechaLlegada() {
        return fechaLlegada;
    }

    public void setFechaLlegada(LocalDate fechaLlegada) {
        this.fechaLlegada = fechaLlegada;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(LocalDateTime fechaBaja) {
        this.fechaBaja = fechaBaja;
    }
}
