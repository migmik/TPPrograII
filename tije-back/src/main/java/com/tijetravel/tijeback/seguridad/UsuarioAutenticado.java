package com.tijetravel.tijeback.seguridad;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tijetravel.tijeback.enums.Permiso;
import com.tijetravel.tijeback.enums.RolUsuario;
import com.tijetravel.tijeback.modelos.Usuario;

public final class UsuarioAutenticado implements UserDetails, CredentialsContainer {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Integer codigo;
    private final String nombreUsuario;
    private String contrasenia;
    private final RolUsuario rol;
    private final Integer codigoTurista;
    private final List<GrantedAuthority> autoridades;

    private UsuarioAutenticado(
            Integer codigo,
            String nombreUsuario,
            String contrasenia,
            RolUsuario rol,
            Integer codigoTurista,
            List<GrantedAuthority> autoridades) {
        this.codigo = codigo;
        this.nombreUsuario = nombreUsuario;
        this.contrasenia = contrasenia;
        this.rol = rol;
        this.codigoTurista = codigoTurista;
        this.autoridades = List.copyOf(autoridades);
    }

    public static UsuarioAutenticado desde(Usuario usuario) {
        List<GrantedAuthority> autoridades = new ArrayList<>();
        autoridades.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
        for (Permiso permiso : Permiso.values()) {
            if (usuario.tienePermiso(permiso)) {
                autoridades.add(new SimpleGrantedAuthority(permiso.name()));
            }
        }

        return new UsuarioAutenticado(
                usuario.getCodigo(),
                usuario.getNombreUsuario(),
                usuario.getContrasenia(),
                usuario.getRol(),
                usuario.getCodigoTurista(),
                autoridades);
    }

    public Integer getCodigo() {
        return codigo;
    }

    @Override
    public String getUsername() {
        return nombreUsuario;
    }

    @Override
    public String getPassword() {
        return contrasenia;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public Integer getCodigoTurista() {
        return codigoTurista;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return autoridades;
    }

    @Override
    public void eraseCredentials() {
        contrasenia = null;
    }
}
