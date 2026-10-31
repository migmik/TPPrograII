package com.tijetravel.tijeback.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "sucursales", uniqueConstraints = @UniqueConstraint(name = "uk_sucursal_direccion", columnNames = "direccion"))
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(nullable = false)
    private String telefono;

    protected Sucursal() {
    }

    public Sucursal(String direccion, String telefono) {
        this(direccion, telefono, "Sin especificar");
    }

    public Sucursal(String direccion, String telefono, String ciudad) {
        actualizarDatos(direccion, telefono, ciudad);
    }

    public void actualizarDatos(String direccion, String telefono) {
        actualizarDatos(direccion, telefono, this.ciudad);
    }

    public void actualizarDatos(String direccion, String telefono, String ciudad) {
        this.direccion = ValidacionModelo.textoObligatorio(direccion, "direccion");
        this.telefono = ValidacionModelo.textoObligatorio(telefono, "telefono");
        this.ciudad = ValidacionModelo.textoObligatorio(ciudad, "ciudad");
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCiudad() { return ciudad; }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public String toString() {
        return "Sucursal | codigo=" + codigo + ", ciudad=" + ciudad
                + ", direccion=" + direccion + ", telefono=" + telefono;
    }
}
