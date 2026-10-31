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
public abstract class Usuario extends EntidadBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer codigo;

    @Column(name = "nombre_usuario", nullable = false)
    private String nombreUsuario;

    // Solo los empleados guardan su DNI aca; el del cliente esta en su turista.
    @Column(length = 8)
    private String dni;

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

    public String getDni() {
        return dni;
    }

    public void actualizarDni(String dni) {
        if (!tieneDniPropio()) {
            throw new IllegalArgumentException("El DNI del cliente se modifica en su turista asociado");
        }
        this.dni = ValidacionModelo.dni(dni);
    }

    /** Indica si el DNI se guarda en el usuario (empleados) o se toma de otro lado. */
    public boolean tieneDniPropio() {
        return true;
    }

    /** Por defecto un usuario puede ver a cualquier turista. */
    public boolean puedeVerTurista(Turista turista) {
        return true;
    }

    /**
     * Codigo del titular al que se limitan las consultas del usuario, o null si
     * puede consultar a todos los turistas.
     */
    public Integer getCodigoGrupoFamiliar() {
        return null;
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
        if (turista == null) {
            return null;
        }
        return turista.getCodigo();
    }

    public abstract boolean tienePermiso(Permiso permiso);

    @Override
    public String toString() {
        return "Usuario | codigo=" + codigo + ", nombreUsuario=" + nombreUsuario + ", rol=" + rol;
    }
}
