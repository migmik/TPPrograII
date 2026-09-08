package com.tijetravel.tijeback.controladores;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tijetravel.tijeback.excepciones.CredencialesInvalidasException;
import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;

@Service
@Transactional(readOnly = true)
public class AutenticacionControlador {
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder codificadorContrasenias;

    public AutenticacionControlador(
            UsuarioRepositorio usuarioRepositorio,
            PasswordEncoder codificadorContrasenias) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.codificadorContrasenias = codificadorContrasenias;
    }

    public Usuario iniciarSesion(String nombreUsuario, String contrasenia) {
        if (nombreUsuario == null || contrasenia == null) {
            throw new CredencialesInvalidasException("Usuario o contrasenia incorrectos");
        }

        Usuario usuario = usuarioRepositorio.findByNombreUsuarioIgnoreCase(nombreUsuario.trim())
                .orElseThrow(() -> new CredencialesInvalidasException(
                        "Usuario o contrasenia incorrectos"));
        if (!codificadorContrasenias.matches(contrasenia, usuario.getContrasenia())) {
            throw new CredencialesInvalidasException("Usuario o contrasenia incorrectos");
        }
        return usuario;
    }
}
