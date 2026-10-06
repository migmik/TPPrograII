package com.tijetravel.tijefront.formularios;

import jakarta.validation.constraints.*;

public class GuardarTuristaFormulario {
    @NotBlank(message = "Ingresá el DNI.")
    @Pattern(regexp = "[0-9]{7,8}", message = "El DNI debe tener 7 u 8 dígitos, sin puntos ni espacios.")
    private String dni;

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    @NotBlank(message = "Este dato es obligatorio.")
    @Size(max = 255, message = "Usá como máximo 255 caracteres.")
    private String nombre;

    @NotBlank(message = "Este dato es obligatorio.")
    @Size(max = 255, message = "Usá como máximo 255 caracteres.")
    private String apellido;

    @NotBlank(message = "Este dato es obligatorio.")
    @Size(max = 255, message = "Usá como máximo 255 caracteres.")
    private String direccion;

    @NotBlank(message = "Este dato es obligatorio.")
    @Size(max = 255, message = "Usá como máximo 255 caracteres.")
    @Email(message = "Ingresá un email válido.")
    private String email;

    @NotBlank(message = "Este dato es obligatorio.")
    @Size(max = 255, message = "Usá como máximo 255 caracteres.")
    private String telefonoFijo;

    @NotBlank(message = "Este dato es obligatorio.")
    @Size(max = 255, message = "Usá como máximo 255 caracteres.")
    private String telefonoCelular;

    @Positive(message = "Elegí un código positivo.")
    private Integer codigoSucursal;

    @Positive(message = "Elegí un código positivo.")
    private Integer codigoTitular;

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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefonoFijo() {
        return telefonoFijo;
    }

    public void setTelefonoFijo(String telefonoFijo) {
        this.telefonoFijo = telefonoFijo;
    }

    public String getTelefonoCelular() {
        return telefonoCelular;
    }

    public void setTelefonoCelular(String telefonoCelular) {
        this.telefonoCelular = telefonoCelular;
    }

    public Integer getCodigoSucursal() {
        return codigoSucursal;
    }

    public void setCodigoSucursal(Integer codigoSucursal) {
        this.codigoSucursal = codigoSucursal;
    }

    public Integer getCodigoTitular() {
        return codigoTitular;
    }

    public void setCodigoTitular(Integer codigoTitular) {
        this.codigoTitular = codigoTitular;
    }
}
