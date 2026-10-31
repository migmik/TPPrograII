package com.tijetravel.tijefront.dto;

/** Una fila del listado de dados de baja, con los textos ya listos para mostrar. */
public class FilaBaja {
    private final Integer codigo;
    private final String descripcion;
    private final String alta;
    private final String baja;

    public FilaBaja(Integer codigo, String descripcion, String alta, String baja) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.alta = alta;
        this.baja = baja;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getAlta() {
        return alta;
    }

    public String getBaja() {
        return baja;
    }
}
