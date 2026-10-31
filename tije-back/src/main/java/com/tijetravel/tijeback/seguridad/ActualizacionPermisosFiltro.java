package com.tijetravel.tijeback.seguridad;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tijetravel.tijeback.modelos.Usuario;
import com.tijetravel.tijeback.repositorios.UsuarioRepositorio;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Los permisos se guardan en la sesion al iniciar sesion. Como el administrador
 * puede cambiar un rol (o dar de baja un usuario) en cualquier momento, en cada
 * pedido se vuelven a leer de la base. Si el usuario ya no esta activo, se
 * cierra su sesion.
 *
 * No es un @Component a proposito: si lo fuera, Spring Boot tambien lo
 * registraria como filtro comun, fuera de la cadena de seguridad.
 */
public class ActualizacionPermisosFiltro extends OncePerRequestFilter {
    private final UsuarioRepositorio usuarioRepositorio;

    public ActualizacionPermisosFiltro(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest solicitud, HttpServletResponse respuesta,
            FilterChain cadena) throws ServletException, IOException {
        SecurityContext contexto = SecurityContextHolder.getContext();
        Authentication actual = contexto.getAuthentication();
        if (actual != null && actual.getPrincipal() instanceof UsuarioAutenticado) {
            UsuarioAutenticado principal = (UsuarioAutenticado) actual.getPrincipal();
            Usuario usuario = usuarioRepositorio.findById(principal.getCodigo()).orElse(null);
            if (usuario == null || !usuario.estaActivo()) {
                SecurityContextHolder.clearContext();
                HttpSession sesion = solicitud.getSession(false);
                if (sesion != null) {
                    sesion.invalidate();
                }
            } else {
                UsuarioAutenticado actualizado = UsuarioAutenticado.desde(usuario);
                actualizado.eraseCredentials();
                contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                        actualizado, null, actualizado.getAuthorities()));
            }
        }
        cadena.doFilter(solicitud, respuesta);
    }
}
