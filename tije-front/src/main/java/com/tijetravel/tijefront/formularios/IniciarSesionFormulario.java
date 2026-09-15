package com.tijetravel.tijefront.formularios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class IniciarSesionFormulario {
    @NotBlank(message = "Ingresá tu nombre de usuario.")
    @Size(max = 255, message = "El nombre de usuario es demasiado largo.")
    private String nombreUsuario;

    @NotBlank(message = "Ingresá tu contraseña.")
    @Size(max = 72, message = "La contraseña no puede superar 72 caracteres.")
    private String contrasenia;

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
}
