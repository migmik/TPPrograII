package com.tijetravel.tijeback.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "turistas")
public class Turista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "telefono_fijo", nullable = false)
    private String telefonoFijo;

    @Column(name = "telefono_celular", nullable = false)
    private String telefonoCelular;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sucursal_codigo", nullable = false)
    private Sucursal sucursalContratacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "titular_codigo")
    private Turista titular;

    protected Turista() {
    }

    public Turista(
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular,
            Sucursal sucursalContratacion) {
        this(nombre, apellido, direccion, email, telefonoFijo, telefonoCelular, sucursalContratacion, null);
    }

    public Turista(
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular,
            Sucursal sucursalContratacion,
            Turista titular) {
        if (titular != null && !titular.isTitular()) {
            throw new IllegalArgumentException("El turista asociado debe ser titular");
        }
        this.titular = titular;
        actualizarDatos(nombre, apellido, direccion, email, telefonoFijo, telefonoCelular, sucursalContratacion);
    }

    public void actualizarDatos(
            String nombre,
            String apellido,
            String direccion,
            String email,
            String telefonoFijo,
            String telefonoCelular,
            Sucursal sucursalContratacion) {
        this.nombre = ValidacionModelo.textoObligatorio(nombre, "nombre");
        this.apellido = ValidacionModelo.textoObligatorio(apellido, "apellido");
        this.direccion = ValidacionModelo.textoObligatorio(direccion, "direccion");
        this.email = ValidacionModelo.email(email);
        this.telefonoFijo = ValidacionModelo.textoObligatorio(telefonoFijo, "telefonoFijo");
        this.telefonoCelular = ValidacionModelo.textoObligatorio(telefonoCelular, "telefonoCelular");
        this.sucursalContratacion = ValidacionModelo.obligatorio(
                sucursalContratacion, "sucursalContratacion");
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefonoFijo() {
        return telefonoFijo;
    }

    public String getTelefonoCelular() {
        return telefonoCelular;
    }

    public Sucursal getSucursalContratacion() {
        return sucursalContratacion;
    }

    public Turista getTitular() {
        return titular;
    }

    public Integer getCodigoTitular() {
        return titular == null ? null : titular.getCodigo();
    }

    public boolean isTitular() {
        return titular == null;
    }

    @Override
    public String toString() {
        return "Turista | codigo=" + codigo + ", nombre=" + nombre + ", apellido=" + apellido
                + ", email=" + email;
    }
}
