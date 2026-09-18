package com.tijetravel.tijefront.dto;

public class SesionRespuesta {
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
}
