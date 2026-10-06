package com.tijetravel.tijefront.formularios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class GuardarHotelFormulario {
    @NotBlank(message = "Ingresá el nombre.")
    @Size(max = 255)
    private String nombre;
    @NotBlank(message = "Ingresá la dirección.")
    @Size(max = 255)
    private String direccion;
    @NotBlank(message = "Ingresá la ciudad.")
    @Size(max = 255)
    private String ciudad;
    @NotBlank(message = "Ingresá el teléfono.")
    @Size(max = 255)
    private String telefono;
    @NotNull(message = "Ingresá la capacidad.")
    @PositiveOrZero(message = "La capacidad no puede ser negativa.")
    private Integer capacidadTotal;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Integer getCapacidadTotal() {
        return capacidadTotal;
    }

    public void setCapacidadTotal(Integer capacidadTotal) {
        this.capacidadTotal = capacidadTotal;
    }
}
