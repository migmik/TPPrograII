package com.tijetravel.tijeback.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sucursales")
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String telefono;

    protected Sucursal() {
    }

    public Sucursal(String direccion, String telefono) {
        actualizarDatos(direccion, telefono);
    }

    public void actualizarDatos(String direccion, String telefono) {
        this.direccion = ValidacionModelo.textoObligatorio(direccion, "direccion");
        this.telefono = ValidacionModelo.textoObligatorio(telefono, "telefono");
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public String toString() {
        return "Sucursal | codigo=" + codigo + ", direccion=" + direccion + ", telefono=" + telefono;
    }
}
