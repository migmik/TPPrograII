package com.tijetravel.tijefront.formularios;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos de un rol: nombre, para que tipo de usuario es y los permisos tildados. */
public class GuardarRolFormulario {
    @NotBlank(message = "Ingresá un nombre para el rol.")
    @Size(max = 60, message = "El nombre no puede superar 60 caracteres.")
    private String nombre;

    @NotBlank(message = "Elegí si el rol es para empleados o para clientes.")
    @Pattern(regexp = "EMPLEADO|CLIENTE", message = "Elegí un tipo de usuario válido.")
    private String tipoUsuario = "EMPLEADO";

    private List<String> permisos = new ArrayList<>();

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public List<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<String> permisos) {
        this.permisos = permisos == null ? new ArrayList<String>() : permisos;
    }
}
