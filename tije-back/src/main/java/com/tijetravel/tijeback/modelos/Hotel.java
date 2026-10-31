package com.tijetravel.tijeback.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "hoteles")
public class Hotel extends EntidadBase {
    public static final int PRECIO_MINIMO = 25000;
    public static final int PRECIO_MAXIMO = 55000;
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

    @Column(name = "precio_noche_persona", nullable = false)
    private int precioNochePersona;

    protected Hotel() {
    }

    public Hotel(String nombre, String direccion, String ciudad, String telefono, int capacidadTotal) {
        this(nombre, direccion, ciudad, telefono, capacidadTotal, PRECIO_MINIMO);
    }

    public Hotel(String nombre, String direccion, String ciudad, String telefono, int capacidadTotal,
            int precioNochePersona) {
        actualizarDatos(nombre, direccion, ciudad, telefono, capacidadTotal, precioNochePersona);
    }

    public void actualizarDatos(String nombre, String direccion, String ciudad, String telefono,
            int capacidadTotal, int precioNochePersona) {
        // Primero se valida todo para no dejar el hotel modificado a medias.
        String nombreValidado = ValidacionModelo.textoObligatorio(nombre, "nombre");
        String direccionValidada = ValidacionModelo.textoObligatorio(direccion, "direccion");
        String ciudadValidada = ValidacionModelo.textoObligatorio(ciudad, "ciudad");
        String telefonoValidado = ValidacionModelo.textoObligatorio(telefono, "telefono");
        ValidacionModelo.enteroNoNegativo(capacidadTotal, "capacidadTotal");
        if (precioNochePersona < PRECIO_MINIMO || precioNochePersona > PRECIO_MAXIMO
                || precioNochePersona % 1000 != 0) {
            throw new IllegalArgumentException("El precio del hotel debe estar entre " + PRECIO_MINIMO + " y "
                    + PRECIO_MAXIMO + " y ser multiplo de 1000");
        }
        this.nombre = nombreValidado;
        this.direccion = direccionValidada;
        this.ciudad = ciudadValidada;
        this.telefono = telefonoValidado;
        this.capacidadTotal = capacidadTotal;
        this.precioNochePersona = precioNochePersona;
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

    public int getPrecioNochePersona() {
        return precioNochePersona;
    }

    @Override
    public String toString() {
        return "Hotel | codigo=" + codigo + ", nombre=" + nombre + ", ciudad=" + ciudad
                + ", capacidadTotal=" + capacidadTotal;
    }
}
