package com.tijetravel.tijeback.modelos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.TipoUsuario;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/**
 * Conjunto de permisos que se le asigna a un usuario. Los roles se crean y se
 * editan desde la aplicacion (por ejemplo un rol "Hotelero" que solo administra
 * hoteles), asi no hace falta tocar el codigo para cambiar que puede hacer cada uno.
 */
@Entity
@Table(name = "roles")
public class Rol extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false, updatable = false, length = 20)
    private TipoUsuario tipoUsuario;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rol_permisos", joinColumns = @JoinColumn(name = "rol_codigo"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permiso", nullable = false, length = 40)
    private Set<Permiso> permisos = new HashSet<>();

    protected Rol() {
    }

    public Rol(String nombre, TipoUsuario tipoUsuario, List<Permiso> permisos) {
        this.tipoUsuario = ValidacionModelo.obligatorio(tipoUsuario, "tipoUsuario");
        actualizar(nombre, permisos);
    }

    /** Cambia el nombre y los permisos. El tipo de usuario no cambia porque ya hay cuentas que lo usan. */
    public void actualizar(String nombre, List<Permiso> permisos) {
        String nombreValidado = ValidacionModelo.textoObligatorio(nombre, "nombre");
        if (nombreValidado.length() > 60) {
            throw new IllegalArgumentException("El nombre del rol no puede superar 60 caracteres");
        }
        ValidacionModelo.obligatorio(permisos, "permisos");
        for (Permiso permiso : permisos) {
            if (!tipoUsuario.admite(permiso)) {
                throw new IllegalArgumentException("Un rol de clientes no puede tener el permiso " + permiso);
            }
        }
        this.nombre = nombreValidado;
        this.permisos.clear();
        this.permisos.addAll(permisos);
    }

    public boolean tienePermiso(Permiso permiso) {
        return permiso != null && permisos.contains(permiso);
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    /** Permisos del rol en el orden en que estan declarados en el enum. */
    public List<Permiso> getPermisos() {
        List<Permiso> ordenados = new ArrayList<>();
        for (Permiso permiso : Permiso.values()) {
            if (permisos.contains(permiso)) {
                ordenados.add(permiso);
            }
        }
        return ordenados;
    }

    @Override
    public String toString() {
        return "Rol | codigo=" + codigo + ", nombre=" + nombre + ", tipo=" + tipoUsuario;
    }
}
