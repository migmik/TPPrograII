package com.tijetravel.tijefront.dto;

public class CsrfRespuesta {
    private String nombreEncabezado;
    private String nombreParametro;
    private String token;

    public String getNombreEncabezado() {
        return nombreEncabezado;
    }

    public void setNombreEncabezado(String nombreEncabezado) {
        this.nombreEncabezado = nombreEncabezado;
    }

    public String getNombreParametro() {
        return nombreParametro;
    }

    public void setNombreParametro(String nombreParametro) {
        this.nombreParametro = nombreParametro;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
