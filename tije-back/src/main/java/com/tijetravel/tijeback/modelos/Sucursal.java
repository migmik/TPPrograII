package com.tijetravel.tijeback.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sucursales")
public class Sucursal extends EntidadBase {
    public static final String CIUDAD_SIN_ESPECIFICAR = "Sin especificar";
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
        this(direccion, telefono, CIUDAD_SIN_ESPECIFICAR);
    }

    public Sucursal(String direccion, String telefono, String ciudad) {
        actualizarDatos(direccion, telefono, ciudad);
    }

    public void actualizarDatos(String direccion, String telefono, String ciudad) {
        String direccionValidada = ValidacionModelo.textoObligatorio(direccion, "direccion");
        String telefonoValidado = ValidacionModelo.textoObligatorio(telefono, "telefono");
        String ciudadValidada = ValidacionModelo.textoObligatorio(ciudad, "ciudad");
        this.direccion = direccionValidada;
        this.telefono = telefonoValidado;
        this.ciudad = ciudadValidada;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public String toString() {
        return "Sucursal | codigo=" + codigo + ", ciudad=" + ciudad
                + ", direccion=" + direccion + ", telefono=" + telefono;
    }
}
