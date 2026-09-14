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

    public Usuario obtener(Authentication autenticacion) {
        if (autenticacion == null || !(autenticacion.getPrincipal() instanceof UsuarioAutenticado principal)) {
            throw new CredencialesInvalidasException("La sesion no es valida");
        }

        Usuario usuario = usuarioRepositorio.findById(principal.getCodigo())
                .orElseThrow(() -> new CredencialesInvalidasException("La sesion no es valida"));
        usuario.getCodigoTurista();
        return usuario;
    }
}
