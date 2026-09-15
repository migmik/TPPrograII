package com.tijetravel.tijefront.dto;

public class DisponibilidadVueloRespuesta {
    private Integer numeroVuelo;
    private String claseVuelo;
    private int plazasDisponibles;

    public Integer getNumeroVuelo() { return numeroVuelo; }
    public void setNumeroVuelo(Integer numeroVuelo) { this.numeroVuelo = numeroVuelo; }
    public String getClaseVuelo() { return claseVuelo; }
    public void setClaseVuelo(String claseVuelo) { this.claseVuelo = claseVuelo; }
    public int getPlazasDisponibles() { return plazasDisponibles; }
    public void setPlazasDisponibles(int plazasDisponibles) { this.plazasDisponibles = plazasDisponibles; }
}
