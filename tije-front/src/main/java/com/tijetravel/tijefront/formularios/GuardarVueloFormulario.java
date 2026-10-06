package com.tijetravel.tijefront.formularios;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class GuardarVueloFormulario {
    @NotNull(message = "Ingresá el número.")
    @Positive(message = "El número debe ser positivo.")
    private Integer numero;
    @NotNull(message = "Ingresá la fecha y hora.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaYHora;
    @NotBlank(message = "Ingresá el origen.")
    @Size(max = 255)
    private String origen;
    @NotBlank(message = "Ingresá el destino.")
    @Size(max = 255)
    private String destino;
    @NotNull(message = "Ingresá el total de plazas.")
    @Positive(message = "El total debe ser positivo.")
    private Integer totalPlazas;
    @NotNull(message = "Ingresá las plazas turista.")
    @PositiveOrZero(message = "Las plazas no pueden ser negativas.")
    private Integer plazasTurista;
    @NotNull(message = "Ingresá las plazas de primera.")
    @PositiveOrZero(message = "Las plazas no pueden ser negativas.")
    private Integer plazasPrimera;

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

    public Integer getTotalPlazas() {
        return totalPlazas;
    }

    public void setTotalPlazas(Integer totalPlazas) {
        this.totalPlazas = totalPlazas;
    }

    public Integer getPlazasTurista() {
        return plazasTurista;
    }

    public void setPlazasTurista(Integer plazasTurista) {
        this.plazasTurista = plazasTurista;
    }

    public Integer getPlazasPrimera() {
        return plazasPrimera;
    }

    public void setPlazasPrimera(Integer plazasPrimera) {
        this.plazasPrimera = plazasPrimera;
    }
}
