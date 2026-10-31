package com.tijetravel.tijefront.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Un permiso que se puede tildar en un rol, con el texto que se muestra. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PermisoRespuesta {
    private String nombre;
    private String descripcion;
    private boolean soloEmpleados;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isSoloEmpleados() {
        return soloEmpleados;
    }

    public void setSoloEmpleados(boolean soloEmpleados) {
        this.soloEmpleados = soloEmpleados;
    }
}
