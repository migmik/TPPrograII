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
import com.tijetravel.tijeback.modelos.Usuario;

/** Datos del usuario que Spring Security guarda en la sesion. Las autoridades son sus permisos. */
public final class UsuarioAutenticado implements UserDetails, CredentialsContainer {
    @Serial
    private static final long serialVersionUID = 2L;

    private final Integer codigo;
    private final String nombreUsuario;
    private String contrasenia;
    private final Integer codigoTurista;
    private final List<GrantedAuthority> autoridades;

    private UsuarioAutenticado(
            Integer codigo,
            String nombreUsuario,
            String contrasenia,
            Integer codigoTurista,
            List<GrantedAuthority> autoridades) {
        this.codigo = codigo;
        this.nombreUsuario = nombreUsuario;
        this.contrasenia = contrasenia;
        this.codigoTurista = codigoTurista;
        this.autoridades = List.copyOf(autoridades);
    }

    public static UsuarioAutenticado desde(Usuario usuario) {
        List<GrantedAuthority> autoridades = new ArrayList<>();
        autoridades.add(new SimpleGrantedAuthority("ROLE_" + usuario.getTipoUsuario().name()));
        for (Permiso permiso : Permiso.values()) {
            if (usuario.tienePermiso(permiso)) {
                autoridades.add(new SimpleGrantedAuthority(permiso.name()));
            }
        }

        return new UsuarioAutenticado(
                usuario.getCodigo(),
                usuario.getNombreUsuario(),
                usuario.getContrasenia(),
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
