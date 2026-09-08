package com.tijetravel.tijeback.modelos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.RolUsuario;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario")
public abstract class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(name = "nombre_usuario", nullable = false, unique = true)
    private String nombreUsuario;

    @JsonIgnore
    @Column(nullable = false)
    private String contrasenia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private RolUsuario rol;

    protected Usuario() {
    }

    protected Usuario(String nombreUsuario, String contrasenia, RolUsuario rol) {
        this.rol = ValidacionModelo.obligatorio(rol, "rol");
        actualizarCredenciales(nombreUsuario, contrasenia);
    }

    public void actualizarCredenciales(String nombreUsuario, String contrasenia) {
        this.nombreUsuario = ValidacionModelo.textoObligatorio(nombreUsuario, "nombreUsuario");
        this.contrasenia = ValidacionModelo.textoObligatorio(contrasenia, "contrasenia");
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    @JsonIgnore
    public String getContrasenia() {
        return contrasenia;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public Turista getTurista() {
        return null;
    }

    public Integer getCodigoTurista() {
        Turista turista = getTurista();
        return turista == null ? null : turista.getCodigo();
    }

    public abstract boolean tienePermiso(Permiso permiso);

    @Override
    public String toString() {
        return "Usuario | codigo=" + codigo + ", nombreUsuario=" + nombreUsuario + ", rol=" + rol;
    }
}
