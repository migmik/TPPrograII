package com.tijetravel.tijefront.formularios;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

public class GuardarReservaFormulario {
    private boolean seleccionBuscador;

    @JsonIgnore
    public boolean isSeleccionBuscador() {
        return seleccionBuscador;
    }

    public void setSeleccionBuscador(boolean seleccionBuscador) {
        this.seleccionBuscador = seleccionBuscador;
    }

    @NotNull(message = "Seleccioná una opción.")
    @Positive(message = "El código debe ser positivo.")
    private Integer codigoTurista;

    @NotNull(message = "Seleccioná una opción.")
    @Positive(message = "El código debe ser positivo.")
    private Integer numeroVuelo;

    @NotNull(message = "Seleccioná una opción.")
    @Positive(message = "El código debe ser positivo.")
    private Integer codigoHotel;

    @NotBlank(message = "Seleccioná una opción.")
    @Pattern(regexp = "TURISTA|PRIMERA", message = "Seleccioná una opción válida.")
    private String claseVuelo;

    @NotBlank(message = "Seleccioná una opción.")
    @Pattern(regexp = "MEDIA_PENSION|PENSION_COMPLETA", message = "Seleccioná una opción válida.")
    private String tipoHospedaje;

    @NotNull(message = "Ingresá la fecha.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaLlegada;

    @NotNull(message = "Ingresá la fecha.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaPartida;

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

    public void setFechaPartida(LocalDate fechaPartida) {
        this.fechaPartida = fechaPartida;
    }
}
