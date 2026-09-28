package com.tijetravel.tijefront.dto;

public class TuristaRespuesta {
    private Integer codigo;
    private String nombre;
    private String apellido;
    private String direccion;
    private String email;
    private String telefonoFijo;
    private String telefonoCelular;
    private Integer codigoSucursal;
    private Integer codigoTitular;
    private boolean titular;

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
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

    public boolean isTitular() {
        return titular;
    }

    public void setTitular(boolean titular) {
        this.titular = titular;
    }
}
