package com.tijetravel.tijefront.formularios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ModificarUsuarioFormulario {
    @jakarta.validation.constraints.Pattern(regexp = "[0-9]{7,8}", message = "El DNI debe tener 7 u 8 dígitos, sin puntos ni espacios.")
    private String dni;

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = (dni != null && dni.isEmpty()) ? null : dni; }

    @NotBlank(message = "Ingresá un nombre de usuario.")
    @Size(max = 255, message = "El nombre no puede superar 255 caracteres.")
    private String nombreUsuario;

    @NotBlank(message = "Ingresá la nueva contraseña.")
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
