package com.tijetravel.tijefront.formularios;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class RegistroClienteFormulario {
    @NotBlank(message = "Ingresá un nombre de usuario.")
    @Size(max = 255, message = "El nombre no puede superar 255 caracteres.")
    private String nombreUsuario;

    @NotBlank(message = "Ingresá una contraseña.")
    @Size(max = 72, message = "La contraseña no puede superar 72 caracteres.")
    private String contrasenia;

    @NotBlank(message = "Ingresá el DNI.")
    @Pattern(regexp = "[0-9]{7,8}", message = "El DNI debe tener 7 u 8 dígitos, sin puntos ni espacios.")
    private String dni;

    @NotBlank(message = "Ingresá el nombre.")
    @Size(max = 255, message = "Usá como máximo 255 caracteres.")
    private String nombre;

    @NotBlank(message = "Ingresá el apellido.")
    @Size(max = 255, message = "Usá como máximo 255 caracteres.")
    private String apellido;

    @NotBlank(message = "Ingresá la dirección.")
    @Size(max = 255, message = "Usá como máximo 255 caracteres.")
    private String direccion;

    @NotBlank(message = "Ingresá el email.")
    @Email(message = "Ingresá un email válido.")
    @Size(max = 255, message = "Usá como máximo 255 caracteres.")
    private String email;

    @NotBlank(message = "Ingresá el teléfono fijo.")
    @Size(max = 255, message = "Usá como máximo 255 caracteres.")
    private String telefonoFijo;

    @NotBlank(message = "Ingresá el teléfono celular.")
    @Size(max = 255, message = "Usá como máximo 255 caracteres.")
    private String telefonoCelular;

    @NotNull(message = "Elegí una sucursal.")
    @Positive(message = "Elegí una sucursal válida.")
    private Integer codigoSucursal;

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefonoFijo() { return telefonoFijo; }
    public void setTelefonoFijo(String telefonoFijo) { this.telefonoFijo = telefonoFijo; }
    public String getTelefonoCelular() { return telefonoCelular; }
    public void setTelefonoCelular(String telefonoCelular) { this.telefonoCelular = telefonoCelular; }
    public Integer getCodigoSucursal() { return codigoSucursal; }
    public void setCodigoSucursal(Integer codigoSucursal) { this.codigoSucursal = codigoSucursal; }
}