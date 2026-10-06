package com.tijetravel.tijefront.formularios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GuardarSucursalFormulario {
    @NotBlank(message = "Ingresá la dirección.")
    @Size(max = 255, message = "La dirección no puede superar 255 caracteres.")
    private String direccion;

    @NotBlank(message = "Ingresá el teléfono.")
    @Size(max = 255, message = "El teléfono no puede superar 255 caracteres.")
    private String telefono;

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
