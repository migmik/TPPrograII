package com.tijetravel.tijeback.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "hoteles", uniqueConstraints = @UniqueConstraint(name = "uk_hotel_nombre_ciudad", columnNames = {
        "nombre", "ciudad" }))
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String ciudad;

    @Column(nullable = false)
    private String telefono;

    @Column(name = "capacidad_total", nullable = false)
    private int capacidadTotal;

    protected Hotel() {
    }

    public Hotel(String nombre, String direccion, String ciudad, String telefono, int capacidadTotal) {
        actualizarDatos(nombre, direccion, ciudad, telefono, capacidadTotal);
    }

    public void actualizarDatos(
            String nombre,
            String direccion,
            String ciudad,
            String telefono,
            int capacidadTotal) {
        this.nombre = ValidacionModelo.textoObligatorio(nombre, "nombre");
        this.direccion = ValidacionModelo.textoObligatorio(direccion, "direccion");
        this.ciudad = ValidacionModelo.textoObligatorio(ciudad, "ciudad");
        this.telefono = ValidacionModelo.textoObligatorio(telefono, "telefono");
        this.capacidadTotal = ValidacionModelo.enteroNoNegativo(capacidadTotal, "capacidadTotal");
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
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

    public int getCapacidadTotal() {
        return capacidadTotal;
    }

    @Override
    public String toString() {
        return "Hotel | codigo=" + codigo + ", nombre=" + nombre + ", ciudad=" + ciudad
                + ", capacidadTotal=" + capacidadTotal;
    }
}
