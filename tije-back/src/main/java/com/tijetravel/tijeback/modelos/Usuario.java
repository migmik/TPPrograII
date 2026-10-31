package com.tijetravel.tijeback.modelos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.TipoUsuario;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Cuenta para entrar al sistema. Hay dos tipos (Empleado y Cliente) y cada
 * usuario tiene un rol que define sus permisos.
 */
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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rol_codigo", nullable = false)
    private Rol rol;

    protected Usuario() {
    }

    protected Usuario(String nombreUsuario, String contrasenia, Rol rol) {
        cambiarRol(rol);
        actualizarCredenciales(nombreUsuario, contrasenia);
    }

    /** Cada subclase dice que tipo de cuenta es; el rol tiene que ser de ese mismo tipo. */
    public abstract TipoUsuario getTipoUsuario();

    public void cambiarRol(Rol nuevoRol) {
        ValidacionModelo.obligatorio(nuevoRol, "rol");
        if (nuevoRol.getTipoUsuario() != getTipoUsuario()) {
            throw new IllegalArgumentException("El rol " + nuevoRol.getNombre() + " no es para este tipo de usuario");
        }
        this.rol = nuevoRol;
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

    /** Los permisos salen del rol; si el rol se dio de baja, el usuario no puede hacer nada. */
    public boolean tienePermiso(Permiso permiso) {
        return rol.estaActivo() && rol.tienePermiso(permiso);
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

    public Rol getRol() {
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

    @Override
    public String toString() {
        return "Usuario | codigo=" + codigo + ", nombreUsuario=" + nombreUsuario + ", rol=" + rol.getNombre();
    }
}
