package com.tijetravel.tijefront.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Usuario logueado. Las pantallas deciden que mostrar segun sus permisos
 * (por ejemplo ${usuarioActual.tienePermiso('ADMINISTRAR_HOTELES')}).
 */
public class SesionRespuesta {
    private String dni;

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = (dni != null && dni.isEmpty()) ? null : dni;
    }

    private Integer codigo;
    private String nombreUsuario;
    private String rol;
    private String tipoUsuario;
    private Integer codigoTurista;
    private List<String> permisos = new ArrayList<>();

    public Integer getCodigo() {
        return codigo;
    }

    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    /** Nombre del rol, por ejemplo "Administrador" o "Hotelero". */
    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public Integer getCodigoTurista() {
        return codigoTurista;
    }

    public void setCodigoTurista(Integer codigoTurista) {
        this.codigoTurista = codigoTurista;
    }

    public List<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<String> permisos) {
        this.permisos = permisos == null ? new ArrayList<String>() : permisos;
    }

    public boolean tienePermiso(String permiso) {
        return permisos.contains(permiso);
    }

    public boolean isCliente() {
        return "CLIENTE".equals(tipoUsuario);
    }

    /** Tiene al menos un permiso de administracion: se usa para mostrar la barra de gestion. */
    public boolean isPersonal() {
        return !isCliente();
    }
}
