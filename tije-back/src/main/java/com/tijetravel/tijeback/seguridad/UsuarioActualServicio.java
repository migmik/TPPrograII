package com.tijetravel.tijeback.seguridad;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tijetravel.tijeback.excepciones.CredencialesInvalidasException;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;

@Service
@Transactional(readOnly = true)
public class UsuarioActualServicio {
    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioActualServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    /** Devuelve el usuario de la sesion o lanza una excepcion si no hay sesion valida. */
    public Usuario obtener(Authentication autenticacion) {
        Usuario usuario = buscar(autenticacion);
        if (usuario == null) {
            throw new CredencialesInvalidasException("La sesion no es valida");
        }
        return usuario;
    }

    /** Devuelve el usuario de la sesion o null si la consulta es anonima. */
    public Usuario buscar(Authentication autenticacion) {
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof UsuarioAutenticado)) {
            return null;
        }
        UsuarioAutenticado principal = (UsuarioAutenticado) autenticacion.getPrincipal();
        return usuarioRepositorio.findById(principal.getCodigo()).orElse(null);
    }
}
