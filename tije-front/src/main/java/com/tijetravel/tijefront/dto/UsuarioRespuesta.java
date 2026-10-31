package com.tijetravel.tijefront.dto;

import java.time.LocalDateTime;

public class UsuarioRespuesta {
    private String dni;

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = (dni != null && dni.isEmpty()) ? null : dni;
    }

    private Integer codigo;
    private String nombreUsuario;
    private String rol;
    private Integer codigoTurista;

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Integer getCodigoTurista() {
        return codigoTurista;
    }

    public void setCodigoTurista(Integer codigoTurista) {
        this.codigoTurista = codigoTurista;
    }

    private Integer codigoRol;
    private String tipoUsuario;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaBaja;

    public Integer getCodigoRol() {
        return codigoRol;
    }

    public void setCodigoRol(Integer codigoRol) {
        this.codigoRol = codigoRol;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public boolean isCliente() {
        return "CLIENTE".equals(tipoUsuario);
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(LocalDateTime fechaBaja) {
        this.fechaBaja = fechaBaja;
    }
}
